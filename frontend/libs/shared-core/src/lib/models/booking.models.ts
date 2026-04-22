export interface CancellationResponse {
  message: string;
  karmaDeducted: number;
  karmaRemaining: number;
  refundInfo: string;
}

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
export type BookingType = 'PRIVATE' | 'PUBLIC';
export type Team = 'A' | 'B' | 'NONE';

export interface BookingParticipantResponse {
  playerId: number;
  name: string;
  surname: string;
  avatarUrl: string | null;
  team: Team;
  splitPrice: number;
  hasPaid: boolean;      
  confirmed: boolean;     
  winner: boolean;        
  owner: boolean;        
  currentUser: boolean;  
}

export interface BookingDetailResponse {
  id: number;
  courtName: string;
  clubName: string;
  clubAddress: string;
  clubLogoUrl: string | null;
  clubLat: number | null;
  clubLng: number | null;
  sport: string;
  sportIconUrl: string | null;
  date: string;         // ISO: "2026-04-10"
  startTime: string;    // "HH:MM:SS"
  endTime: string;      // "HH:MM:SS"
  bookingType: BookingType;
  bookingStatus: BookingStatus;
  result: string | null;
  hasWinners: boolean;   
  totalPrice: number;
  courtPrice: number;
  fullyPaid: boolean;
  splitPayment: boolean;
  paymentMethod: string | null;
  currentUserOwner: boolean;
  participants: BookingParticipantResponse[];
}

export interface NextBookingResponse {
  id: number;
  courtLabel: string;
  clubName: string;
  date: string;       // ISO date: "2026-04-10"
  time: string;       // "HH:MM:SS"
  sport: string;
  confirmedPlayers: number;
  totalPlayers: number;
}

export interface NearbyCourtResponse {
  id: number;
  name: string;
  clubName: string;
  clubCity: string | null;
  sport: string;
  surface: string | null;
  pricePerHour: number;
  distance: number;
  coverType: 'indoor' | 'outdoor';
  covered: boolean;
  lighting: boolean;
  imageUrl: string | null;
}

export interface CourtExploreFilters {
  sport?: string;
  surface?: string;
  covered?: boolean;
  lighting?: boolean;
  maxPrice?: number;
  sortBy?: 'distance' | 'price';
  sortDir?: 'asc' | 'desc';
}

export interface RecentActivityResponse {
  id: number;
  sport: string;
  sportIconUrl: string;
  sportColor: string;
  description: string;
  date: string;       // ISO datetime: "2026-04-05T16:00:00"
}