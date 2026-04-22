export interface ClubResponse {
  id: number;
  name: string;
  description: string;
  phone: string;
  contactEmail: string;
  address: string;
  nif: string;
  logoUrl: string | null;
  geoLat: number | null;
  geoLong: number | null;
  cityId: number | null;
  cityName: string;
  organizationId: number | null;
  organizationName: string | null;
}

export interface ClubRequest {
  name: string;
  description: string;
  phone: string;
  contactEmail: string;
  address: string;
  nif: string;
  logoUrl: string | null;
  geoLat: number | null;
  geoLong: number | null;
  cityId: number | null;
  organizationId: number;
}
