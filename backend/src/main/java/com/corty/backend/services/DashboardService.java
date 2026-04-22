package com.corty.backend.services;

import com.corty.backend.dto.DashboardStatsResponse;
import com.corty.backend.dto.DashboardStatsResponse.*;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.Booking;
import com.corty.backend.model.User;
import com.corty.backend.repository.DashboardRepository;
import com.corty.backend.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository repo;
    private final OrganizationRepository organizationRepository;

    public DashboardStatsResponse getStats(User principal) {
        boolean isOrg = principal.getRole() != null && "ORGANIZATION".equals(principal.getRole().getName());
        if (isOrg) {
            Long orgId = organizationRepository.findByUser_IdUser(principal.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada"))
                    .getIdOrganization();
            return buildOrgStats(orgId);
        }
        return buildAdminStats();
    }

    private DashboardStatsResponse buildAdminStats() {
        LocalDate today = LocalDate.now();
        int year  = today.getYear();
        int month = today.getMonthValue();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        long activeCourts  = repo.countActiveCourts();
        long bookedHours   = repo.sumBookedHoursThisMonth(year, month);
        double occupancy   = activeCourts > 0
                ? Math.min(100.0, (bookedHours * 100.0) / (activeCourts * daysInMonth * 10.0))
                : 0.0;

        return DashboardStatsResponse.builder()
                .bookingsToday(repo.countBookingsToday(today))
                .bookingsThisMonth(repo.countBookingsThisMonth(year, month))
                .revenueThisMonth(repo.revenueThisMonth(year, month))
                .occupancyRate(round(occupancy))
                .activeCourts(activeCourts)
                .activeClubs(repo.countClubs())
                .cancelledThisMonth(repo.countCancelledThisMonth(year, month))
                .totalUsers(repo.countTotalUsers())
                .newUsersThisWeek(repo.countNewUsersSince(LocalDateTime.now().minusDays(7)))
                .bookingsLast30Days(buildDayCounts(repo.bookingsPerDayLast30(today.minusDays(29))))
                .revenueLast8Weeks(buildWeekRevenues(repo.revenuePerWeekLast8(today.minusDays(55))))
                .bookingsByStatus(buildLabelCounts(repo.countByStatus()))
                .bookingsBySport(buildLabelCounts(repo.countBySport()))
                .clubsByOrganization(buildLabelCounts(repo.clubsPerOrganization()))
                .topCourts(buildLabelCounts(repo.topCourts(PageRequest.of(0, 5))))
                .upcomingToday(buildUpcoming(repo.upcomingToday(today, PageRequest.of(0, 8))))
                .recentBookings(buildRecent(repo.recentBookings(PageRequest.of(0, 5))))
                .build();
    }

    private DashboardStatsResponse buildOrgStats(Long orgId) {
        LocalDate today = LocalDate.now();
        int year  = today.getYear();
        int month = today.getMonthValue();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        long activeCourts = repo.countActiveCourtsbyOrg(orgId);
        long bookedHours  = repo.sumBookedHoursThisMonthByOrg(orgId, year, month);
        double occupancy  = activeCourts > 0
                ? Math.min(100.0, (bookedHours * 100.0) / (activeCourts * daysInMonth * 10.0))
                : 0.0;

        return DashboardStatsResponse.builder()
                .bookingsToday(repo.countBookingsTodayByOrg(orgId, today))
                .bookingsThisMonth(repo.countBookingsThisMonthByOrg(orgId, year, month))
                .revenueThisMonth(repo.revenueThisMonthByOrg(orgId, year, month))
                .occupancyRate(round(occupancy))
                .activeCourts(activeCourts)
                .activeClubs(repo.countClubsByOrg(orgId))
                .cancelledThisMonth(repo.countCancelledThisMonthByOrg(orgId, year, month))
                .bookingsLast30Days(buildDayCounts(repo.bookingsPerDayLast30ByOrg(orgId, today.minusDays(29))))
                .revenueLast8Weeks(buildWeekRevenues(repo.revenuePerWeekLast8ByOrg(orgId, today.minusDays(55))))
                .bookingsByStatus(buildLabelCounts(repo.countByStatusByOrg(orgId)))
                .topCourts(buildLabelCounts(repo.topCourtsByOrg(orgId, PageRequest.of(0, 5))))
                .upcomingToday(buildUpcoming(repo.upcomingTodayByOrg(orgId, today, PageRequest.of(0, 8))))
                .recentBookings(buildRecent(repo.recentBookingsByOrg(orgId, PageRequest.of(0, 5))))
                .build();
    }

    // ── Builders ─────────────────────────────────────────────────────────────

    private List<DayCount> buildDayCounts(List<Object[]> rows) {
        return rows.stream()
                .map(r -> DayCount.builder()
                        .date(r[0].toString())
                        .count(((Number) r[1]).longValue())
                        .build())
                .toList();
    }

    private List<WeekRevenue> buildWeekRevenues(List<Object[]> rows) {
        return rows.stream()
                .map(r -> WeekRevenue.builder()
                        .week(r[0].toString())
                        .revenue(new BigDecimal(r[1].toString()))
                        .build())
                .toList();
    }

    private List<LabelCount> buildLabelCounts(List<Object[]> rows) {
        return rows.stream()
                .map(r -> LabelCount.builder()
                        .label(r[0] != null ? r[0].toString() : "Sin nombre")
                        .count(((Number) r[1]).longValue())
                        .build())
                .toList();
    }

    private List<UpcomingBooking> buildUpcoming(List<Booking> bookings) {
        return bookings.stream()
                .map(b -> UpcomingBooking.builder()
                        .id(b.getIdBooking())
                        .time(b.getStartTime().toString().substring(0, 5) + " – " + b.getEndTime().toString().substring(0, 5))
                        .courtName(b.getCourt().getName())
                        .clubName(b.getCourt().getClub().getName())
                        .username(b.getOwner().getUsername())
                        .status(b.getBookingStatus().name())
                        .build())
                .toList();
    }

    private List<RecentBooking> buildRecent(List<Booking> bookings) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return bookings.stream()
                .map(b -> RecentBooking.builder()
                        .id(b.getIdBooking())
                        .date(b.getDate().format(fmt))
                        .courtName(b.getCourt().getName())
                        .clubName(b.getCourt().getClub().getName())
                        .username(b.getOwner().getUsername())
                        .totalPrice(b.getTotalPrice())
                        .status(b.getBookingStatus().name())
                        .build())
                .toList();
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
