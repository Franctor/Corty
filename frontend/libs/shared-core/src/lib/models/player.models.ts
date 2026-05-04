export interface PlayerSportProfile {
  sportId: number;
  sport: string;
  sportIconUrl: string | null;
  sportColor: string | null;
  level: number;
  playedMatches: number;
  wins: number;
  losses: number;
}

export interface PlayerProfileResponse {
  id: number;
  username: string;
  email: string;
  name: string;
  surname: string;
  avatarUrl: string | null;
  biography: string | null;
  phone: string | null;
  gender: 'MALE' | 'FEMALE' | 'OTHER' | null;
  birthDate: string | null;
  karma: number;
  city: string | null;
  publicProfile: boolean;
  sports: PlayerSportProfile[];
}

export interface PlayerProfileUpdateRequest {
  name?: string;
  surname?: string;
  biography?: string;
  avatarUrl?: string;
  cityId?: number;
  publicProfile: boolean;
}

export interface PlayerSportStat {
  sport: string;
  sportIconUrl: string | null;
  sportColor: string | null;
  matches: number;
  wins: number;
  losses: number;
  level: number;
}

export interface PlayerStatsResponse {
  totalMatches: number;
  totalWins: number;
  totalLosses: number;
  karma: number;
  sports: PlayerSportStat[];
}

export interface FriendResponse {
  friendshipId: number;
  playerId: number | null;
  userId: number;
  name: string;
  surname: string;
  avatarUrl: string | null;
  karma: number | null;
  status: 'PENDING' | 'ACCEPTED';
  iAmRequester: boolean;
}
