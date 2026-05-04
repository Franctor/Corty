export interface JoinPaymentCheckResponse {
  requiresPayment: boolean;
  hasPaymentMethod: boolean;
  amount: number;
}

export interface CancellationResponse {
  message: string;
  karmaDeducted: number;
  karmaRemaining: number;
  refundInfo: string;
}

export type BookingStatus = 'PENDING_PAYMENT' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
export type PaymentMethod = 'CASH' | 'CREDIT_CARD' | 'WALLET';
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
  teamSport: boolean;
  totalPrice: number;
  courtPrice: number;
  fullyPaid: boolean;
  splitPayment: boolean;
  paymentMethod: string | null;
  currentUserOwner: boolean;
  currentUserParticipant: boolean;
  myJoinRequestStatus: 'PENDING' | 'ACCEPTED' | 'REJECTED' | null;
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

export interface SlotResponse {
  startTime: string; // "HH:MM:SS"
  endTime: string;
  available: boolean;
}

export interface BookingCreateRequest {
  courtId: number;
  date: string;         // "YYYY-MM-DD"
  startTime: string;    // "HH:MM:SS"
  endTime: string;
  bookingType: BookingType;
  paymentMethod: PaymentMethod;
  splitPayment: boolean;
  notes?: string;
}

export interface BookingCreateResponse {
  bookingId: number;
}

export interface BookingListItemResponse {
  id: number;
  courtName: string;
  clubName: string;
  clubLogoUrl: string | null;
  sport: string;
  sportIconUrl: string | null;
  date: string;
  startTime: string;
  endTime: string;
  bookingStatus: BookingStatus;
  totalPrice: number;
  fullyPaid: boolean;
  participantCount: number;
}

export type JoinRequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface PublicBookingResponse {
  id: number;
  courtName: string;
  clubName: string;
  clubLogoUrl: string | null;
  clubLat: number | null;
  clubLng: number | null;
  sport: string;
  sportIconUrl: string | null;
  sportColor: string | null;
  date: string;
  startTime: string;
  endTime: string;
  currentPlayers: number;
  maxPlayers: number;
  avgLevel: number;
  totalPrice: number;
  pricePerPlayer: number;
  splitPayment: boolean;
  notes: string | null;
  distanceKm: number | null;
  myRequestStatus: JoinRequestStatus | null;
}

export interface JoinRequestResponse {
  id: number;
  playerId: number;
  name: string;
  surname: string;
  avatarUrl: string | null;
  level: number;
  karma: number;
  status: JoinRequestStatus;
}

export interface BookingResultRequest {
  result: string;
  winnerTeam: 'A' | 'B' | null;
  assignments: { playerId: number; team: 'A' | 'B' | 'NONE' }[];
}

export interface RecentActivityResponse {
  id: number;
  sportId: number;
  sport: string;
  sportIconUrl: string;
  sportColor: string;
  description: string;
  date: string;
}