export interface DashboardStats {
  bookingsToday: number;
  bookingsThisMonth: number;
  revenueThisMonth: number;
  occupancyRate: number;
  activeCourts: number;
  activeClubs: number;
  cancelledThisMonth: number;
  totalUsers?: number;
  newUsersThisWeek?: number;
  bookingsLast30Days: DayCount[];
  revenueLast8Weeks: WeekRevenue[];
  bookingsByStatus: LabelCount[];
  bookingsBySport?: LabelCount[];
  clubsByOrganization?: LabelCount[];
  topCourts: LabelCount[];
  upcomingToday: UpcomingBooking[];
  recentBookings: RecentBooking[];
}

export interface DayCount {
  date: string;
  count: number;
}

export interface WeekRevenue {
  week: string;
  revenue: number;
}

export interface LabelCount {
  label: string;
  count: number;
}

export interface UpcomingBooking {
  id: number;
  time: string;
  courtName: string;
  clubName: string;
  username: string;
  status: string;
}

export interface RecentBooking {
  id: number;
  date: string;
  courtName: string;
  clubName: string;
  username: string;
  totalPrice: number;
  status: string;
}
