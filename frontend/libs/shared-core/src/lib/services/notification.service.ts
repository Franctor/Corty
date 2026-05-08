import { inject, Injectable, NgZone, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';
import { API_URL } from '../tokens/api.tokens';
import { NotificationResponse } from '../models/notification.models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http   = inject(HttpClient);
  private apiUrl = inject(API_URL);
  private ngZone = inject(NgZone);

  readonly unreadCount  = signal(0);
  readonly latest       = signal<NotificationResponse[]>([]);
  readonly lastReceived = signal<NotificationResponse | null>(null);
  readonly lastEvent    = signal<NotificationResponse | null>(null);

  private eventSource: EventSource | null = null;

  private isTypeEnabled(type: string): boolean {
    try {
      const raw = localStorage.getItem('notif_prefs');
      if (!raw) return true;
      const prefs = JSON.parse(raw) as Record<string, boolean>;
      return prefs[type] !== false;
    } catch {
      return true;
    }
  }

  connectSse(token: string): void {
    if (this.eventSource) return;
    this.eventSource = new EventSource(`${this.apiUrl}/notifications/stream?token=${token}`);
    this.eventSource.addEventListener('notification', (e: MessageEvent) => {
      const notif: NotificationResponse = JSON.parse(e.data);
      this.ngZone.run(() => this.handleIncoming(notif));
    });
    this.eventSource.onerror = () => {
      this.eventSource?.close();
      this.eventSource = null;
    };
  }

  disconnectSse(): void {
    this.eventSource?.close();
    this.eventSource = null;
  }

  private handleIncoming(notif: NotificationResponse): void {
    this.lastEvent.set(notif);
    if (!this.isTypeEnabled(notif.type)) return;
    this.latest.update(list => [notif, ...list].slice(0, 10));
    this.unreadCount.update(c => c + 1);
    this.lastReceived.set(notif);
  }

  registerFcmToken(token: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/notifications/fcm-token`, { token });
  }

  loadUnreadCount(): void {
    this.http.get<NotificationResponse[]>(`${this.apiUrl}/notifications`)
      .subscribe(list => {
        const filtered = list.filter(n => this.isTypeEnabled(n.type));
        this.unreadCount.set(filtered.filter(n => !n.read).length);
        this.latest.set(filtered.slice(0, 10));
      });
  }

  loadLatest(): void {
    this.http.get<NotificationResponse[]>(`${this.apiUrl}/notifications`)
      .subscribe(list => this.latest.set(list.filter(n => this.isTypeEnabled(n.type)).slice(0, 10)));
  }

  getAll(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.apiUrl}/notifications`).pipe(
      map(list => list.filter(n => this.isTypeEnabled(n.type)))
    );
  }

  markRead(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/notifications/${id}/read`, {}).pipe(
      tap(() => {
        this.unreadCount.update(c => Math.max(0, c - 1));
        this.latest.update(list => list.map(n => n.id === id ? { ...n, read: true } : n));
      })
    );
  }

  markAllRead(): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/notifications/read-all`, {}).pipe(
      tap(() => {
        this.unreadCount.set(0);
        this.latest.update(list => list.map(n => ({ ...n, read: true })));
      })
    );
  }
}
