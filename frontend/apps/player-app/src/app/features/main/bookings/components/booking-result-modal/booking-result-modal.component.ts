import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { IonSpinner } from '@ionic/angular/standalone';
import { BookingDetailResponse, BookingParticipantResponse, BookingResultRequest, BookingService, MediaUrlPipe } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';

type TeamSlot = 'A' | 'B' | 'NONE';

interface ParticipantSlot {
  participant: BookingParticipantResponse;
  team: TeamSlot;
}

@Component({
  selector: 'app-booking-result-modal',
  templateUrl: './booking-result-modal.component.html',
  styleUrl: './booking-result-modal.component.scss',
  standalone: true,
  imports: [IonSpinner, MediaUrlPipe, LucideAngularModule],
})
export class BookingResultModalComponent implements OnInit {
  private bookingService = inject(BookingService);

  readonly booking = input.required<BookingDetailResponse>();
  readonly bookingId = input.required<number>();
  readonly closed = output<void>();
  readonly saved = output<void>();

  readonly slotState = signal<ParticipantSlot[]>([]);
  readonly resultText = signal('');
  readonly winnerTeam = signal<'A' | 'B' | null>(null);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly teamA = computed(() => this.slotState().filter(s => s.team === 'A'));
  readonly teamB = computed(() => this.slotState().filter(s => s.team === 'B'));
  readonly unassigned = computed(() => this.slotState().filter(s => s.team === 'NONE'));

  readonly canSubmit = computed(() =>
    !this.submitting() && this.teamA().length > 0 && this.teamB().length > 0
  );

  ngOnInit(): void {
    this.slotState.set(
      this.booking().participants.map(p => ({ participant: p, team: 'NONE' as TeamSlot }))
    );
  }

  setTeam(playerId: number, team: TeamSlot): void {
    this.slotState.update(slots =>
      slots.map(s => s.participant.playerId === playerId ? { ...s, team } : s)
    );
    const winner = this.winnerTeam();
    if (winner === 'A' && this.slotState().filter(s => s.team === 'A').length === 0) {
      this.winnerTeam.set(null);
    } else if (winner === 'B' && this.slotState().filter(s => s.team === 'B').length === 0) {
      this.winnerTeam.set(null);
    }
  }

  setWinner(team: 'A' | 'B' | null): void {
    this.winnerTeam.set(this.winnerTeam() === team ? null : team);
  }

  onSubmit(): void {
    if (!this.canSubmit()) return;
    this.submitting.set(true);
    this.error.set(null);

    const request: BookingResultRequest = {
      result: this.resultText(),
      winnerTeam: this.winnerTeam(),
      assignments: this.slotState().map(s => ({
        playerId: s.participant.playerId,
        team: s.team,
      })),
    };

    this.bookingService.registerResult(this.bookingId(), request).subscribe({
      next: () => {
        this.submitting.set(false);
        this.saved.emit();
      },
      error: err => {
        this.submitting.set(false);
        this.error.set(err?.error?.message ?? 'No se pudo registrar el resultado.');
      },
    });
  }

  onClose(): void {
    this.closed.emit();
  }

  readonly initial = (name: string) => name[0]?.toUpperCase() ?? '?';
}
