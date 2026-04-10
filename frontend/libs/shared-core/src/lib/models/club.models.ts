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
  cityName: string;
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
  cityId: number;
}
