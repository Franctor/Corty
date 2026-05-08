import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_URL } from '../tokens/api.tokens';
import { MessageRequest, MessageResponse, ConversationResponse } from '../models/chat.models';
import { HttpResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http   = inject(HttpClient);
  private apiUrl = inject(API_URL);

  readonly unreadCount = signal(0);

  sendMessage(body: MessageRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/chat/send`, body);
  }

  getConversations(): Observable<ConversationResponse[]> {
    return this.http.get<ConversationResponse[]>(`${this.apiUrl}/chat/conversations`).pipe(
      tap(convs => this.unreadCount.set(convs.reduce((acc, c) => acc + c.unreadMessagesCount, 0)))
    );
  }

  getHistory(conversationId: number): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(`${this.apiUrl}/chat/history/${conversationId}`);
  }

  markRead(conversationId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/chat/history/${conversationId}/read`, {});
  }

  getConversationWith(recipientId: number): Observable<HttpResponse<ConversationResponse>> {
    return this.http.get<ConversationResponse>(`${this.apiUrl}/chat/with/${recipientId}`, { observe: 'response' });
  }
}
