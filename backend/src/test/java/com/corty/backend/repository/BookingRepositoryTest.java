

package com.corty.backend.repository;

import com.corty.backend.model.*;
import com.corty.backend.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("BookingRepository — queries custom")
class BookingRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired BookingRepository bookingRepository;

    private User owner;
    private Player player;
    private Court court;

    @BeforeEach
    void setUp() {
        Role role = em.persist(Role.builder().name("PLAYER").build());

        owner = em.persist(User.builder()
                .username("testowner")
                .email("owner@test.com")
                .password("hash")
                .role(role)
                .enabled(true)
                .build());

        player = em.persist(Player.builder()
                .name("Test")
                .surname("Player")
                .karma(80)
                .profileComplete(true)
                .user(owner)
                .build());

        Sport sport = em.persist(Sport.builder()
                .name("Pádel")
                .playersPerMatch(4)
                .playersPerTeam(2)
                .iconUrl("padel.png")
                .teamSport(false)
                .build());

        City city = em.persist(City.builder()
                .code("MAD")
                .label("Madrid")
                .build());

        Organization org = em.persist(Organization.builder()
                .user(owner)
                .build());

        Club club = em.persist(Club.builder()
                .name("Club Test")
                .description("Test description")
                .phone("600000001")
                .contactEmail("club@test.com")
                .address("Calle Test 1")
                .nif("12345678A")
                .city(city)
                .organization(org)
                .build());

        court = em.persist(Court.builder()
                .name("Pista 1")
                .pricePerHour(BigDecimal.valueOf(20))
                .club(club)
                .sport(sport)
                .build());

        em.flush();
    }

    // ── existsOverlappingBooking ───────────────────────────────────────────────

    @Test
    @DisplayName("TC-R01: Reservas solapadas → existsOverlappingBooking devuelve true")
    void existsOverlappingBooking_returns_true_when_overlap() {
        em.persist(Booking.builder()
                .court(court)
                .owner(owner)
                .date(LocalDate.of(2030, 6, 15))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingType(BookingType.PRIVATE)
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.valueOf(20))
                .build());
        em.flush();

        boolean result = bookingRepository.existsOverlappingBooking(
                court.getIdCourt(),
                LocalDate.of(2030, 6, 15),
                LocalTime.of(10, 30),
                LocalTime.of(11, 30)
        );

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("TC-R02: Sin solapamiento → existsOverlappingBooking devuelve false")
    void existsOverlappingBooking_returns_false_when_no_overlap() {
        em.persist(Booking.builder()
                .court(court)
                .owner(owner)
                .date(LocalDate.of(2030, 6, 15))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingType(BookingType.PRIVATE)
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.valueOf(20))
                .build());
        em.flush();

        boolean result = bookingRepository.existsOverlappingBooking(
                court.getIdCourt(),
                LocalDate.of(2030, 6, 15),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("TC-R03: Reserva CANCELLED no cuenta como solapamiento")
    void existsOverlappingBooking_ignores_cancelled_bookings() {
        em.persist(Booking.builder()
                .court(court)
                .owner(owner)
                .date(LocalDate.of(2030, 6, 15))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .bookingStatus(BookingStatus.CANCELLED)
                .bookingType(BookingType.PRIVATE)
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.valueOf(20))
                .build());
        em.flush();

        boolean result = bookingRepository.existsOverlappingBooking(
                court.getIdCourt(),
                LocalDate.of(2030, 6, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        assertThat(result).isFalse();
    }

    // ── findUpcomingByUserOrPlayer ─────────────────────────────────────────────

    @Test
    @DisplayName("TC-R04: Owner tiene reserva futura CONFIRMED → aparece en upcoming")
    void findUpcomingByUserOrPlayer_returns_owner_booking() {
        em.persist(Booking.builder()
                .court(court)
                .owner(owner)
                .date(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingType(BookingType.PRIVATE)
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.valueOf(20))
                .build());
        em.flush();

        List<Booking> result = bookingRepository.findUpcomingByUserOrPlayer(
                owner.getIdUser(),
                player.getIdPlayer(),
                LocalDate.now()
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwner().getIdUser()).isEqualTo(owner.getIdUser());
    }

    @Test
    @DisplayName("TC-R05: Reserva pasada CONFIRMED → no aparece en upcoming")
    void findUpcomingByUserOrPlayer_excludes_past_bookings() {
        em.persist(Booking.builder()
                .court(court)
                .owner(owner)
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingType(BookingType.PRIVATE)
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.valueOf(20))
                .build());
        em.flush();

        List<Booking> result = bookingRepository.findUpcomingByUserOrPlayer(
                owner.getIdUser(),
                player.getIdPlayer(),
                LocalDate.now()
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("TC-R06: Reserva futura CANCELLED → no aparece en upcoming")
    void findUpcomingByUserOrPlayer_excludes_cancelled_bookings() {
        em.persist(Booking.builder()
                .court(court)
                .owner(owner)
                .date(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .bookingStatus(BookingStatus.CANCELLED)
                .bookingType(BookingType.PRIVATE)
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(BigDecimal.valueOf(20))
                .build());
        em.flush();

        List<Booking> result = bookingRepository.findUpcomingByUserOrPlayer(
                owner.getIdUser(),
                player.getIdPlayer(),
                LocalDate.now()
        );

        assertThat(result).isEmpty();
    }
}
