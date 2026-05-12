package com.corty.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {

    // ── KPIs ────────────────────────────────────────────────────────────────
    private long bookingsToday;
    private long bookingsThisMonth;
    private BigDecimal revenueThisMonth;
    private double occupancyRate;          // porcentaje 0-100
    private long activeCourts;
    private long activeClubs;
    private long cancelledThisMonth;

    // Solo ADMIN
    private Long totalUsers;
    private Long newUsersThisWeek;

    // ── Series temporales ────────────────────────────────────────────────────
    private List<DayCount> bookingsLast30Days;
    private List<WeekRevenue> revenueLast8Weeks;

    // ── Distribuciones ───────────────────────────────────────────────────────
    private List<LabelCount> bookingsByStatus;
    private List<LabelCount> bookingsBySport;       // Solo ADMIN
    private List<LabelCount> clubsByOrganization;   // Solo ADMIN

    // ── Rankings ─────────────────────────────────────────────────────────────
    private List<LabelCount> topCourts;

    // ── Tablas rápidas ───────────────────────────────────────────────────────
    private List<UpcomingBooking> upcomingToday;
    private List<RecentBooking> recentBookings;

    // ── Nested DTOs ──────────────────────────────────────────────────────────
    @Data
    @Builder
    public static class DayCount {

        private String date;   // "yyyy-MM-dd"
        private long count;
    }

    @Data
    @Builder
    public static class WeekRevenue {

        private String week;   // "yyyy-Www"
        private BigDecimal revenue;
    }

    @Data
    @Builder
    public static class LabelCount {

        private String label;
        private long count;
    }

    @Data
    @Builder
    public static class UpcomingBooking {

        private Long id;
        private String time;       // "HH:mm – HH:mm"
        private String courtName;
        private String clubName;
        private String username;
        private String status;
    }

    @Data
    @Builder
    public static class RecentBooking {

        private Long id;
        private String date;
        private String courtName;
        private String clubName;
        private String username;
        private BigDecimal totalPrice;
        private String status;
    }
}
