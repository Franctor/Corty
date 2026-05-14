import { Component, inject, input, output, signal, OnChanges, SimpleChanges } from '@angular/core';
import { BookingService, SlotResponse } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';

interface DayChip {
  iso: string;
  label: string;
  sublabel: string;
}

export interface ScheduleSelection {
  date: string;
  startTime: string;
  endTime: string;
}

@Component({
  selector: 'app-court-schedule-picker',
  templateUrl: './court-schedule-picker.component.html',
  styleUrl: './court-schedule-picker.component.scss',
  standalone: true,
  imports: [LucideAngularModule],
})
export class CourtSchedulePickerComponent implements OnChanges {
  private bookingService = inject(BookingService);

  readonly courtId         = input.required<number>();
  readonly slotDurationMin = input<number>(60);
  readonly selectionChange = output<ScheduleSelection | null>();

  readonly days         = signal<DayChip[]>([]);
  readonly activeDate   = signal<string>('');
  readonly slots        = signal<SlotResponse[]>([]);
  readonly loadingSlots = signal(false);
  readonly chosenStart  = signal<string>('');
  readonly chosenEnd    = signal<string>('');

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['courtId']) {
      this.reset();
      this.days.set(this.buildDays(14));
    }
  }

  selectDay(iso: string): void {
    if (iso === this.activeDate()) return;
    this.activeDate.set(iso);
    this.chosenStart.set('');
    this.chosenEnd.set('');
    this.selectionChange.emit(null);
    this.loadSlotsFor(iso);
  }

  selectSlot(slot: SlotResponse): void {
    if (!slot.available) return;
    this.chosenStart.set(slot.startTime);
    this.chosenEnd.set(slot.endTime);
    this.selectionChange.emit({
      date: this.activeDate(),
      startTime: slot.startTime,
      endTime: slot.endTime,
    });
  }

  isChosen(slot: SlotResponse): boolean {
    return slot.startTime === this.chosenStart();
  }

  fmt(time: string): string {
    return time.slice(0, 5);
  }

  private loadSlotsFor(date: string): void {
    this.loadingSlots.set(true);
    this.slots.set([]);
    this.bookingService.getAvailableSlots(this.courtId(), date).subscribe({
      next: response => { this.slots.set(response.slots); this.loadingSlots.set(false); },
      error: () => this.loadingSlots.set(false),
    });
  }

  private reset(): void {
    this.activeDate.set('');
    this.slots.set([]);
    this.chosenStart.set('');
    this.chosenEnd.set('');
    this.selectionChange.emit(null);
  }

  private buildDays(count: number): DayChip[] {
    const result: DayChip[] = [];
    const today = new Date();
    for (let i = 0; i < count; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() + i);
      const iso = this.toIso(d);
      const label = i === 0
        ? 'Hoy'
        : d.toLocaleDateString('es-ES', { weekday: 'short' }).replace('.', '');
      const sublabel = d.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' }).replace('.', '');
      result.push({ iso, label, sublabel });
    }
    return result;
  }

  private toIso(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}
