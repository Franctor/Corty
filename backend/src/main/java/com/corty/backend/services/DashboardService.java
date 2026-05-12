package com.corty.backend.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.corty.backend.dto.DashboardStatsResponse;
import com.corty.backend.dto.DashboardStatsResponse.DayCount;
import com.corty.backend.dto.DashboardStatsResponse.LabelCount;
import com.corty.backend.dto.DashboardStatsResponse.RecentBooking;
import com.corty.backend.dto.DashboardStatsResponse.UpcomingBooking;
import com.corty.backend.dto.DashboardStatsResponse.WeekRevenue;
import com.corty.backend.exception.ResourceNotFoundException;
import com.corty.backend.model.Booking;
import com.corty.backend.model.User;
import com.corty.backend.repository.DashboardRepository;
import com.corty.backend.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final BigDecimal CORTY_KEEP = new BigDecimal("0.95");

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
        int year = today.getYear();
        int month = today.getMonthValue();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        long activeCourts = repo.countActiveCourts();
        long bookedHours = repo.sumBookedHoursThisMonth(year, month);
        double occupancy = activeCourts > 0
                ? Math.min(100.0, (bookedHours * 100.0) / (activeCourts * daysInMonth * 10.0))
                : 0.0;

        return DashboardStatsResponse.builder()
                .bookingsToday(repo.countBookingsToday(today))
                .bookingsThisMonth(repo.countBookingsThisMonth(year, month))
                .revenueThisMonth(repo.revenueThisMonthSplit(year, month).add(repo.revenueThisMonthNoSplit(year, month)).multiply(CORTY_KEEP))
                .occupancyRate(round(occupancy))
                .activeCourts(activeCourts)
                .activeClubs(repo.countClubs())
                .cancelledThisMonth(repo.countCancelledThisMonth(year, month))
                .totalUsers(repo.countTotalUsers())
                .newUsersThisWeek(repo.countNewUsersSince(LocalDateTime.now().minusDays(7)))
                .bookingsLast30Days(buildDayCounts(repo.bookingsPerDayLast30(today.minusDays(29))))
                .revenueLast8Weeks(buildWeekRevenuesMerged(
                        repo.revenuePerWeekLast8Split(today.minusDays(55)),
                        repo.revenuePerWeekLast8NoSplit(today.minusDays(55))))
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
        int year = today.getYear();
        int month = today.getMonthValue();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        long activeCourts = repo.countActiveCourtsbyOrg(orgId);
        long bookedHours = repo.sumBookedHoursThisMonthByOrg(orgId, year, month);
        double occupancy = activeCourts > 0
                ? Math.min(100.0, (bookedHours * 100.0) / (activeCourts * daysInMonth * 10.0))
                : 0.0;

        return DashboardStatsResponse.builder()
                .bookingsToday(repo.countBookingsTodayByOrg(orgId, today))
                .bookingsThisMonth(repo.countBookingsThisMonthByOrg(orgId, year, month))
                .revenueThisMonth(repo.revenueThisMonthByOrgSplit(orgId, year, month).add(repo.revenueThisMonthByOrgNoSplit(orgId, year, month)).multiply(CORTY_KEEP))
                .occupancyRate(round(occupancy))
                .activeCourts(activeCourts)
                .activeClubs(repo.countClubsByOrg(orgId))
                .cancelledThisMonth(repo.countCancelledThisMonthByOrg(orgId, year, month))
                .bookingsLast30Days(buildDayCounts(repo.bookingsPerDayLast30ByOrg(orgId, today.minusDays(29))))
                .revenueLast8Weeks(buildWeekRevenuesMerged(
                        repo.revenuePerWeekLast8ByOrgSplit(orgId, today.minusDays(55)),
                        repo.revenuePerWeekLast8ByOrgNoSplit(orgId, today.minusDays(55))))
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

    private List<WeekRevenue> buildWeekRevenuesMerged(List<Object[]> splitRows, List<Object[]> noSplitRows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] r : splitRows) {
            String yw = r[0].toString();
            map.merge(yw, new BigDecimal(r[1].toString()), BigDecimal::add);
        }
        for (Object[] r : noSplitRows) {
            String yw = r[0].toString();
            map.merge(yw, new BigDecimal(r[1].toString()), BigDecimal::add);
        }
        List<WeekRevenue> result = new ArrayList<>();
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> result.add(WeekRevenue.builder()
                .week(e.getKey())
                .revenue(e.getValue().multiply(CORTY_KEEP))
                .build()));
        return result;
    }

    private List<LabelCount> buildLabelCounts(List<Object[]> rows) {
        if (rows == null) {
            return List.of();
        }
        return rows.stream()
                .filter(r -> r != null && r[1] != null)
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
