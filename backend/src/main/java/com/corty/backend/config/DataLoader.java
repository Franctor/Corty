package com.corty.backend.config;

import com.corty.backend.model.*;
import com.corty.backend.model.enums.*;
import com.corty.backend.model.PlayerSport;
import com.corty.backend.repository.*;
import com.corty.backend.repository.ClubBalanceEntryRepository;
import com.corty.backend.repository.OrganizationRepository;
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
    private final AuthorityRepository authorityRepository;
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SportRepository sportRepository;
    private final ClubRepository clubRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final PlayerBookingRepository playerBookingRepository;
    private final HoraryClubRepository horaryClubRepository;
    private final SurfaceRepository surfaceRepository;
    private final PlayerSportRepository playerSportRepository;
    private final ClubBalanceEntryRepository clubBalanceEntryRepository;

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
        Authority forceDelete = authorityRepository.findByName("FORCE_DELETE")
                .orElseGet(() -> authorityRepository
                .save(Authority.builder().name("FORCE_DELETE").build()));
        Authority manageRoles = authorityRepository.findByName("MANAGE_ROLES")
                .orElseGet(() -> authorityRepository
                .save(Authority.builder().name("MANAGE_ROLES").build()));
        Authority managePricing = authorityRepository.findByName("MANAGE_PRICING")
                .orElseGet(() -> authorityRepository
                .save(Authority.builder().name("MANAGE_PRICING").build()));
        Authority viewReports = authorityRepository.findByName("VIEW_REPORTS")
                .orElseGet(() -> authorityRepository
                .save(Authority.builder().name("VIEW_REPORTS").build()));
        Authority impersonateUser = authorityRepository.findByName("IMPERSONATE_USER")
                .orElseGet(() -> authorityRepository
                .save(Authority.builder().name("IMPERSONATE_USER").build()));
        Authority manageStaff = authorityRepository.findByName("MANAGE_STAFF")
                .orElseGet(() -> authorityRepository
                .save(Authority.builder().name("MANAGE_STAFF").build()));
        Authority managePromotions = authorityRepository.findByName("MANAGE_PROMOTIONS")
                .orElseGet(() -> authorityRepository
                .save(Authority.builder().name("MANAGE_PROMOTIONS").build()));
        Authority verifiedPlayer = authorityRepository.findByName("VERIFIED_PLAYER")
                .orElseGet(() -> authorityRepository
                .save(Authority.builder().name("VERIFIED_PLAYER").build()));

        Role playerRole = roleRepository.findByName("PLAYER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("PLAYER").build()));
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));
        Role superadminRole = roleRepository.findByName("SUPERADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("SUPERADMIN").build()));
        Role orgRole = roleRepository.findByName("ORGANIZATION")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ORGANIZATION").build()));

        City madrid = cityRepository.findByCode("28079").orElse(cityRepository.findAll().get(0));
        City barcelona = cityRepository.findByCode("08019").orElse(madrid);
        City sevilla = cityRepository.findByCode("41091").orElse(madrid);
        City valencia = cityRepository.findByCode("46250").orElse(madrid);

        // --- Admins y Organization de prueba ---
        // admin: ADMIN sin FORCE_DELETE
        userRepository.save(User.builder()
                .username("admin").email("admin@corty.app")
                .password(passwordEncoder.encode("Admin1234!"))
                .role(adminRole).enabled(true).creationDate(LocalDateTime.now()).build());

        // superadmin: rol SUPERADMIN + todas las authorities
        userRepository.save(User.builder()
                .username("superadmin").email("superadmin@corty.app")
                .password(passwordEncoder.encode("Super1234!"))
                .role(superadminRole)
                .extraAuthorities(new java.util.HashSet<>(java.util.List.of(
                        forceDelete, manageRoles, managePricing, viewReports, impersonateUser)))
                .enabled(true).creationDate(LocalDateTime.now()).build());

        User userOrg1 = userRepository.save(User.builder()
                .username("org1").email("org1@corty.app")
                .password(passwordEncoder.encode("Org12345!"))
                .role(orgRole).enabled(true).creationDate(LocalDateTime.now()).build());

        Organization org1 = organizationRepository.save(Organization.builder()
                .businessName("Corty Deportes S.L.")
                .cif("B12345678")
                .fiscalCity(madrid)
                .user(userOrg1)
                .build());

        // --- Deportes ---
        Sport futbol = sportRepository.save(Sport.builder()
                .name("Fútbol").playersPerTeam(7).playersPerMatch(14)
                .iconUrl("/sport-icons/futbol.svg").color("#58CC02")
                .teamSport(true).build());

        Sport padel = sportRepository.save(Sport.builder()
                .name("Pádel").playersPerTeam(2).playersPerMatch(4)
                .iconUrl("/sport-icons/padel.svg").color("#1CB0F6")
                .teamSport(true).build());

        Sport tenis = sportRepository.save(Sport.builder()
                .name("Tenis").playersPerTeam(1).playersPerMatch(2)
                .iconUrl("/sport-icons/tenis.svg").color("#FF9600")
                .teamSport(false).build());

        Sport basket = sportRepository.save(Sport.builder()
                .name("Baloncesto").playersPerTeam(5).playersPerMatch(10)
                .iconUrl("/sport-icons/basket.svg").color("#FF4B4B")
                .teamSport(true).build());

        // --- Usuarios y jugadores (12 jugadores para superar paginación) ---
        User user1 = userRepository.save(User.builder()
                .username("fran").email("fran@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player1 = playerRepository.save(Player.builder()
                .name("Fran").surname("García").phone("600000001")
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

        User user4 = userRepository.save(User.builder()
                .username("lucia").email("lucia@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player4 = playerRepository.save(Player.builder()
                .name("Lucía").surname("Fernández").phone("600000004")
                .gender(Gender.FEMALE).birthDate(LocalDate.of(2001, 7, 30))
                .city(barcelona).user(user4).build());

        User user5 = userRepository.save(User.builder()
                .username("miguel").email("miguel@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player5 = playerRepository.save(Player.builder()
                .name("Miguel").surname("Sánchez").phone("600000005")
                .gender(Gender.MALE).birthDate(LocalDate.of(1993, 2, 14))
                .city(sevilla).user(user5).build());

        User user6 = userRepository.save(User.builder()
                .username("sofia").email("sofia@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player6 = playerRepository.save(Player.builder()
                .name("Sofía").surname("Romero").phone("600000006")
                .gender(Gender.FEMALE).birthDate(LocalDate.of(1999, 9, 5))
                .city(valencia).user(user6).build());

        User user7 = userRepository.save(User.builder()
                .username("pablo").email("pablo@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player7 = playerRepository.save(Player.builder()
                .name("Pablo").surname("Jiménez").phone("600000007")
                .gender(Gender.MALE).birthDate(LocalDate.of(1997, 4, 20))
                .city(madrid).user(user7).build());

        User user8 = userRepository.save(User.builder()
                .username("marta").email("marta@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player8 = playerRepository.save(Player.builder()
                .name("Marta").surname("Díaz").phone("600000008")
                .gender(Gender.FEMALE).birthDate(LocalDate.of(2002, 12, 3))
                .city(madrid).user(user8).build());

        User user9 = userRepository.save(User.builder()
                .username("jorge").email("jorge@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player9 = playerRepository.save(Player.builder()
                .name("Jorge").surname("Moreno").phone("600000009")
                .gender(Gender.MALE).birthDate(LocalDate.of(1990, 6, 18))
                .city(barcelona).user(user9).build());

        User user10 = userRepository.save(User.builder()
                .username("elena").email("elena@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player10 = playerRepository.save(Player.builder()
                .name("Elena").surname("Ruiz").phone("600000010")
                .gender(Gender.FEMALE).birthDate(LocalDate.of(1996, 1, 25))
                .city(sevilla).user(user10).build());

        User user11 = userRepository.save(User.builder()
                .username("david").email("david@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player11 = playerRepository.save(Player.builder()
                .name("David").surname("Navarro").phone("600000011")
                .gender(Gender.MALE).birthDate(LocalDate.of(1994, 8, 11))
                .city(valencia).user(user11).build());

        User user12 = userRepository.save(User.builder()
                .username("irene").email("irene@corty.app")
                .password(passwordEncoder.encode("Test1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player player12 = playerRepository.save(Player.builder()
                .name("Irene").surname("Torres").phone("600000012")
                .gender(Gender.FEMALE).birthDate(LocalDate.of(2003, 3, 7))
                .city(madrid).user(user12).build());

        // Usuario nuevo sin actividad (para probar estado onboarding)
        User userNuevo = userRepository.save(User.builder()
                .username("nuevo").email("nuevo@corty.app")
                .password(passwordEncoder.encode("Nuevo1234!"))
                .role(playerRole).enabled(true).creationDate(LocalDateTime.now()).build());
        Player playerNuevo = playerRepository.save(Player.builder()
                .name("Jugador").surname("Nuevo").phone("600000099")
                .gender(Gender.MALE).birthDate(LocalDate.of(2000, 1, 1))
                .karma(100)
                .city(madrid).user(userNuevo).build());

        // --- Superficies ---
        Surface cesped = surfaceRepository.save(Surface.builder()
                .name("Césped artificial").description("Hierba artificial de alta densidad")
                .iconUrl("/surface-icons/1.svg").build());

        Surface tierraBatida = surfaceRepository.save(Surface.builder()
                .name("Tierra batida").description("Superficie de polvo de ladrillo")
                .iconUrl("/surface-icons/2.svg").build());

        Surface madera = surfaceRepository.save(Surface.builder()
                .name("Madera").description("Parquet de madera para pistas interiores")
                .iconUrl("/surface-icons/3.svg").build());

        // --- Clubs (6 clubs para superar paginación) ---
        Club clubElite = clubRepository.save(Club.builder()
                .name("Club Deportivo Elite")
                .description("Club deportivo en el centro de Madrid con instalaciones de primer nivel.")
                .phone("910000001").contactEmail("info@cdelite.es")
                .address("Calle Gran Vía 42, Madrid").nif("C12345678")
                .geoLat(new BigDecimal("40.41650")).geoLong(new BigDecimal("-3.70256"))
                .logoUrl("/club-logos/1.svg")
                .city(madrid).organization(org1).build());

        Club clubNorte = clubRepository.save(Club.builder()
                .name("Pistas Norte")
                .description("Complejo deportivo al norte de Madrid.")
                .phone("910000002").contactEmail("info@pistasnorte.es")
                .address("Avenida de la Paz 10, Madrid").nif("C87654321")
                .geoLat(new BigDecimal("40.47200")).geoLong(new BigDecimal("-3.68900"))
                .logoUrl("/club-logos/2.svg")
                .city(madrid).organization(org1).build());

        Club clubBcn = clubRepository.save(Club.builder()
                .name("Sport Center Barcelona")
                .description("Centro deportivo moderno en el corazón de Barcelona.")
                .phone("930000001").contactEmail("info@sportbcn.es")
                .address("Passeig de Gràcia 88, Barcelona").nif("C11223344")
                .geoLat(new BigDecimal("41.39600")).geoLong(new BigDecimal("2.16000"))
                .logoUrl("/club-logos/3.svg")
                .city(barcelona).organization(org1).build());

        Club clubSevilla = clubRepository.save(Club.builder()
                .name("Andalucía Courts")
                .description("Pistas de pádel y tenis en Sevilla.")
                .phone("950000001").contactEmail("info@andaluciacourts.es")
                .address("Avenida de la Constitución 5, Sevilla").nif("C44556677")
                .geoLat(new BigDecimal("37.38800")).geoLong(new BigDecimal("-5.99200"))
                .logoUrl("/club-logos/4.svg")
                .city(sevilla).organization(org1).build());

        Club clubValencia = clubRepository.save(Club.builder()
                .name("Valencia Sport Club")
                .description("Instalaciones deportivas de alto rendimiento en Valencia.")
                .phone("960000001").contactEmail("info@valenciasport.es")
                .address("Calle Colón 30, Valencia").nif("C55667788")
                .geoLat(new BigDecimal("39.46900")).geoLong(new BigDecimal("-0.37600"))
                .logoUrl("/club-logos/5.svg")
                .city(valencia).organization(org1).build());

        Club clubSur = clubRepository.save(Club.builder()
                .name("Pistas del Sur")
                .description("Club deportivo en el sur de Madrid.")
                .phone("910000006").contactEmail("info@pistasdelsur.es")
                .address("Calle Leganés 15, Madrid").nif("C66778899")
                .geoLat(new BigDecimal("40.38500")).geoLong(new BigDecimal("-3.73100"))
                .logoUrl("/club-logos/6.svg")
                .city(madrid).organization(org1).build());

        // Horarios de clubes: lun-vie 08:00-22:00, sáb 09:00-21:00, dom 10:00-20:00
        for (Club club : List.of(clubElite, clubNorte, clubBcn, clubSevilla, clubValencia, clubSur)) {
            createClubSchedules(club);
        }

        // --- Pistas (14 pistas para superar paginación) ---
        Court pistaPadel = courtRepository.save(Court.builder()
                .name("Pista Pádel 1").pricePerHour(new BigDecimal("24.00"))
                .covered(true).lighting(true).imageUrl("/court-images/padel.jpeg")
                .club(clubElite).sport(padel).surface(cesped).build());

        Court pistaFutbol = courtRepository.save(Court.builder()
                .name("Campo Fútbol 7").pricePerHour(new BigDecimal("60.00"))
                .covered(false).lighting(true).imageUrl("/court-images/futbol.jpg")
                .club(clubElite).sport(futbol).surface(cesped).build());

        Court pistaTenis = courtRepository.save(Court.builder()
                .name("Pista Tenis Central").pricePerHour(new BigDecimal("18.00"))
                .covered(false).lighting(false).imageUrl("/court-images/tenis.jpg")
                .club(clubNorte).sport(tenis).surface(tierraBatida).build());

        courtRepository.save(Court.builder()
                .name("Pista Basket Indoor").pricePerHour(new BigDecimal("30.00"))
                .covered(true).lighting(true).imageUrl("/court-images/baloncesto.Jpg")
                .club(clubNorte).sport(basket).surface(madera).build());

        courtRepository.save(Court.builder()
                .name("Pista Pádel 2").pricePerHour(new BigDecimal("26.00"))
                .covered(false).lighting(true).imageUrl("/court-images/padel.jpeg")
                .club(clubElite).sport(padel).surface(cesped).build());

        courtRepository.save(Court.builder()
                .name("Pista Pádel BCN").pricePerHour(new BigDecimal("28.00"))
                .covered(true).lighting(true).imageUrl("/court-images/padel.jpeg")
                .club(clubBcn).sport(padel).surface(cesped).build());

        courtRepository.save(Court.builder()
                .name("Pista Tenis BCN").pricePerHour(new BigDecimal("20.00"))
                .covered(false).lighting(true).imageUrl("/court-images/tenis.jpg")
                .club(clubBcn).sport(tenis).surface(tierraBatida).build());

        courtRepository.save(Court.builder()
                .name("Pista Fútbol BCN").pricePerHour(new BigDecimal("55.00"))
                .covered(false).lighting(true).imageUrl("/court-images/futbol.jpg")
                .club(clubBcn).sport(futbol).surface(cesped).build());

        courtRepository.save(Court.builder()
                .name("Pista Pádel Sevilla 1").pricePerHour(new BigDecimal("22.00"))
                .covered(true).lighting(true).imageUrl("/court-images/padel.jpeg")
                .club(clubSevilla).sport(padel).surface(cesped).build());

        courtRepository.save(Court.builder()
                .name("Pista Pádel Sevilla 2").pricePerHour(new BigDecimal("22.00"))
                .covered(false).lighting(false).imageUrl("/court-images/padel.jpeg")
                .club(clubSevilla).sport(padel).surface(tierraBatida).build());

        courtRepository.save(Court.builder()
                .name("Pista Tenis Valencia").pricePerHour(new BigDecimal("19.00"))
                .covered(false).lighting(true).imageUrl("/court-images/tenis.jpg")
                .club(clubValencia).sport(tenis).surface(tierraBatida).build());

        courtRepository.save(Court.builder()
                .name("Pista Basket Valencia").pricePerHour(new BigDecimal("35.00"))
                .covered(true).lighting(true).imageUrl("/court-images/baloncesto.Jpg")
                .club(clubValencia).sport(basket).surface(madera).build());

        courtRepository.save(Court.builder()
                .name("Campo Fútbol Sur").pricePerHour(new BigDecimal("50.00"))
                .covered(false).lighting(true).imageUrl("/court-images/futbol.jpg")
                .club(clubSur).sport(futbol).surface(cesped).build());

        courtRepository.save(Court.builder()
                .name("Pista Pádel Sur").pricePerHour(new BigDecimal("21.00"))
                .covered(false).lighting(false).imageUrl("/court-images/padel.jpeg")
                .club(clubSur).sport(padel).surface(cesped).build());

        // ── PlayerSport ──────────────────────────────────────────────────────
        playerSportRepository.save(PlayerSport.builder().player(player1).sport(padel).level(7.2)
                .playedMatches(12).wins(8).losses(4).build());
        playerSportRepository.save(PlayerSport.builder().player(player1).sport(tenis).level(5.8)
                .playedMatches(6).wins(4).losses(2).build());
        playerSportRepository.save(PlayerSport.builder().player(player1).sport(futbol).level(4.1)
                .playedMatches(3).wins(1).losses(2).build());

        // ── RESERVAS COMPLETADAS — splitPayment=true, paidAmount real ────────
        // Enero (hace ~4 meses)
        Booking c01a = completedSplit(user1, pistaPadel, LocalDate.now().minusMonths(4).withDayOfMonth(5),
                "6-3", new BigDecimal("24.00"));
        saveParticipantPaid(c01a, player1, Team.A, true);
        saveParticipantPaid(c01a, player2, Team.B, false);

        Booking c01b = completedSplit(user3, pistaFutbol, LocalDate.now().minusMonths(4).withDayOfMonth(15),
                null, new BigDecimal("60.00"));
        saveParticipantPaid(c01b, player3, Team.A, false);
        saveParticipantPaid(c01b, player4, Team.B, true);
        saveParticipantPaid(c01b, player5, Team.A, false);

        Booking c01c = completedSplit(user2, pistaPadel, LocalDate.now().minusMonths(4).withDayOfMonth(22),
                "6-4", new BigDecimal("24.00"));
        saveParticipantPaid(c01c, player2, Team.A, true);
        saveParticipantPaid(c01c, player6, Team.B, false);

        // Febrero (hace ~3 meses)
        Booking c02a = completedSplit(user5, pistaFutbol, LocalDate.now().minusMonths(3).withDayOfMonth(3),
                "3-1", new BigDecimal("60.00"));
        saveParticipantPaid(c02a, player5, Team.A, true);
        saveParticipantPaid(c02a, player7, Team.B, false);
        saveParticipantPaid(c02a, player8, Team.A, true);

        Booking c02b = completedSplit(user1, pistaPadel, LocalDate.now().minusMonths(3).withDayOfMonth(10),
                "7-5", new BigDecimal("24.00"));
        saveParticipantPaid(c02b, player1, Team.A, true);
        saveParticipantPaid(c02b, player3, Team.B, false);

        Booking c02c = completedSplit(user4, pistaTenis, LocalDate.now().minusMonths(3).withDayOfMonth(20),
                "6-2, 6-1", new BigDecimal("18.00"));
        saveParticipantPaid(c02c, player4, Team.A, true);
        saveParticipantPaid(c02c, player9, Team.B, false);

        // Marzo (hace ~2 meses)
        Booking c03a = completedSplit(user6, pistaFutbol, LocalDate.now().minusMonths(2).withDayOfMonth(2),
                "2-2", new BigDecimal("60.00"));
        saveParticipantPaid(c03a, player6, Team.A, false);
        saveParticipantPaid(c03a, player10, Team.B, false);
        saveParticipantPaid(c03a, player11, Team.A, false);

        Booking c03b = completedSplit(user2, pistaPadel, LocalDate.now().minusMonths(2).withDayOfMonth(12),
                "6-1, 6-0", new BigDecimal("24.00"));
        saveParticipantPaid(c03b, player2, Team.A, true);
        saveParticipantPaid(c03b, player12, Team.B, false);

        Booking c03c = completedSplit(user1, pistaFutbol, LocalDate.now().minusMonths(2).withDayOfMonth(25),
                "4-2", new BigDecimal("60.00"));
        saveParticipantPaid(c03c, player1, Team.A, true);
        saveParticipantPaid(c03c, player3, Team.B, false);
        saveParticipantPaid(c03c, player5, Team.A, true);

        // Abril (hace ~1 mes)
        Booking c04a = completedSplit(user7, pistaPadel, LocalDate.now().minusMonths(1).withDayOfMonth(4),
                "6-4", new BigDecimal("24.00"));
        saveParticipantPaid(c04a, player7, Team.A, true);
        saveParticipantPaid(c04a, player8, Team.B, false);

        Booking c04b = completedSplit(user9, pistaFutbol, LocalDate.now().minusMonths(1).withDayOfMonth(14),
                "1-3", new BigDecimal("60.00"));
        saveParticipantPaid(c04b, player9, Team.A, false);
        saveParticipantPaid(c04b, player10, Team.B, true);
        saveParticipantPaid(c04b, player11, Team.A, false);

        Booking c04c = completedSplit(user1, pistaPadel, LocalDate.now().minusMonths(1).withDayOfMonth(20),
                "7-6, 6-3", new BigDecimal("24.00"));
        saveParticipantPaid(c04c, player1, Team.A, true);
        saveParticipantPaid(c04c, player4, Team.B, false);

        Booking c04d = completedSplit(user12, pistaTenis, LocalDate.now().minusMonths(1).withDayOfMonth(28),
                "6-2, 6-4", new BigDecimal("18.00"));
        saveParticipantPaid(c04d, player12, Team.A, true);
        saveParticipantPaid(c04d, player2, Team.B, false);

        // Mayo (este mes)
        Booking c05a = completedSplit(user3, pistaPadel, LocalDate.now().minusDays(10), "6-3",
                new BigDecimal("24.00"));
        saveParticipantPaid(c05a, player3, Team.A, true);
        saveParticipantPaid(c05a, player6, Team.B, false);

        Booking c05b = completedSplit(user5, pistaFutbol, LocalDate.now().minusDays(6), "3-2",
                new BigDecimal("60.00"));
        saveParticipantPaid(c05b, player5, Team.A, true);
        saveParticipantPaid(c05b, player7, Team.B, false);
        saveParticipantPaid(c05b, player8, Team.A, true);

        Booking c05c = completedSplit(user1, pistaPadel, LocalDate.now().minusDays(3), "6-2, 6-1",
                new BigDecimal("24.00"));
        saveParticipantPaid(c05c, player1, Team.A, true);
        saveParticipantPaid(c05c, player2, Team.B, false);

        // ── RESERVA CANCELADA (sin pago — splitPayment=false para owner paga todo) ──
        Booking cancelada = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaPadel)
                .date(LocalDate.now().minusDays(1)).startTime(LocalTime.of(17, 0))
                .endTime(LocalTime.of(18, 30))
                .bookingStatus(BookingStatus.CANCELLED).bookingType(BookingType.PRIVATE)
                .splitPayment(false).totalPrice(new BigDecimal("24.00"))
                .paymentMethod(PaymentMethod.CASH).build());
        saveParticipant(cancelada, player1, Team.A);
        saveParticipant(cancelada, player2, Team.B);

        // ── RESERVAS FUTURAS ─────────────────────────────────────────────────
        Booking proximaOwner = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaPadel)
                .date(LocalDate.now().plusDays(2)).startTime(LocalTime.of(19, 0))
                .endTime(LocalTime.of(20, 30))
                .bookingStatus(BookingStatus.CONFIRMED).bookingType(BookingType.PUBLIC)
                .splitPayment(true).totalPrice(new BigDecimal("24.00"))
                .paymentMethod(PaymentMethod.ONLINE).build());
        saveParticipant(proximaOwner, player1, Team.A);
        saveParticipant(proximaOwner, player2, Team.B);
        saveParticipant(proximaOwner, player3, Team.A);

        Booking proximaParticipante = bookingRepository.save(Booking.builder()
                .owner(user3).court(pistaFutbol)
                .date(LocalDate.now().plusDays(5)).startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(12, 30))
                .bookingStatus(BookingStatus.CONFIRMED).bookingType(BookingType.PUBLIC)
                .splitPayment(true).totalPrice(new BigDecimal("60.00"))
                .paymentMethod(PaymentMethod.ONLINE).build());
        saveParticipant(proximaParticipante, player3, Team.A);
        saveParticipant(proximaParticipante, player1, Team.B);
        saveParticipant(proximaParticipante, player2, Team.B);

        Booking proximaOwnerPaga = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaPadel)
                .date(LocalDate.now().plusDays(3)).startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 30))
                .bookingStatus(BookingStatus.CONFIRMED).bookingType(BookingType.PUBLIC)
                .splitPayment(false).totalPrice(new BigDecimal("24.00"))
                .paymentMethod(PaymentMethod.ONLINE).build());
        saveParticipant(proximaOwnerPaga, player1, Team.A);
        saveParticipant(proximaOwnerPaga, player2, Team.B);
        saveParticipant(proximaOwnerPaga, player3, Team.A);

        Booking proximaParcial = bookingRepository.save(Booking.builder()
                .owner(user1).court(pistaTenis)
                .date(LocalDate.now().plusDays(1)).startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .bookingStatus(BookingStatus.CONFIRMED).bookingType(BookingType.PRIVATE)
                .splitPayment(true).totalPrice(new BigDecimal("18.00"))
                .paymentMethod(PaymentMethod.ONLINE).build());
        saveParticipant(proximaParcial, player1, Team.A);
        saveParticipant(proximaParcial, player2, Team.B);

        Booking b8 = bookingRepository.save(Booking.builder()
                .owner(user8).court(pistaFutbol)
                .date(LocalDate.now().plusDays(6)).startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 30))
                .bookingStatus(BookingStatus.CONFIRMED).bookingType(BookingType.PUBLIC)
                .splitPayment(true).totalPrice(new BigDecimal("60.00"))
                .paymentMethod(PaymentMethod.ONLINE).build());
        saveParticipant(b8, player8, Team.A);
        saveParticipant(b8, player9, Team.B);

        Booking b11 = bookingRepository.save(Booking.builder()
                .owner(user11).court(pistaFutbol)
                .date(LocalDate.now().plusDays(8)).startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .bookingStatus(BookingStatus.CONFIRMED).bookingType(BookingType.PUBLIC)
                .splitPayment(true).totalPrice(new BigDecimal("60.00"))
                .paymentMethod(PaymentMethod.ONLINE).build());
        saveParticipant(b11, player11, Team.A);
        saveParticipant(b11, player12, Team.B);

        // ── PENALIZACIONES — distribuidas en los últimos 5 meses ─────────────
        // Enero
        savePenalty(clubElite, c01a, new BigDecimal("12.00"), ClubBalanceReason.PARTICIPANT_LATE_CANCEL,
                "Ana canceló con 3h de antelación",
                LocalDateTime.now().minusMonths(4).withDayOfMonth(5));
        savePenalty(clubElite, c01b, new BigDecimal("30.00"), ClubBalanceReason.PARTICIPANT_LAST_MINUTE_CANCEL,
                "Carlos no se presentó", LocalDateTime.now().minusMonths(4).withDayOfMonth(16));

        // Febrero
        savePenalty(clubElite, c02a, new BigDecimal("30.00"), ClubBalanceReason.OWNER_LATE_CANCEL,
                "Owner canceló 4h antes", LocalDateTime.now().minusMonths(3).withDayOfMonth(3));
        savePenalty(clubElite, c02b, new BigDecimal("12.00"), ClubBalanceReason.PARTICIPANT_LATE_CANCEL,
                "Carlos canceló en ventana parcial",
                LocalDateTime.now().minusMonths(3).withDayOfMonth(11));

        // Marzo
        savePenalty(clubElite, c03a, new BigDecimal("60.00"), ClubBalanceReason.OWNER_LAST_MINUTE_CANCEL,
                "Owner no se presentó (<2h)", LocalDateTime.now().minusMonths(2).withDayOfMonth(2));
        savePenalty(clubElite, c03c, new BigDecimal("12.00"), ClubBalanceReason.PARTICIPANT_LATE_CANCEL,
                "Lucía canceló con 5h de antelación",
                LocalDateTime.now().minusMonths(2).withDayOfMonth(25));

        // Abril
        savePenalty(clubElite, c04a, new BigDecimal("12.00"), ClubBalanceReason.PARTICIPANT_LATE_CANCEL,
                "Marta canceló 3h antes", LocalDateTime.now().minusMonths(1).withDayOfMonth(4));
        savePenalty(clubElite, c04b, new BigDecimal("20.00"), ClubBalanceReason.PARTICIPANT_LAST_MINUTE_CANCEL,
                "Jorge no se presentó", LocalDateTime.now().minusMonths(1).withDayOfMonth(14));
        savePenalty(clubElite, c04c, new BigDecimal("60.00"), ClubBalanceReason.OWNER_LAST_MINUTE_CANCEL,
                "Owner no se presentó al partido",
                LocalDateTime.now().minusMonths(1).withDayOfMonth(21));

        // Mayo
        savePenalty(clubElite, c05a, new BigDecimal("12.00"), ClubBalanceReason.PARTICIPANT_LATE_CANCEL,
                "Sofia canceló 2h antes", LocalDateTime.now().minusDays(10));
        savePenalty(clubElite, c05c, new BigDecimal("12.00"), ClubBalanceReason.PARTICIPANT_LATE_CANCEL,
                "Ana canceló con 1h", LocalDateTime.now().minusDays(3));

        System.out.println("✅ Datos de demo cargados");
        System.out.println(
                "   Usuarios (>10): admin, superadmin, org1, fran, ana, carlos, lucia, miguel, sofia, pablo, marta, jorge, elena, david, irene");
        System.out.println("   Pistas  (>10): 14 pistas en 6 clubs");
        System.out.println("   Reservas(>10): 12+ reservas en distintos estados");
        System.out.println("   → admin      / Admin1234!");
        System.out.println("   → superadmin / Super1234!");
        System.out.println("   → org1       / Org12345!");
        System.out.println("   → fran / Test1234!");
    }

    private void createClubSchedules(Club club) {
        java.time.DayOfWeek[] weekdays = {
            java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY,
            java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.THURSDAY,
            java.time.DayOfWeek.FRIDAY
        };
        for (java.time.DayOfWeek day : weekdays) {
            horaryClubRepository.save(HoraryClub.builder()
                    .club(club).dayWeek(day)
                    .openTime(LocalTime.of(8, 0)).closeTime(LocalTime.of(22, 0))
                    .build());
        }
        horaryClubRepository.save(HoraryClub.builder()
                .club(club).dayWeek(java.time.DayOfWeek.SATURDAY)
                .openTime(LocalTime.of(9, 0)).closeTime(LocalTime.of(21, 0))
                .build());
        horaryClubRepository.save(HoraryClub.builder()
                .club(club).dayWeek(java.time.DayOfWeek.SUNDAY)
                .openTime(LocalTime.of(10, 0)).closeTime(LocalTime.of(20, 0))
                .build());
    }

    private Booking completedSplit(User owner, Court court, LocalDate date, String result, BigDecimal total) {
        return bookingRepository.save(Booking.builder()
                .owner(owner).court(court).date(date)
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 30))
                .bookingStatus(BookingStatus.COMPLETED).bookingType(BookingType.PUBLIC)
                .splitPayment(true).result(result).totalPrice(total)
                .paymentMethod(PaymentMethod.ONLINE).fullyPaid(true).build());
    }

    /**
     * Participante sin pago (reserva futura o splitPayment=false)
     */
    private void saveParticipant(Booking booking, Player player, Team team) {
        BigDecimal split = booking.getTotalPrice().divide(new BigDecimal("2"), 2,
                java.math.RoundingMode.HALF_UP);
        playerBookingRepository.save(PlayerBooking.builder()
                .booking(booking).player(player).team(team)
                .splitPrice(split).isConfirmed(true).hasPaid(false).build());
    }

    /**
     * Participante con pago Stripe (splitPayment=true, reserva completada)
     */
    private PlayerBooking saveParticipantPaid(Booking booking, Player player, Team team, boolean isWinner) {
        // splitPrice = totalPrice / nº de participantes actuales + 1
        int n = booking.getParticipants().size() + 1;
        BigDecimal split = booking.getTotalPrice().divide(new BigDecimal(n), 2, java.math.RoundingMode.HALF_UP);
        return playerBookingRepository.save(PlayerBooking.builder()
                .booking(booking).player(player).team(team)
                .splitPrice(split).paidAmount(split)
                .paymentMethod(PaymentMethod.ONLINE)
                .isConfirmed(true).hasPaid(true).isWinner(isWinner)
                .paidAt(booking.getDate().atTime(9, 0))
                .build());
    }

    private void saveParticipantWinner(Booking booking, Player player, Team team, boolean isWinner) {
        saveParticipantPaid(booking, player, team, isWinner);
    }

    private void savePenalty(Club club, Booking booking, BigDecimal amount, ClubBalanceReason reason,
            String desc, LocalDateTime when) {
        clubBalanceEntryRepository.save(ClubBalanceEntry.builder()
                .club(club).booking(booking).amount(amount)
                .reason(reason).description(desc).createdAt(when).build());
    }

    // -------------------------------------------------------------------------
    // Location data from JSON
    // -------------------------------------------------------------------------
    private void loadLocationData() throws IOException {
        InputStream inputStream = new ClassPathResource("data/regions.json").getInputStream();
        List<Map<String, Object>> data = objectMapper.readValue(inputStream, new TypeReference<>() {
        });
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
