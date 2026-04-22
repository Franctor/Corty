export interface BookingAdminResponse {
  id: number;
  date: string;
  startTime: string;
  endTime: string;
  bookingType: 'PRIVATE' | 'PUBLIC';
  bookingStatus: 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
  totalPrice: number;
  fullyPaid: boolean;
  splitPayment: boolean;
  paymentMethod: string | null;
  notes: string | null;
  createdAt: string;
  courtName: string;
  clubName: string;
  ownerUsername: string;
  participantCount: number;
}

export interface BookingAdminDetailResponse {
  id: number;
  date: string;
  startTime: string;
  endTime: string;
  bookingType: 'PRIVATE' | 'PUBLIC';
  bookingStatus: 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
  totalPrice: number;
  fullyPaid: boolean;
  splitPayment: boolean;
  paymentMethod: string | null;
  notes: string | null;
  createdAt: string;
  courtName: string;
  clubName: string;
  ownerUsername: string;
  participants: BookingParticipantRow[];
}

export interface BookingParticipantRow {
  username: string;
  fullName: string;
  splitPrice: number;
  hasPaid: boolean;
  confirmed: boolean;
  winner: boolean;
  owner: boolean;
}

export interface BookingAdminUpdateRequest {
  bookingStatus?: string;
  notes?: string;
  cancelReason?: string;
}
