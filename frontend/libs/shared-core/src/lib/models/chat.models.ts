export interface MessageRequest {
  senderId: number;
  recipientId: number;
  content: string;
}

export interface MessageResponse {
  idMessage: number;
  content: string;
  sentAt: string;
  idSender: number;
  senderUsername: string;
  idConversation: number;
  read: boolean;
  isMine: boolean;
}

export interface ConversationResponse {
  idConversation: number;
  lastMessageAt: string;
  otherParticipantId: number;
  otherParticipantName: string;
  otherParticipantAvatar?: string;
  lastMessageContent: string;
  lastMessageRead: boolean;
  unreadMessagesCount: number;
}