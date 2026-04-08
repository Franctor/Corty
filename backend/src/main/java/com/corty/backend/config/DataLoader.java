package com.corty.backend.config;

import com.corty.backend.model.*;
import com.corty.backend.model.enums.*;
import com.corty.backend.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RegionRepository regionRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CityRepository cityRepository;
    private final RoleRepository roleRepository;
    private final PlayerRepository playerRepository;
    private final SportRepository sportRepository;
    private final ClubRepository clubRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;

    @Override
    public void run(String... args) throws Exception {
        if (regionRepository.count() == 0) {
            loadLocationData();
        }
        if (userRepository.count() == 0) {
            loadDemoData();
        }
    }

    // -------------------------------------------------------------------------
    // Demo data
    // -------------------------------------------------------------------------

    private void loadDemoData() {
        Role playerRole = roleRepository.findByName("PLAYER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("PLAYER").build()));

        City madrid = cityRepository.findByCode("28079")
                .orElse(cityRepository.findAll().get(0));

        // --- Deportes ---
        Sport futbol = sportRepository.save(Sport.builder()
                .name("Fútbol").playersPerTeam(7).playersPerMatch(14)
                .iconUrl("/assets/sports/futbol.svg").color("#58CC02")
                .isTeamSport(true).defaultDurationMins(90).build());

        Sport padel = sportRepository.save(Sport.builder()
                .name("Pádel").playersPerTeam(2).playersPerMatch(4)
                .iconUrl("/assets/sports/padel.svg").color("#1CB0F6")
                .isTeamSport(true).defaultDurationMins(90).build());

        Sport tenis = sportRepository.save(Sport.builder()
                .name("Tenis").playersPerTeam(1).playersPerMatch(2)
                .iconUrl("/assets/sports/tenis.svg").color("#FF9600")
                .isTeamSport(false).defaultDurationMins(60).build());

        Sport basket = sportRepository.save(Sport.builder()
                .name("Baloncesto").playersPerTeam(5).playersPerMatch(10)
                .iconUrl("/assets/sports/basket.svg").color("#FF4B4B")
                .isTeamSport(true).defaultDurationMins(60).build());

        // --- Usuarios y jugadores ---
        User user1 = userRepository.save(User.builder()
                .username("franco").email("franco@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());

        Player player1 = playerRepository.save(Player.builder()
                .name("Franco").surname("García").phone("600000001")
                .gender(Gender.MALE).birthDate(LocalDate.of(2000, 5, 15))
                .biography("Jugador habitual de pádel y fútbol.")
                .city(madrid).user(user1).build());

        User user2 = userRepository.save(User.builder()
                .username("ana").email("ana@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());

        Player player2 = playerRepository.save(Player.builder()
                .name("Ana").surname("Martínez").phone("600000002")
                .gender(Gender.FEMALE).birthDate(LocalDate.of(1998, 3, 22))
                .city(madrid).user(user2).build());

        User user3 = userRepository.save(User.builder()
                .username("carlos").email("carlos@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());

        Player player3 = playerRepository.save(Player.builder()
                .name("Carlos").surname("López").phone("600000003")
                .gender(Gender.MALE).birthDate(LocalDate.of(1995, 11, 8))
                .city(madrid).user(user3).build());

        // --- Clubs ---
        Club clubElite = clubRepository.save(Club.builder()
                .name("Club Deportivo Elite")
                .description("Club deportivo en el centro de Madrid con instalaciones de primer nivel.")
                .phone("910000001").contactEmail("info@cdelite.es")
                .address("Calle Gran Vía 42, Madrid").nif("B12345678")
                .geoLat(new BigDecimal("40.41650")).geoLong(new BigDecimal("-3.70256"))
                .city(madrid).build());

        Club clubNorte = clubRepository.save(Club.builder()
                .name("Pistas Norte")
                .description("Complejo deportivo al norte de Madrid.")
                .phone("910000002").contactEmail("info@pistasnorte.es")
                .address("Avenida de la Paz 10, Madrid").nif("B87654321")
                .geoLat(new BigDecimal("40.47200")).geoLong(new BigDecimal("-3.68900"))
                .city(madrid).build());

        // --- Pistas ---
        Court pistaPadel = courtRepository.save(Court.builder()
                .name("Pista Pádel 1").pricePerHour(new BigDecimal("24.00"))
                .isCovered(true).hasLighting(true).club(clubElite).sport(padel).build());

        Court pistaFutbol = courtRepository.save(Court.builder()
                .name("Campo Fútbol 7").pricePerHour(new BigDecimal("60.00"))
                .isCovered(false).hasLighting(true).club(clubElite).sport(futbol).build());

        Court pistaTenis = courtRepository.save(Court.builder()
                .name("Pista Tenis Central").pricePerHour(new BigDecimal("18.00"))
                .isCovered(false).hasLighting(false).club(clubNorte).sport(tenis).build());

        courtRepository.save(Court.builder()
                .name("Pista Basket Indoor").pricePerHour(new BigDecimal("30.00"))
                .isCovered(true).hasLighting(true).club(clubNorte).sport(basket).build());

        // ── RESERVAS COMPLETADAS con resultado y ganadores ──────────────────

        // Pádel hace 3 días: franco(A) gana, ana(B) pierde — con resultado
        Booking b1 = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaPadel)
                .date(LocalDate.now().minusDays(3)).startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(19, 30))
                .bookingStatus(BookingStatus.COMPLETED).bookingType(BookingType.PUBLIC)
                .result("6-3").totalPrice(new BigDecimal("24.00"))
                .paymentMethod(PaymentMethod.CASH).fullyPaid(true).build());
        saveParticipantWinner(b1, player1, Team.A, true);
        saveParticipantWinner(b1, player2, Team.B, false);

        // Fútbol hace 7 días: sin ganador registrado
        Booking b2 = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaFutbol)
                .date(LocalDate.now().minusDays(7)).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 30))
                .bookingStatus(BookingStatus.COMPLETED).bookingType(BookingType.PUBLIC)
                .totalPrice(new BigDecimal("60.00")).paymentMethod(PaymentMethod.CASH).fullyPaid(true).build());
        saveParticipant(b2, player1, Team.A);
        saveParticipant(b2, player3, Team.B);

        // Tenis hace 14 días: privada, solo franco
        Booking b3 = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaTenis)
                .date(LocalDate.now().minusDays(14)).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0))
                .bookingStatus(BookingStatus.COMPLETED).bookingType(BookingType.PRIVATE)
                .result("6-4, 6-2").totalPrice(new BigDecimal("18.00"))
                .paymentMethod(PaymentMethod.CASH).fullyPaid(true).build());
        saveParticipantWinner(b3, player1, Team.A, true);
        saveParticipantWinner(b3, player2, Team.B, false);

        // ── RESERVA CANCELADA ────────────────────────────────────────────────

        // Franco canceló esta reserva de pádel (para probar vista CANCELLED)
        Booking cancelada = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaPadel)
                .date(LocalDate.now().minusDays(1)).startTime(LocalTime.of(17, 0)).endTime(LocalTime.of(18, 30))
                .bookingStatus(BookingStatus.CANCELLED).bookingType(BookingType.PRIVATE)
                .totalPrice(new BigDecimal("24.00")).paymentMethod(PaymentMethod.CASH).build());
        saveParticipant(cancelada, player1, Team.A);
        saveParticipant(cancelada, player2, Team.B);

        // ── RESERVAS FUTURAS ─────────────────────────────────────────────────

        // Franco es OWNER — cancelación gratuita (>24h), con ana y carlos
        Booking proximaOwner = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaPadel)
                .date(LocalDate.now().plusDays(2)).startTime(LocalTime.of(19, 0)).endTime(LocalTime.of(20, 30))
                .bookingStatus(BookingStatus.CONFIRMED).bookingType(BookingType.PUBLIC)
                .totalPrice(new BigDecimal("24.00")).paymentMethod(PaymentMethod.CASH).build());
        saveParticipant(proximaOwner, player1, Team.A);
        saveParticipant(proximaOwner, player2, Team.B);
        saveParticipant(proximaOwner, player3, Team.A);

        // Franco es PARTICIPANTE (carlos es owner) — para probar "Abandonar reserva"
        Booking proximaParticipante = bookingRepository.save(Booking.builder()
                .owner(user3).court(pistaFutbol)
                .date(LocalDate.now().plusDays(5)).startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(12, 30))
                .bookingStatus(BookingStatus.CONFIRMED).bookingType(BookingType.PUBLIC)
                .totalPrice(new BigDecimal("60.00")).paymentMethod(PaymentMethod.CASH).build());
        saveParticipant(proximaParticipante, player3, Team.A);
        saveParticipant(proximaParticipante, player1, Team.B);
        saveParticipant(proximaParticipante, player2, Team.B);

        // Franco es OWNER que pagó el total (splitPayment=false) — participantes sin reembolso al abandonar
        Booking proximaOwnerPaga = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaPadel)
                .date(LocalDate.now().plusDays(3)).startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 30))
                .bookingStatus(BookingStatus.CONFIRMED).bookingType(BookingType.PUBLIC)
                .splitPayment(false).totalPrice(new BigDecimal("24.00")).paymentMethod(PaymentMethod.CASH).build());
        saveParticipant(proximaOwnerPaga, player1, Team.A);
        saveParticipant(proximaOwnerPaga, player2, Team.B);
        saveParticipant(proximaOwnerPaga, player3, Team.A);

        // Reserva futura con penalización parcial (~12h) — para probar ventana 2-24h
        // Usamos +0 días + hora cercana para simular que está a pocas horas
        Booking proximaParcial = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaTenis)
                .date(LocalDate.now().plusDays(1)).startTime(LocalTime.of(LocalTime.now().getHour(), 0)).endTime(LocalTime.of(LocalTime.now().getHour() + 1, 0))
                .bookingStatus(BookingStatus.PENDING).bookingType(BookingType.PRIVATE)
                .totalPrice(new BigDecimal("18.00")).paymentMethod(PaymentMethod.CASH).build());
        saveParticipant(proximaParcial, player1, Team.A);
        saveParticipant(proximaParcial, player2, Team.B);

        System.out.println("✅ Datos de demo cargados");
        System.out.println("   → franco / Test1234!  (owner en proxima pádel y participante en fútbol de carlos)");
        System.out.println("   → ana    / Test1234!  (participante en varias reservas)");
        System.out.println("   → carlos / Test1234!  (owner en reserva de fútbol)");
    }

    private void saveParticipant(Booking booking, Player player, Team team) {
        playerBookingRepository.save(PlayerBooking.builder()
                .booking(booking).player(player).team(team)
                .splitPrice(booking.getTotalPrice().divide(BigDecimal.TWO))
                .isConfirmed(true).hasPaid(true).build());
    }

    private void saveParticipantWinner(Booking booking, Player player, Team team, boolean isWinner) {
        playerBookingRepository.save(PlayerBooking.builder()
                .booking(booking).player(player).team(team)
                .splitPrice(booking.getTotalPrice().divide(BigDecimal.TWO))
                .isConfirmed(true).hasPaid(true).isWinner(isWinner).build());
    }

    // -------------------------------------------------------------------------
    // Location data from JSON
    // -------------------------------------------------------------------------

    private void loadLocationData() throws IOException {
        InputStream inputStream = new ClassPathResource("data/regions.json").getInputStream();
        List<Map<String, Object>> data = objectMapper.readValue(inputStream, new TypeReference<>() {});
        for (Map<String, Object> cMap : data) {
            Region region = Region.builder()
                    .code(String.valueOf(cMap.get("code")))
                    .label((String) cMap.get("label"))
                    .provinces(new ArrayList<>())
                    .build();

            List<Map<String, Object>> pList = (List<Map<String, Object>>) cMap.get("provinces");
            for (Map<String, Object> pMap : pList) {
                Province province = Province.builder()
                        .code(String.valueOf(pMap.get("code")))
                        .label((String) pMap.get("label"))
                        .region(region).cities(new ArrayList<>())
                        .build();

                List<Map<String, Object>> tList = (List<Map<String, Object>>) pMap.get("towns");
                for (Map<String, Object> tMap : tList) {
                    City city = City.builder()
                            .code(String.valueOf(tMap.get("code")))
                            .label((String) tMap.get("label"))
                            .province(province).build();
                    province.getCities().add(city);
                }
                region.getProvinces().add(province);
            }
            regionRepository.save(region);
        }
    }
}
