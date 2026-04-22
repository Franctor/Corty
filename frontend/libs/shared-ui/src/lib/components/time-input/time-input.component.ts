import {
  Component, forwardRef, input, output, signal, computed, effect, ChangeDetectionStrategy,
  HostListener, ElementRef, inject,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { NgStyle } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

const HOURS   = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'));
const MINUTES = ['00', '15', '30', '45'];
const DROPDOWN_HEIGHT = 200;

@Component({
  selector: 'ui-time-input',
  templateUrl: 'time-input.component.html',
  styleUrl: 'time-input.component.scss',
  standalone: true,
  imports: [NgStyle, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => TimeInputComponent), multi: true }],
})
export class TimeInputComponent implements ControlValueAccessor {
  private el = inject(ElementRef);

  readonly label       = input<string>('');
  readonly placeholder = input<string>('--:--');
  readonly disabled    = input(false);
  readonly dropUp      = input(false);
  readonly value       = input<string>('');
  readonly valueChange = output<string>();

  readonly isOpen         = signal(false);
  readonly selectedHour   = signal<string | null>(null);
  readonly selectedMinute = signal<string | null>(null);
  readonly dropdownStyle  = signal<Record<string, string>>({});
  private readonly disabledByForm = signal(false);

  readonly hours   = HOURS;
  readonly minutes = MINUTES;

  readonly displayValue = computed(() => {
    const h = this.selectedHour();
    const m = this.selectedMinute();
    return (h !== null && m !== null) ? `${h}:${m}` : '';
  });

  private onChange: (v: string) => void = () => {};
  protected onTouched: () => void = () => {};

  constructor() {
    effect(() => this.parseAndSet(this.value()));
  }

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

  selectHour(h: string): void {
    this.selectedHour.set(h);
    this.emitIfComplete();
  }

  selectMinute(m: string): void {
    this.selectedMinute.set(m);
    this.emitIfComplete();
  }

  private emitIfComplete(): void {
    const h = this.selectedHour();
    const m = this.selectedMinute();
    if (h === null || m === null) return;
    const value = `${h}:${m}`;
    this.onChange(value);
    this.onTouched();
    this.valueChange.emit(value);
    this.isOpen.set(false);
  }

  private parseAndSet(value: string): void {
    if (!value) {
      this.selectedHour.set(null);
      this.selectedMinute.set(null);
      return;
    }
    const [h, m] = value.split(':');
    this.selectedHour.set(h ?? null);
    const snappedMin = MINUTES.includes(m) ? m : MINUTES.reduce((p, c) => Math.abs(+c - +m) < Math.abs(+p - +m) ? c : p, '00');
    this.selectedMinute.set(snappedMin);
  }

  writeValue(value: string): void {
    this.parseAndSet(value);
  }

  registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void         { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void      { this.disabledByForm.set(isDisabled); }
}
