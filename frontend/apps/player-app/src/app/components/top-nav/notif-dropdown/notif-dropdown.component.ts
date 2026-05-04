import { Component, inject, output } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { NotificationService, NotificationResponse } from '@frontend/shared-core';

@Component({
  selector: 'app-notif-dropdown',
  templateUrl: './notif-dropdown.component.html',
  styleUrl: './notif-dropdown.component.scss',
  standalone: true,
  imports: [LucideAngularModule, DatePipe],
})
export class NotifDropdownComponent {
  private router = inject(Router);
  readonly notificationService = inject(NotificationService);

  readonly close = output<void>();

  readonly notifications = this.notificationService.latest;
  readonly unreadCount = this.notificationService.unreadCount;

  onMarkAllRead(): void {
    this.notificationService.markAllRead().subscribe();
  }

  onNotificationClick(n: NotificationResponse): void {
    if (!n.read) {
      this.notificationService.markRead(n.id).subscribe();
    }
    if (n.referenceId && this.isBookingType(n.type)) {
      this.router.navigate(['/bookings', n.referenceId]);
      this.close.emit();
    }
  }

  onViewAll(): void {
    this.router.navigate(['/notifications']);
    this.close.emit();
  }

  private isBookingType(type: NotificationResponse['type']): boolean {
    return [
      'INVITATION', 'BOOKING_CONFIRMED', 'BOOKING_CANCELLED',
      'JOIN_REQUEST', 'JOIN_ACCEPTED', 'JOIN_REJECTED',
      'PARTICIPANT_JOINED', 'PARTICIPANT_LEFT',
      'MATCH_READY', 'RESULT_PENDING',
    ].includes(type);
  }

  iconForType(type: NotificationResponse['type']): string {
    const map: Partial<Record<NotificationResponse['type'], string>> = {
      JOIN_ACCEPTED: 'circle-check',
      JOIN_REJECTED: 'circle-x',
      JOIN_REQUEST: 'user-plus',
      BOOKING_CANCELLED: 'calendar-x',
      BOOKING_CONFIRMED: 'calendar-check',
      PARTICIPANT_JOINED: 'user-plus',
      PARTICIPANT_LEFT: 'user-minus',
      INVITATION: 'mail',
      PAYMENT_SUCCESS: 'credit-card',
      PAYMENT_PENDING: 'clock',
      FRIEND_REQUEST: 'user-plus',
      FRIEND_ACCEPTED: 'users',
      NEW_MESSAGE: 'message-circle',
      MATCH_READY: 'zap',
      RESULT_PENDING: 'trophy',
      LEVEL_UP: 'trending-up',
      NEW_REVIEW: 'star',
      SYSTEM_ALERT: 'bell',
    };
    return map[type] ?? 'bell';
  }
}
