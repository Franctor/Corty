export interface CourtAdminResponse {
  id: number;
  name: string;
  pricePerHour: number;
  active: boolean;
  covered: boolean;
  lighting: boolean;
  clubName: string;
  clubCity: string | null;
  clubAddress: string | null;
  clubPhone: string | null;
  clubEmail: string | null;
  clubDescription: string | null;
  clubLogoUrl: string | null;
  sportName: string;
  surfaceName: string | null;
  imageUrl: string | null;
  useClubSchedule: boolean;
  slotDurationMinutes: number;
}

export interface CourtDetailResponse {
  id: number;
  name: string;
  pricePerHour: number;
  covered: boolean;
  lighting: boolean;
  slotDurationMinutes: number;
  imageUrl: string | null;
  sportName: string;
  surfaceName: string | null;
  clubName: string;
  clubCity: string | null;
  clubAddress: string | null;
  clubPhone: string | null;
  clubEmail: string | null;
  clubDescription: string | null;
  clubLogoUrl: string | null;
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
