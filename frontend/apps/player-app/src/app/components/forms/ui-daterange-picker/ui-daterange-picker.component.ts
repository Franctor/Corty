import { Component, computed, inject, input, OnInit, Optional, output, Self, signal } from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { IonDatetime, IonIcon, IonModal, IonPopover, Platform } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { calendarOutline, arrowForwardOutline } from 'ionicons/icons';

export interface DateRange {
  from: string | null; // ISO date "YYYY-MM-DD"
  to: string | null;
}

@Component({
  selector: 'ui-daterange-picker',
  templateUrl: './ui-daterange-picker.component.html',
  styleUrls: ['./ui-daterange-picker.component.scss'],
  standalone: true,
  imports: [IonModal, IonPopover, IonDatetime, IonIcon],
})
export class UiDaterangePickerComponent implements ControlValueAccessor {
  private platform = inject(Platform);

  readonly placeholder = input('Cualquier fecha');

  readonly from = signal<string | null>(null);
  readonly to   = signal<string | null>(null);

  readonly isOpen     = signal(false);
  readonly isDisabled = signal(false);
  // which field is being edited in the sheet
  readonly activeField = signal<'from' | 'to'>('from');

  readonly triggerId = 'daterange-trigger';

  readonly displayValue = computed(() => {
    const f = this.from();
    const t = this.to();
    if (!f && !t) return null;
    if (f && !t) return `${this.formatDisplay(f)} →`;
    if (!f && t) return `→ ${this.formatDisplay(t!)}`;
    return `${this.formatDisplay(f!)} – ${this.formatDisplay(t!)}`;
  });

  readonly fromDatetimeValue = computed(() =>
    this.from() ? `${this.from()}T00:00:00` : undefined
  );
  readonly toDatetimeValue = computed(() =>
    this.to() ? `${this.to()}T00:00:00` : undefined
  );

  get isDesktop(): boolean {
    return this.platform.is('desktop');
  }

  constructor(@Optional() @Self() public ngControl: NgControl) {
    if (ngControl) ngControl.valueAccessor = this;
    addIcons({ calendarOutline, arrowForwardOutline });
  }

  open(): void {
    if (this.isDisabled()) return;
    this.activeField.set('from');
    this.isOpen.set(true);
  }

  close(): void {
    this.isOpen.set(false);
    this.onTouched();
  }

  confirm(): void {
    this.emitChange();
    this.close();
  }

  clear(): void {
    this.from.set(null);
    this.to.set(null);
    this.emitChange();
    this.close();
  }

  onFromChange(event: any): void {
    const date = (event.detail.value as string).split('T')[0];
    this.from.set(date);
    // if to is before new from, clear it
    if (this.to() && this.to()! < date) this.to.set(null);
    // auto-advance to "to" field on mobile
    if (!this.isDesktop) this.activeField.set('to');
  }

  onToChange(event: any): void {
    const date = (event.detail.value as string).split('T')[0];
    this.to.set(date);
  }

  selectField(field: 'from' | 'to'): void {
    this.activeField.set(field);
  }

  formatDisplay(iso: string): string {
    const [y, m, d] = iso.split('-');
    const date = new Date(+y, +m - 1, +d);
    return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' });
  }

  private emitChange(): void {
    this.onChange({ from: this.from(), to: this.to() });
  }

  onChange = (_: DateRange) => {};
  onTouched = () => {};
  writeValue(value: DateRange): void {
    this.from.set(value?.from ?? null);
    this.to.set(value?.to ?? null);
  }
  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled.set(isDisabled); }
}
