import { Component, computed, input } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { calendarOutline, timeOutline, lockClosedOutline } from 'ionicons/icons';
import { BookingType } from '@frontend/shared-core';

@Component({
  selector: 'app-booking-info-grid',
  templateUrl: './booking-info-grid.component.html',
  styleUrl: './booking-info-grid.component.scss',
  standalone: true,
  imports: [IonIcon],
})
export class BookingInfoGridComponent {
  readonly date = input.required<string>();       // ISO "2026-04-10"
  readonly startTime = input.required<string>(); // "HH:MM:SS"
  readonly endTime = input.required<string>();   // "HH:MM:SS"
  readonly bookingType = input.required<BookingType>();

  readonly timeRange = computed(() =>
    `${this.startTime().slice(0, 5)} - ${this.endTime().slice(0, 5)}`
  );

  readonly durationMin = computed(() => {
    const [sh, sm] = this.startTime().split(':').map(Number);
    const [eh, em] = this.endTime().split(':').map(Number);
    return (eh * 60 + em) - (sh * 60 + sm);
  });

  readonly typeLabel = computed(() =>
    this.bookingType() === 'PRIVATE' ? 'Reserva Privada' : 'Reserva Pública'
  );

  readonly dateFormatted = computed(() => {
    const d = new Date(this.date() + 'T00:00:00');
    return d.toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' });
  });

  constructor() {
    addIcons({ calendarOutline, timeOutline, lockClosedOutline });
  }
}
