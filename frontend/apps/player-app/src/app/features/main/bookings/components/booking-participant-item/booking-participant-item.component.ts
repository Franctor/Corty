import { Component, computed, input } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  trophyOutline, closeCircleOutline,
  checkmarkCircleOutline, timeOutline,
} from 'ionicons/icons';
import { BookingParticipantResponse, MediaUrlPipe } from '@frontend/shared-core';

@Component({
  selector: 'app-booking-participant-item',
  templateUrl: './booking-participant-item.component.html',
  styleUrl: './booking-participant-item.component.scss',
  standalone: true,
  imports: [IonIcon, MediaUrlPipe],
})
export class BookingParticipantItemComponent {
  readonly participant = input.required<BookingParticipantResponse>();
  /** true = reserva pasada (COMPLETED/CANCELLED) */
  readonly isPast = input<boolean>(false);
  /** true = hay ganadores registrados en la reserva */
  readonly hasWinners = input<boolean>(false);

  readonly roleLabel = computed(() => {
    const participant = this.participant();
    const parts: string[] = [];
    if (participant.owner) parts.push('Admin');
    if (participant.currentUser) parts.push('Tú');
    return parts.join(' · ');
  });

  readonly initial = computed(() => this.participant().name[0]?.toUpperCase() ?? '?');

  constructor() {
    addIcons({ trophyOutline, closeCircleOutline, checkmarkCircleOutline, timeOutline });
  }
}
