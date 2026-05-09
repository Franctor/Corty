import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent, IonSpinner } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { DatePipe } from '@angular/common';
import { NotificationService, NotificationResponse } from '@frontend/shared-core';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.page.html',
  styleUrl: './notifications.page.scss',
  standalone: true,
  imports: [IonContent, IonSpinner, LucideAngularModule, PageHeaderComponent, DatePipe],
})
export class NotificationsPage {
  private notificationService = inject(NotificationService);
  private router              = inject(Router);

  readonly notifications = signal<NotificationResponse[]>([]);
  readonly loading       = signal(true);

  ionViewWillEnter(): void {
    this.loading.set(true);
    this.notificationService.getAll().subscribe({
      next: list => { this.notifications.set(list); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }

  onMarkAllRead(): void {
    this.notificationService.markAllRead().subscribe(() => {
      this.notifications.update(list => list.map(n => ({ ...n, read: true })));
    });
  }

  onNotificationClick(n: NotificationResponse): void {
    if (!n.read) {
      this.notificationService.markRead(n.id).subscribe(() => {
        this.notifications.update(list =>
          list.map(item => item.id === n.id ? { ...item, read: true } : item)
        );
      });
    }
    this.navigate(n);
  }

  private navigate(n: NotificationResponse): void {
    if (!n.referenceId) return;
    if (this.isBookingType(n.type)) {
      this.router.navigate(['/bookings', n.referenceId]);
    } else if (n.type === 'FRIEND_REQUEST' || n.type === 'FRIEND_ACCEPTED') {
      this.router.navigate(['/social']);
    } else if (n.type === 'NEW_MESSAGE') {
      this.router.navigate(['/social/chat', n.referenceId]);
    }
  }

  private isBookingType(type: NotificationResponse['type']): boolean {
    return [
      'BOOKING_CONFIRMED', 'BOOKING_CANCELLED',
      'JOIN_REQUEST', 'JOIN_ACCEPTED', 'JOIN_REJECTED',
      'PARTICIPANT_JOINED', 'PARTICIPANT_LEFT',
      'MATCH_READY', 'RESULT_PENDING',
    ].includes(type);
  }

  iconForType(type: NotificationResponse['type']): string {
    const map: Partial<Record<NotificationResponse['type'], string>> = {
      JOIN_ACCEPTED:     'circle-check',
      JOIN_REJECTED:     'circle-x',
      JOIN_REQUEST:      'user-plus',
      BOOKING_CANCELLED:  'calendar-x',
      BOOKING_CONFIRMED:  'calendar-check',
      PARTICIPANT_JOINED: 'user-plus',
      PARTICIPANT_LEFT:   'user-minus',
      FRIEND_REQUEST:    'user-plus',
      FRIEND_ACCEPTED:   'users',
      NEW_MESSAGE:       'message-circle',
      MATCH_READY:       'zap',
      RESULT_PENDING:    'trophy',
      LEVEL_UP:          'trending-up',
      SYSTEM_ALERT:      'bell',
    };
    return map[type] ?? 'bell';
  }

  hasUnread(): boolean {
    return this.notifications().some(n => !n.read);
  }
}
