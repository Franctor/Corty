import {
  Component, forwardRef, input, signal, computed, ChangeDetectionStrategy,
  HostListener, ElementRef, inject,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { NgStyle } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

interface CalendarDay { date: Date; currentMonth: boolean; }

const MONTHS_ES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
const DAYS_ES   = ['L','M','X','J','V','S','D'];
const DROPDOWN_HEIGHT = 310;

@Component({
  selector: 'ui-date-input',
  templateUrl: 'date-input.component.html',
  styleUrl: 'date-input.component.scss',
  standalone: true,
  imports: [NgStyle, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => DateInputComponent), multi: true }],
})
export class DateInputComponent implements ControlValueAccessor {
  private el = inject(ElementRef);

  readonly label       = input<string>('');
  readonly placeholder = input<string>('Selecciona fecha');
  readonly disabled    = input(false);
  readonly dropUp      = input(false);
  readonly minDate     = input<string>('');

  readonly isOpen          = signal(false);
  readonly selectedDate    = signal<Date | null>(null);
  readonly viewYear        = signal(new Date().getFullYear());
  readonly viewMonth       = signal(new Date().getMonth());
  readonly dropdownStyle   = signal<Record<string, string>>({});
  private readonly disabledByForm = signal(false);

  readonly monthLabel = computed(() => MONTHS_ES[this.viewMonth()]);
  readonly weekDays   = DAYS_ES;

  readonly displayValue = computed(() => {
    const d = this.selectedDate();
    if (!d) return '';
    return `${String(d.getDate()).padStart(2,'0')}/${String(d.getMonth()+1).padStart(2,'0')}/${d.getFullYear()}`;
  });

  readonly calendarDays = computed<CalendarDay[]>(() => {
    const year  = this.viewYear();
    const month = this.viewMonth();
    const first = new Date(year, month, 1);
    let startDow = first.getDay() - 1;
    if (startDow < 0) startDow = 6;
    const days: CalendarDay[] = [];
    for (let i = startDow; i > 0; i--) {
      days.push({ date: new Date(year, month, 1 - i), currentMonth: false });
    }
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    for (let d = 1; d <= daysInMonth; d++) {
      days.push({ date: new Date(year, month, d), currentMonth: true });
    }
    while (days.length < 42) {
      const last = days[days.length - 1].date;
      const next = new Date(last); next.setDate(next.getDate() + 1);
      days.push({ date: next, currentMonth: false });
    }
    return days;
  });

  private onChange: (v: string) => void = () => {};
  protected onTouched: () => void = () => {};

  @HostListener('document:click', ['$event'])
  onOutsideClick(e: MouseEvent): void {
    if (!this.el.nativeElement.contains(e.target)) this.isOpen.set(false);
  }

  toggle(): void {
    if (this.disabled() || this.disabledByForm()) return;
    if (!this.isOpen()) this.positionDropdown();
    this.isOpen.update(v => !v);
  }

  private positionDropdown(): void {
    const rect: DOMRect = this.el.nativeElement.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const openUp = this.dropUp() || spaceBelow < DROPDOWN_HEIGHT + 8;
    const style: Record<string, string> = { left: `${rect.left}px` };
    if (openUp) {
      style['bottom'] = `${window.innerHeight - rect.top + 4}px`;
      style['top'] = 'auto';
    } else {
      style['top'] = `${rect.bottom + 4}px`;
      style['bottom'] = 'auto';
    }
    this.dropdownStyle.set(style);
  }

  prevMonth(): void {
    const m = this.viewMonth();
    if (m === 0) { this.viewMonth.set(11); this.viewYear.update(y => y - 1); }
    else         { this.viewMonth.set(m - 1); }
  }

  nextMonth(): void {
    const m = this.viewMonth();
    if (m === 11) { this.viewMonth.set(0); this.viewYear.update(y => y + 1); }
    else          { this.viewMonth.set(m + 1); }
  }

  isDayDisabled(day: CalendarDay): boolean {
    const min = this.minDate();
    if (!min) return false;
    return this.toIso(day.date) < min;
  }

  selectDay(day: CalendarDay): void {
    if (this.isDayDisabled(day)) return;
    this.selectedDate.set(day.date);
    this.isOpen.set(false);
    const iso = this.toIso(day.date);
    this.onChange(iso);
    this.onTouched();
  }

  isSelected(day: CalendarDay): boolean {
    const s = this.selectedDate();
    if (!s) return false;
    return s.toDateString() === day.date.toDateString();
  }

  isToday(day: CalendarDay): boolean {
    return day.date.toDateString() === new Date().toDateString();
  }

  private toIso(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  }

  writeValue(value: string): void {
    if (value) {
      const d = new Date(value + 'T00:00:00');
      this.selectedDate.set(d);
      this.viewMonth.set(d.getMonth());
      this.viewYear.set(d.getFullYear());
    } else {
      this.selectedDate.set(null);
    }
  }

  registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void         { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void      { this.disabledByForm.set(isDisabled); }
}
