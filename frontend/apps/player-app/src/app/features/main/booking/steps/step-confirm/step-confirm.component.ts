import { Component, computed, input, output } from '@angular/core';
import { IonSpinner } from '@ionic/angular/standalone';
import { CourtDetailResponse } from '@frontend/shared-core';
import { BookingState } from '../../booking.page';
import { LucideAngularModule } from 'lucide-angular';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-booking-step-confirm',
  templateUrl: './step-confirm.component.html',
  styleUrls: ['./step-confirm.component.scss'],
  standalone: true,
  imports: [IonSpinner, LucideAngularModule, DecimalPipe],
})
export class BookingStepConfirmComponent {
  readonly court      = input.required<CourtDetailResponse>();
  readonly state      = input.required<BookingState>();
  readonly submitting = input(false);
  readonly confirm    = output<void>();

  readonly durationMinutes = computed(() => {
    const s = this.state();
    if (!s.startTime || !s.endTime) return 0;
    const [sh, sm] = s.startTime.split(':').map(Number);
    const [eh, em] = s.endTime.split(':').map(Number);
    return (eh * 60 + em) - (sh * 60 + sm);
  });

  readonly totalPrice = computed(() => {
    const mins = this.durationMinutes();
    return (this.court().pricePerHour * mins) / 60;
  });

  readonly dateLabel = computed(() => {
    const d = this.state().date;
    if (!d) return '';
    return new Date(d + 'T00:00:00').toLocaleDateString('es-ES', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
    });
  });

  readonly timeRange = computed(() => {
    const s = this.state();
    return `${s.startTime.slice(0, 5)} – ${s.endTime.slice(0, 5)}`;
  });

  readonly typeLabel = computed(() =>
    this.state().bookingType === 'PUBLIC' ? 'Pública' : 'Privada'
  );
}
