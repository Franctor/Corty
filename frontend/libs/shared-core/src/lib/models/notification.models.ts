export type NotificationType =
  | 'INVITATION'
  | 'BOOKING_CONFIRMED'
  | 'BOOKING_CANCELLED'
  | 'JOIN_REQUEST'
  | 'JOIN_ACCEPTED'
  | 'JOIN_REJECTED'
  | 'PARTICIPANT_JOINED'
  | 'PARTICIPANT_LEFT'
  | 'PAYMENT_SUCCESS'
  | 'PAYMENT_PENDING'
  | 'FRIEND_REQUEST'
  | 'FRIEND_ACCEPTED'
  | 'NEW_MESSAGE'
  | 'MATCH_READY'
  | 'RESULT_PENDING'
  | 'NEW_REVIEW'
  | 'LEVEL_UP'
  | 'SYSTEM_ALERT';

export interface NotificationResponse {
  id: number;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  type: NotificationType;
  referenceId: number | null;
}
