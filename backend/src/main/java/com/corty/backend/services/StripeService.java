package com.corty.backend.services;

import com.corty.backend.config.StripeConfig;
import com.corty.backend.dto.PaymentIntentResponse;
import com.corty.backend.dto.SavedCardResponse;
import com.corty.backend.dto.SetupIntentResponse;
import com.corty.backend.exception.CortyException;
import com.corty.backend.model.Booking;
import com.corty.backend.model.Player;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.model.enums.PaymentMethod;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod.Card;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SetupIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    private final StripeConfig stripeConfig;
    private final BookingRepository bookingRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public PaymentIntentResponse createPaymentIntent(Long bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CortyException("Reserva no encontrada", HttpStatus.NOT_FOUND));

        if (!booking.getOwner().getUsername().equals(username)) {
            throw new CortyException("No autorizado", HttpStatus.FORBIDDEN);
        }
        if (booking.isFullyPaid()) {
            throw new CortyException("La reserva ya está pagada", HttpStatus.CONFLICT);
        }

        // Stripe trabaja en céntimos (enteros)
        long amountCents = booking.getTotalPrice()
                .multiply(java.math.BigDecimal.valueOf(100))
                .longValue();

        String dateLabel = booking.getDate()
                .format(DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es")));
        String description = String.format("%s · %s %s",
                booking.getCourt().getName(),
                dateLabel,
                booking.getStartTime().toString().substring(0, 5));

        // Obtener el stripeCustomerId del owner si existe
        Player owner = playerRepository.findByUser_IdUser(booking.getOwner().getIdUser()).orElse(null);
        String customerId = owner != null ? owner.getStripeCustomerId() : null;

        try {
            PaymentIntentCreateParams.Builder intentBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency("eur")
                    .setDescription(description)
                    .setReceiptEmail(booking.getOwner().getEmail())
                    .putMetadata("bookingId", bookingId.toString())
                    .addPaymentMethodType("card");

            if (customerId != null) {
                intentBuilder.setCustomer(customerId);
            }

            PaymentIntent intent = PaymentIntent.create(intentBuilder.build());

            return PaymentIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .publishableKey(stripeConfig.getPublishableKey())
                    .bookingId(bookingId)
                    .amount(booking.getTotalPrice().toPlainString())
                    .description(description)
                    .build();

        } catch (StripeException e) {
            throw new CortyException("Error al crear el pago: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public SetupIntentResponse createSetupIntent(String username) {
        Player player = getPlayer(username);

        String customerId = player.getStripeCustomerId();
        if (customerId == null) {
            try {
                Customer customer = Customer.create(
                        CustomerCreateParams.builder()
                                .setEmail(player.getUser().getEmail())
                                .setName(player.getName() + " " + player.getSurname())
                                .build()
                );
                customerId = customer.getId();
                player.setStripeCustomerId(customerId);
                playerRepository.save(player);
            } catch (StripeException e) {
                throw new CortyException("Error al crear cliente Stripe", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        try {
            com.stripe.model.SetupIntent intent = com.stripe.model.SetupIntent.create(
                    SetupIntentCreateParams.builder()
                            .setCustomer(customerId)
                            .addPaymentMethodType("card")
                            .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
                            .build()
            );
            return SetupIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .publishableKey(stripeConfig.getPublishableKey())
                    .build();
        } catch (StripeException e) {
            throw new CortyException("Error al crear SetupIntent", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public SavedCardResponse getSavedCard(String username) {
        Player player = getPlayer(username);
        if (player.getDefaultPaymentMethodId() == null) return null;

        try {
            com.stripe.model.PaymentMethod pm =
                    com.stripe.model.PaymentMethod.retrieve(player.getDefaultPaymentMethodId());
            Card card = pm.getCard();
            return SavedCardResponse.builder()
                    .paymentMethodId(pm.getId())
                    .brand(card.getBrand())
                    .last4(card.getLast4())
                    .expMonth(card.getExpMonth().intValue())
                    .expYear(card.getExpYear().intValue())
                    .build();
        } catch (StripeException e) {
            return null;
        }
    }

    @Transactional
    public void savePaymentMethod(String username, String paymentMethodId) {
        Player player = getPlayer(username);
        String oldPmId = player.getDefaultPaymentMethodId();

        try {
            // Adjuntar al customer si no lo está
            com.stripe.model.PaymentMethod pm =
                    com.stripe.model.PaymentMethod.retrieve(paymentMethodId);
            if (pm.getCustomer() == null) {
                pm.attach(com.stripe.param.PaymentMethodAttachParams.builder()
                        .setCustomer(player.getStripeCustomerId())
                        .build());
            }
            // Desconectar la anterior
            if (oldPmId != null && !oldPmId.equals(paymentMethodId)) {
                com.stripe.model.PaymentMethod old =
                        com.stripe.model.PaymentMethod.retrieve(oldPmId);
                old.detach();
            }
            player.setDefaultPaymentMethodId(paymentMethodId);
            playerRepository.save(player);
        } catch (StripeException e) {
            throw new CortyException("Error al guardar el método de pago", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void deletePaymentMethod(String username) {
        Player player = getPlayer(username);
        String pmId = player.getDefaultPaymentMethodId();
        if (pmId == null) return;

        try {
            com.stripe.model.PaymentMethod pm =
                    com.stripe.model.PaymentMethod.retrieve(pmId);
            pm.detach();
        } catch (StripeException ignored) {}

        player.setDefaultPaymentMethodId(null);
        playerRepository.save(player);
    }

    @Transactional
    public String chargePlayer(Player player, BigDecimal amount, String description) {
        String pmId = player.getDefaultPaymentMethodId();
        if (pmId == null) {
            throw new CortyException("El jugador no tiene método de pago guardado", HttpStatus.PAYMENT_REQUIRED);
        }
        String customerId = player.getStripeCustomerId();
        if (customerId == null) {
            throw new CortyException("El jugador no tiene cuenta Stripe", HttpStatus.PAYMENT_REQUIRED);
        }

        long amountCents = amount.multiply(java.math.BigDecimal.valueOf(100)).longValue();

        try {
            PaymentIntent intent = PaymentIntent.create(
                    PaymentIntentCreateParams.builder()
                            .setAmount(amountCents)
                            .setCurrency("eur")
                            .setCustomer(customerId)
                            .setPaymentMethod(pmId)
                            .setDescription(description)
                            .setConfirm(true)
                            .setOffSession(true)
                            .build()
            );
            return intent.getId();
        } catch (StripeException e) {
            throw new CortyException("Error al cobrar al jugador: " + e.getMessage(), HttpStatus.PAYMENT_REQUIRED);
        }
    }

    private Player getPlayer(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CortyException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        return playerRepository.findByUser_IdUser(user.getIdUser())
                .orElseThrow(() -> new CortyException("Jugador no encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        if (stripeConfig.getWebhookSecret() == null || stripeConfig.getWebhookSecret().isBlank()) return;

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new CortyException("Webhook inválido", HttpStatus.BAD_REQUEST);
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            try {
                String rawJson = event.getDataObjectDeserializer().getRawJson();
                com.fasterxml.jackson.databind.JsonNode node =
                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(rawJson);
                String intentId     = node.path("id").asText(null);
                String bookingIdStr = node.path("metadata").path("bookingId").asText(null);
                if (bookingIdStr == null || bookingIdStr.isEmpty()) return;
                Long bookingId = Long.parseLong(bookingIdStr);
                bookingRepository.findById(bookingId).ifPresent(booking -> {
                    booking.setFullyPaid(true);
                    booking.setPaymentId(intentId);
                    booking.setPaymentMethod(PaymentMethod.ONLINE);
                    booking.setBookingStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);
                });
            } catch (Exception e) {
                log.error("Error procesando webhook: {}", e.getMessage());
            }
        }
    }
}
