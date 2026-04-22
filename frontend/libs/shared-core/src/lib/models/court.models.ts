export interface CourtAdminResponse {
  id: number;
  name: string;
  pricePerHour: number;
  active: boolean;
  covered: boolean;
  lighting: boolean;
  clubName: string;
  sportName: string;
  surfaceName: string | null;
  imageUrl: string | null;
  useClubSchedule: boolean;
  slotDurationMinutes: number;
}

export interface CourtRequest {
  name: string;
  pricePerHour: number;
  active: boolean;
  covered: boolean;
  lighting: boolean;
  clubId: number;
  sportId: number;
  surfaceId: number | null;
  imageUrl: string | null;
  useClubSchedule: boolean;
  slotDurationMinutes: number;
}

export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export interface CourtScheduleEntry {
  id?: number;
  dayOfWeek: DayOfWeek;
  openTime: string;
  closeTime: string;
  closed: boolean;
}

export interface CourtBlock {
  id?: number;
  blockDate: string;
  startTime: string;
  endTime: string;
  reason: string | null;
}
