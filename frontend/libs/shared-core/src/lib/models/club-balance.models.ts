export type ClubBalanceReason =
  | 'PARTICIPANT_LATE_CANCEL'
  | 'PARTICIPANT_LAST_MINUTE_CANCEL'
  | 'OWNER_LATE_CANCEL'
  | 'OWNER_LAST_MINUTE_CANCEL';

export interface ClubBalanceEntry {
  id: number;
  amount: number;
  reason: ClubBalanceReason;
  description: string | null;
  bookingId: number | null;
  createdAt: string;
}

export interface ClubStatsResponse {
  totalRevenue: number;
  totalPenalties: number;
  revenueByMonth: Record<string, number>;
  penaltiesByMonth: Record<string, number>;
  penaltiesByReason: Record<string, number>;
  balanceEntries: ClubBalanceEntry[];
}
