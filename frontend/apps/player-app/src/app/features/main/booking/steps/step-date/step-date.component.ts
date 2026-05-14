import { Component, inject, input, output, signal, OnInit } from '@angular/core';
import { IonSpinner } from '@ionic/angular/standalone';
import { AvailabilityResponse, BookingService, CourtDetailResponse, SlotResponse } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';

interface DayChip {
  iso: string;
  label: string;
  sublabel: string;
}

@Component({
  selector: 'app-booking-step-date',
  templateUrl: './step-date.component.html',
  styleUrls: ['./step-date.component.scss'],
  standalone: true,
  imports: [IonSpinner, LucideAngularModule],
})
export class BookingStepDateComponent implements OnInit {
  private bookingService = inject(BookingService);

  readonly court         = input.required<CourtDetailResponse>();
  readonly selectedDate  = input<string>('');
  readonly selectedStart = input<string>('');
  readonly selectedEnd   = input<string>('');
  readonly slotSelected  = output<{ date: string; startTime: string; endTime: string }>();

  readonly activeDate   = signal<string>('');
  readonly slots        = signal<SlotResponse[]>([]);
  readonly courtClosed  = signal(false);
  readonly loadingSlots = signal(false);
  readonly chosenStart  = signal<string>('');
  readonly chosenEnd    = signal<string>('');
  readonly days         = signal<DayChip[]>([]);

  ngOnInit(): void {
    this.days.set(this.buildDays(7));
    this.checkTodayAvailability();
  }

  selectDay(iso: string): void {
    if (iso === this.activeDate()) return;
    this.activeDate.set(iso);
    this.chosenStart.set('');
    this.chosenEnd.set('');
    this.courtClosed.set(false);
    this.loadSlotsFor(iso);
  }

  selectSlot(slot: SlotResponse): void {
    if (!slot.available) return;
    this.chosenStart.set(slot.startTime);
    this.chosenEnd.set(slot.endTime);
  }

  onNext(): void {
    if (!this.activeDate() || !this.chosenStart()) return;
    this.slotSelected.emit({
      date:      this.activeDate(),
      startTime: this.chosenStart(),
      endTime:   this.chosenEnd(),
    });
  }

  isSlotChosen(slot: SlotResponse): boolean {
    return slot.startTime === this.chosenStart();
  }

  formatTime(time: string): string {
    return time.slice(0, 5);
  }

  private checkTodayAvailability(): void {
    const todayIso = this.toIso(new Date());
    this.bookingService.getAvailableSlots(this.court().id, todayIso).subscribe({
      next: (response: AvailabilityResponse) => {
        const { closed, slots } = response;
        const noAvailableToday = closed || slots.length === 0 || slots.every(s => !s.available);
        if (noAvailableToday) {
          const current = this.days();
          const without = current.slice(1);
          const last = new Date();
          last.setDate(last.getDate() + 7);
          without.push(this.makeChip(last, false));
          this.days.set(without);
        }
      },
    });
  }

  private loadSlotsFor(date: string): void {
    this.loadingSlots.set(true);
    this.slots.set([]);
    this.bookingService.getAvailableSlots(this.court().id, date).subscribe({
      next: (response: AvailabilityResponse) => {
        const { closed, slots } = response;
        this.courtClosed.set(closed);

        if (!closed) {
          const todayIso = this.toIso(new Date());
          const isToday = date === todayIso;
          const noAvailableToday = slots.length === 0 || slots.every(s => !s.available);

          if (isToday && noAvailableToday) {
            const current = this.days();
            const without = current.slice(1);
            const last = new Date();
            last.setDate(last.getDate() + 7);
            without.push(this.makeChip(last, false));
            this.days.set(without);
            this.activeDate.set('');
            this.slots.set([]);
          } else {
            this.slots.set(slots);
          }
        }

        this.loadingSlots.set(false);
      },
      error: () => this.loadingSlots.set(false),
    });
  }

  private buildDays(count: number): DayChip[] {
    const result: DayChip[] = [];
    const today = new Date();
    for (let i = 0; i < count; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() + i);
      result.push(this.makeChip(d, i === 0));
    }
    return result;
  }

  private makeChip(d: Date, isToday: boolean): DayChip {
    const iso      = this.toIso(d);
    const label    = isToday
      ? 'Hoy'
      : d.toLocaleDateString('es-ES', { weekday: 'short' }).replace('.', '');
    const sublabel = d.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' }).replace('.', '');
    return { iso, label, sublabel };
  }

  private toIso(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
