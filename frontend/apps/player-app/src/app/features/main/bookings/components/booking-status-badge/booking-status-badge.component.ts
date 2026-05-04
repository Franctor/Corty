import { Component, computed, input } from '@angular/core';
import { BookingStatus } from '@frontend/shared-core';

@Component({
  selector: 'app-booking-status-badge',
  templateUrl: './booking-status-badge.component.html',
  styleUrl: './booking-status-badge.component.scss',
  standalone: true,
})
export class BookingStatusBadgeComponent {
  readonly status = input.required<BookingStatus>();

  readonly label = computed(() => {
    switch (this.status()) {
      case 'PENDING_PAYMENT': return 'Pago pendiente';
      case 'CONFIRMED':       return 'Confirmada';
      case 'COMPLETED':       return 'Completada';
      case 'CANCELLED':       return 'Cancelada';
    }
  });

  readonly modifier = computed(() => this.status().toLowerCase().replace('_', '-'));
}
