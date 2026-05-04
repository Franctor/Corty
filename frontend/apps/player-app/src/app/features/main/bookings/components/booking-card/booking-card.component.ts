import { Component, computed, inject, input } from '@angular/core';
import { Router } from '@angular/router';
import { BookingListItemResponse } from '@frontend/shared-core';
import { MediaService } from '@frontend/shared-core';
import { BookingStatusBadgeComponent } from '../booking-status-badge/booking-status-badge.component';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-booking-card',
  templateUrl: './booking-card.component.html',
  styleUrls: ['./booking-card.component.scss'],
  standalone: true,
  imports: [BookingStatusBadgeComponent, LucideAngularModule],
})
export class BookingCardComponent {
  private router      = inject(Router);
  private mediaService = inject(MediaService);

  readonly booking = input.required<BookingListItemResponse>();

  readonly logoUrl = computed(() =>
  this.mediaService.getFullUrl(this.booking().clubLogoUrl ?? '')
);

  readonly timeRange = computed(() => {
    const b = this.booking();
    return `${b.startTime.slice(0, 5)} – ${b.endTime.slice(0, 5)}`;
  });

  readonly durationMinutes = computed(() => {
    const b = this.booking();
    const [sh, sm] = b.startTime.split(':').map(Number);
    const [eh, em] = b.endTime.split(':').map(Number);
    return (eh * 60 + em) - (sh * 60 + sm);
  });

  readonly dateLabel = computed(() => {
    const d = this.booking().date;
    return new Date(d + 'T00:00:00').toLocaleDateString('es-ES', {
      weekday: 'short', day: 'numeric', month: 'short',
    });
  });

  goToDetail(): void {
    this.router.navigate(['/bookings', this.booking().id]);
  }
}
