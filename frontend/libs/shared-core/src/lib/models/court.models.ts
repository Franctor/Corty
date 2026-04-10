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
}
