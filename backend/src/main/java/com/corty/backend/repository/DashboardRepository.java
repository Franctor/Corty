package com.corty.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.corty.backend.model.Booking;

@Repository
public interface DashboardRepository extends JpaRepository<Booking, Long> {

    // ── KPIs globales ────────────────────────────────────────────────────────
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.date = :today AND b.bookingStatus <> 'CANCELLED'")
    long countBookingsToday(@Param("today") LocalDate today);

    @Query("SELECT COUNT(b) FROM Booking b WHERE YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus IN ('CONFIRMED','COMPLETED')")
    long countBookingsThisMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(COALESCE(pb.paidAmount, pb.splitPrice)), 0) FROM Booking b JOIN b.participants pb WHERE YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus = 'COMPLETED' AND b.splitPayment = true")
    BigDecimal revenueThisMonthSplit(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus = 'COMPLETED' AND b.splitPayment = false")
    BigDecimal revenueThisMonthNoSplit(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(b) FROM Booking b WHERE YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus = 'CANCELLED'")
    long countCancelledThisMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(c) FROM Court c WHERE c.active = true")
    long countActiveCourts();

    @Query("SELECT COUNT(cl) FROM Club cl")
    long countClubs();

    // ── KPIs por org ─────────────────────────────────────────────────────────
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId AND b.date = :today AND b.bookingStatus <> 'CANCELLED'")
    long countBookingsTodayByOrg(@Param("orgId") Long orgId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId AND YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus IN ('CONFIRMED','COMPLETED')")
    long countBookingsThisMonthByOrg(@Param("orgId") Long orgId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(COALESCE(pb.paidAmount, pb.splitPrice)), 0) FROM Booking b JOIN b.participants pb WHERE b.court.club.organization.idOrganization = :orgId AND YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus = 'COMPLETED' AND b.splitPayment = true")
    BigDecimal revenueThisMonthByOrgSplit(@Param("orgId") Long orgId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId AND YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus = 'COMPLETED' AND b.splitPayment = false")
    BigDecimal revenueThisMonthByOrgNoSplit(@Param("orgId") Long orgId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId AND YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus = 'CANCELLED'")
    long countCancelledThisMonthByOrg(@Param("orgId") Long orgId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(c) FROM Court c WHERE c.club.organization.idOrganization = :orgId AND c.active = true")
    long countActiveCourtsbyOrg(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(cl) FROM Club cl WHERE cl.organization.idOrganization = :orgId")
    long countClubsByOrg(@Param("orgId") Long orgId);

    // ── Ocupación ─────────────────────────────────────────────────────────────
    // Reservas confirmadas/completadas este mes / total slots teóricos
    // Aproximación: total horas reservadas vs capacidad teórica de pistas activas * días del mes * 10h/día
    @Query("SELECT COALESCE(SUM(HOUR(b.endTime) - HOUR(b.startTime)), 0) FROM Booking b WHERE YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus IN ('CONFIRMED','COMPLETED')")
    long sumBookedHoursThisMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(HOUR(b.endTime) - HOUR(b.startTime)), 0) FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId AND YEAR(b.date) = :year AND MONTH(b.date) = :month AND b.bookingStatus IN ('CONFIRMED','COMPLETED')")
    long sumBookedHoursThisMonthByOrg(@Param("orgId") Long orgId, @Param("year") int year, @Param("month") int month);

    // ── Usuarios ─────────────────────────────────────────────────────────────
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = 'PLAYER'")
    long countTotalUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = 'PLAYER' AND u.creationDate >= :since")
    long countNewUsersSince(@Param("since") java.time.LocalDateTime since);

    // ── Reservas últimos 30 días ──────────────────────────────────────────────
    @Query("SELECT CAST(b.date AS string), COUNT(b) FROM Booking b WHERE b.date >= :since AND b.bookingStatus <> 'CANCELLED' GROUP BY b.date ORDER BY b.date ASC")
    List<Object[]> bookingsPerDayLast30(@Param("since") LocalDate since);

    @Query("SELECT CAST(b.date AS string), COUNT(b) FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId AND b.date >= :since AND b.bookingStatus <> 'CANCELLED' GROUP BY b.date ORDER BY b.date ASC")
    List<Object[]> bookingsPerDayLast30ByOrg(@Param("orgId") Long orgId, @Param("since") LocalDate since);

    // ── Ingresos últimas 8 semanas (nativo MySQL YEARWEEK) ──────────────────
    @Query(value = "SELECT YEARWEEK(b.date, 1) AS yw, COALESCE(SUM(pb.paid_amount), 0) FROM bookings b JOIN player_bookings pb ON pb.id_booking = b.id_booking WHERE b.booking_status = 'COMPLETED' AND b.split_payment = true AND b.date >= :since GROUP BY yw ORDER BY yw ASC", nativeQuery = true)
    List<Object[]> revenuePerWeekLast8Split(@Param("since") LocalDate since);

    @Query(value = "SELECT YEARWEEK(b.date, 1) AS yw, COALESCE(SUM(b.total_price), 0) FROM bookings b WHERE b.booking_status = 'COMPLETED' AND b.split_payment = false AND b.date >= :since GROUP BY yw ORDER BY yw ASC", nativeQuery = true)
    List<Object[]> revenuePerWeekLast8NoSplit(@Param("since") LocalDate since);

    @Query(value = "SELECT YEARWEEK(b.date, 1) AS yw, COALESCE(SUM(pb.paid_amount), 0) FROM bookings b JOIN player_bookings pb ON pb.id_booking = b.id_booking JOIN courts c ON b.id_court = c.id_court JOIN clubs cl ON c.id_club = cl.id_club WHERE cl.id_organization = :orgId AND b.booking_status = 'COMPLETED' AND b.split_payment = true AND b.date >= :since GROUP BY yw ORDER BY yw ASC", nativeQuery = true)
    List<Object[]> revenuePerWeekLast8ByOrgSplit(@Param("orgId") Long orgId, @Param("since") LocalDate since);

    @Query(value = "SELECT YEARWEEK(b.date, 1) AS yw, COALESCE(SUM(b.total_price), 0) FROM bookings b JOIN courts c ON b.id_court = c.id_court JOIN clubs cl ON c.id_club = cl.id_club WHERE cl.id_organization = :orgId AND b.booking_status = 'COMPLETED' AND b.split_payment = false AND b.date >= :since GROUP BY yw ORDER BY yw ASC", nativeQuery = true)
    List<Object[]> revenuePerWeekLast8ByOrgNoSplit(@Param("orgId") Long orgId, @Param("since") LocalDate since);

    // ── Por estado ────────────────────────────────────────────────────────────
    @Query("SELECT b.bookingStatus, COUNT(b) FROM Booking b GROUP BY b.bookingStatus")
    List<Object[]> countByStatus();

    @Query("SELECT b.bookingStatus, COUNT(b) FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId GROUP BY b.bookingStatus")
    List<Object[]> countByStatusByOrg(@Param("orgId") Long orgId);

    // ── Por deporte (admin only) ───────────────────────────────────────────────
    @Query("SELECT s.name, COUNT(b) FROM Booking b JOIN b.court c JOIN c.sport s WHERE b.bookingStatus <> 'CANCELLED' GROUP BY s.idSport, s.name ORDER BY COUNT(b) DESC")
    List<Object[]> countBySport();

    // ── Clubs por organización (admin only) ───────────────────────────────────
    @Query("SELECT o.businessName, COUNT(cl) FROM Club cl JOIN cl.organization o GROUP BY o.idOrganization, o.businessName ORDER BY COUNT(cl) DESC")
    List<Object[]> clubsPerOrganization();

    // ── Top pistas ────────────────────────────────────────────────────────────
    @Query("SELECT b.court.name, COUNT(b) FROM Booking b WHERE b.bookingStatus <> 'CANCELLED' GROUP BY b.court.idCourt, b.court.name ORDER BY COUNT(b) DESC")
    List<Object[]> topCourts(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT b.court.name, COUNT(b) FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId AND b.bookingStatus <> 'CANCELLED' GROUP BY b.court.idCourt, b.court.name ORDER BY COUNT(b) DESC")
    List<Object[]> topCourtsByOrg(@Param("orgId") Long orgId, org.springframework.data.domain.Pageable pageable);

    // ── Próximas reservas hoy ─────────────────────────────────────────────────
    @Query("SELECT b FROM Booking b WHERE b.date = :today AND b.bookingStatus = 'CONFIRMED' ORDER BY b.startTime ASC")
    List<Booking> upcomingToday(@Param("today") LocalDate today, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId AND b.date = :today AND b.bookingStatus = 'CONFIRMED' ORDER BY b.startTime ASC")
    List<Booking> upcomingTodayByOrg(@Param("orgId") Long orgId, @Param("today") LocalDate today, org.springframework.data.domain.Pageable pageable);

    // ── Últimas reservas ──────────────────────────────────────────────────────
    @Query("SELECT b FROM Booking b ORDER BY b.createdAt DESC")
    List<Booking> recentBookings(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.court.club.organization.idOrganization = :orgId ORDER BY b.createdAt DESC")
    List<Booking> recentBookingsByOrg(@Param("orgId") Long orgId, org.springframework.data.domain.Pageable pageable);
}
