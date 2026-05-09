export type NotificationType =
  | 'BOOKING_CONFIRMED'
  | 'BOOKING_CANCELLED'
  | 'JOIN_REQUEST'
  | 'JOIN_ACCEPTED'
  | 'JOIN_REJECTED'
  | 'PARTICIPANT_JOINED'
  | 'PARTICIPANT_LEFT'
  | 'FRIEND_REQUEST'
  | 'FRIEND_ACCEPTED'
  | 'NEW_MESSAGE'
  | 'MATCH_READY'
  | 'RESULT_PENDING'
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
