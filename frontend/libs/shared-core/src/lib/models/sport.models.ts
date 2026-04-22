export interface SportFilterResponse {
  id: number;
  name: string;
  iconUrl: string;
  color: string;
}

export interface SportResponse {
  id: number;
  name: string;
  playersPerTeam: number;
  playersPerMatch: number;
  iconUrl: string;
  color: string;
  teamSport: boolean;
}

export interface SportRequest {
  name: string;
  playersPerTeam: number;
  playersPerMatch: number;
  iconUrl: string;
  color: string;
  teamSport: boolean;
}
