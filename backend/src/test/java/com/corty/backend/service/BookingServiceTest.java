package com.corty.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.corty.backend.dto.BookingCreateRequest;
import com.corty.backend.dto.BookingCreateResponse;
import com.corty.backend.dto.CancellationResponse;
import com.corty.backend.exception.BusinessLogicException;
import com.corty.backend.mapper.BookingMapper;
import com.corty.backend.model.Booking;
import com.corty.backend.model.Club;
import com.corty.backend.model.Court;
import com.corty.backend.model.Player;
import com.corty.backend.model.PlayerBooking;
import com.corty.backend.model.PlayerSport;
import com.corty.backend.model.Role;
import com.corty.backend.model.Sport;
import com.corty.backend.model.User;
import com.corty.backend.model.enums.BookingStatus;
import com.corty.backend.model.enums.BookingType;
import com.corty.backend.model.enums.NotificationType;
import com.corty.backend.model.enums.PaymentMethod;
import com.corty.backend.repository.BookingRepository;
import com.corty.backend.repository.ClubBalanceEntryRepository;
import com.corty.backend.repository.CourtRepository;
import com.corty.backend.repository.JoinRequestRepository;
import com.corty.backend.repository.PlayerBookingRepository;
import com.corty.backend.repository.PlayerRepository;
import com.corty.backend.repository.PlayerSportRepository;
import com.corty.backend.repository.UserRepository;
import com.corty.backend.services.BookingService;
import com.corty.backend.services.EmailService;
import com.corty.backend.services.NotificationService;
import com.corty.backend.services.StripeService;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService — lógica de creación y cancelación")
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    PlayerBookingRepository playerBookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    PlayerRepository playerRepository;
    @Mock
    CourtRepository courtRepository;
    @Mock
    BookingMapper bookingMapper;
    @Mock
    NotificationService notificationService;
    @Mock
    EmailService emailService;
    @Mock
    JoinRequestRepository joinRequestRepository;
    @Mock
    PlayerSportRepository playerSportRepository;
    @Mock
    StripeService stripeService;
    @Mock
    ClubBalanceEntryRepository clubBalanceEntryRepository;

    @InjectMocks
    BookingService bookingService;

    private User user;
    private Player player;
    private Court court;
    private Club club;
    private Sport sport;
    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder().idRole(1L).name("PLAYER").build();

        user = User.builder()
                .idUser(1L)
                .username("testuser")
                .email("test@example.com")
                .role(role)
                .enabled(true)
                .build();

        player = Player.builder()
                .idPlayer(1L)
                .name("Test")
                .surname("User")
                .karma(80)
                .user(user)
                .build();

        sport = Sport.builder().idSport(1L).name("Pádel").build();

        club = Club.builder().idClub(1L).name("Club Test").build();

        court = Court.builder()
                .idCourt(1L)
                .name("Pista 1")
                .pricePerHour(BigDecimal.valueOf(20))
                .club(club)
                .sport(sport)
                .build();
    }

    // ── createBooking ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-B10: Reserva CASH → estado CONFIRMED, notificación y email enviados")
    void createBooking_cash_confirmed_sends_notification_and_email() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCourtId(1L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setBookingType(BookingType.PRIVATE);
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setSplitPayment(false);

        Booking savedBooking = Booking.builder()
                .idBooking(42L)
                .court(court)
                .owner(user)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .bookingStatus(BookingStatus.CONFIRMED)
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.valueOf(20))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(playerRepository.findByUser_IdUser(1L)).thenReturn(Optional.of(player));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(bookingRepository.existsOverlappingBooking(anyLong(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any())).thenReturn(savedBooking);
        when(playerSportRepository.findByPlayer_IdPlayerAndSport_IdSport(anyLong(), anyLong()))
                .thenReturn(Optional.of(new PlayerSport()));

        BookingCreateResponse response = bookingService.createBooking(request, "testuser");

        assertThat(response.getBookingId()).isEqualTo(42L);
        verify(notificationService).send(eq(1L), eq(NotificationType.BOOKING_CONFIRMED), any(), any(), eq(42L));
        verify(emailService).sendBookingConfirmed(eq("test@example.com"), eq("testuser"),
                eq("Pista 1"), eq("Club Test"), any(), any(), any());
    }

    @Test
    @DisplayName("TC-B11: Reserva ONLINE → estado PENDING_PAYMENT, sin notificación ni email")
    void createBooking_online_pending_payment_no_notification() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCourtId(1L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setBookingType(BookingType.PRIVATE);
        request.setPaymentMethod(PaymentMethod.ONLINE);
        request.setSplitPayment(false);

        Booking savedBooking = Booking.builder()
                .idBooking(43L)
                .court(court)
                .owner(user)
                .bookingStatus(BookingStatus.PENDING_PAYMENT)
                .paymentMethod(PaymentMethod.ONLINE)
                .totalPrice(BigDecimal.valueOf(20))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(playerRepository.findByUser_IdUser(1L)).thenReturn(Optional.of(player));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(bookingRepository.existsOverlappingBooking(anyLong(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any())).thenReturn(savedBooking);
        when(playerSportRepository.findByPlayer_IdPlayerAndSport_IdSport(anyLong(), anyLong()))
                .thenReturn(Optional.of(new PlayerSport()));

        bookingService.createBooking(request, "testuser");

        verifyNoInteractions(notificationService);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("TC-B12: Karma < 15 → no puede crear reserva pública")
    void createBooking_low_karma_cannot_create_public() {
        player.setKarma(10);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setCourtId(1L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setBookingType(BookingType.PUBLIC);
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setSplitPayment(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(playerRepository.findByUser_IdUser(1L)).thenReturn(Optional.of(player));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));

        assertThatThrownBy(() -> bookingService.createBooking(request, "testuser"))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("karma");
    }

    @Test
    @DisplayName("TC-B13: Horario solapado → lanza BusinessLogicException")
    void createBooking_overlapping_throws() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCourtId(1L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setBookingType(BookingType.PRIVATE);
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setSplitPayment(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(playerRepository.findByUser_IdUser(1L)).thenReturn(Optional.of(player));
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(bookingRepository.existsOverlappingBooking(anyLong(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(request, "testuser"))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("horario");
    }

    // ── cancelBooking ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-B14: Owner cancela con >24h → FREE, 0 karma, notifica participantes")
    void cancelBooking_free_window_no_karma_deducted() {
        Booking booking = buildConfirmedBooking(48);

        Player participant = Player.builder()
                .idPlayer(2L)
                .name("Otro")
                .surname("Jugador")
                .user(User.builder().idUser(2L).email("otro@test.com").username("otro").build())
                .build();
        PlayerBooking pb = PlayerBooking.builder()
                .player(participant)
                .booking(booking)
                .build();
        booking.setParticipants(List.of(pb));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(playerRepository.findByUser_IdUser(1L)).thenReturn(Optional.of(player));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        CancellationResponse response = bookingService.cancelBooking(10L, "testuser");

        assertThat(response.getKarmaDeducted()).isZero();
        verify(notificationService).send(eq(2L), eq(NotificationType.BOOKING_CANCELLED), any(), any(), any());
        verify(emailService).sendBookingCancelled(eq("otro@test.com"), eq("Otro"), any(), any(), any(), any(), any());
        verify(playerRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-B15: No owner intenta cancelar → AccessDeniedException")
    void cancelBooking_non_owner_throws_access_denied() {
        User otherUser = User.builder().idUser(99L).username("otro").build();
        Booking booking = buildConfirmedBooking(48);
        booking.setOwner(otherUser);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(playerRepository.findByUser_IdUser(1L)).thenReturn(Optional.of(player));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(10L, "testuser"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("TC-B16: Cancelar reserva ya cancelada → BusinessLogicException")
    void cancelBooking_already_cancelled_throws() {
        Booking booking = buildConfirmedBooking(48);
        booking.setBookingStatus(BookingStatus.CANCELLED);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(playerRepository.findByUser_IdUser(1L)).thenReturn(Optional.of(player));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(10L, "testuser"))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("cancelada");
    }

    // ── leaveBooking ──────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-B17: Owner intenta abandonar → BusinessLogicException")
    void leaveBooking_owner_throws() {
        Booking booking = buildConfirmedBooking(48);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(playerRepository.findByUser_IdUser(1L)).thenReturn(Optional.of(player));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.leaveBooking(10L, "testuser"))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("propietario");
    }

    @Test
    @DisplayName("TC-B18: Participante abandona con <2h → -15 karma, NO_REFUND")
    void leaveBooking_no_refund_window_15_karma_penalty() {
        User ownerUser = User.builder().idUser(99L).username("owner").build();
        Booking booking = Booking.builder()
                .idBooking(10L)
                .court(court)
                .owner(ownerUser)
                .date(LocalDate.now())
                .startTime(LocalTime.now().plusMinutes(30))
                .endTime(LocalTime.now().plusMinutes(90))
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingType(BookingType.PUBLIC)
                .paymentMethod(PaymentMethod.CASH)
                .splitPayment(true)
                .totalPrice(BigDecimal.valueOf(20))
                .participants(List.of())
                .build();

        PlayerBooking pb = PlayerBooking.builder()
                .player(player)
                .booking(booking)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(playerRepository.findByUser_IdUser(1L)).thenReturn(Optional.of(player));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(playerBookingRepository.findByBookingIdAndPlayerId(10L, 1L)).thenReturn(Optional.of(pb));
        when(playerRepository.save(any())).thenReturn(player);

        CancellationResponse response = bookingService.leaveBooking(10L, "testuser");

        assertThat(response.getKarmaDeducted()).isEqualTo(15);
        assertThat(player.getKarma()).isEqualTo(65); // 80 - 15
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Booking buildConfirmedBooking(long hoursFromNow) {
        return Booking.builder()
                .idBooking(10L)
                .court(court)
                .owner(user)
                .date(LocalDate.now().plusDays(hoursFromNow / 24 + 1))
                .startTime(LocalTime.now().plusHours(hoursFromNow % 24))
                .endTime(LocalTime.now().plusHours((hoursFromNow % 24) + 1))
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingType(BookingType.PRIVATE)
                .paymentMethod(PaymentMethod.CASH)
                .splitPayment(true)
                .totalPrice(BigDecimal.valueOf(20))
                .participants(List.of())
                .build();
    }
}
