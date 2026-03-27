import { Component, Input, OnInit, Optional, Self, signal, computed } from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import {
  IonModal, IonPopover, IonDatetime,
  IonButton, IonIcon, IonButtons, Platform
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { calendarOutline } from 'ionicons/icons';
import { FormFieldComponent } from '@frontend/shared-ui';

@Component({
  selector: 'ui-datepicker',
  templateUrl: './ui-datepicker.component.html',
  styleUrls: ['./ui-datepicker.component.scss'],
  standalone: true,
  imports: [IonModal, IonPopover, IonDatetime, IonButton, IonIcon, FormFieldComponent],
})
export class UiDatepickerComponent implements ControlValueAccessor, OnInit {

  @Input() label!: string;
  @Input() optional = false;
  @Input() min?: string;
  @Input() max?: string;

  readonly isDisabled = signal(false);
  readonly isOpen = signal(false);

  private readonly _isoValue = signal<string>('');

  readonly displayValue = computed(() => {
    const iso = this._isoValue();
    if (!iso) return '';
    const [year, month, day] = iso.split('-');
    return `${day}/${month}/${year}`;
  });

  readonly datetimeValue = computed(() => {
    const iso = this._isoValue();
    return iso ? `${iso}T00:00:00` : undefined;
  });

  constructor(
    @Optional() @Self() public ngControl: NgControl,
    private platform: Platform,
  ) {
    if (ngControl) ngControl.valueAccessor = this;
    addIcons({ calendarOutline });
  }

  ngOnInit(): void {}

  get isDesktop(): boolean {
    return this.platform.is('desktop');
  }

  get triggerId(): string {
    return `datepicker-trigger-${this.label?.replace(/\s+/g, '-').toLowerCase()}`;
  }

  openPicker(): void {
    if (this.isDisabled()) return;
    this.isOpen.set(true);
  }

  closePicker(): void {
    this.isOpen.set(false);
    this.onTouched();
  }

  onDatetimeChange(event: any): void {
    const isoString: string = event.detail.value;
    const dateOnly = isoString.split('T')[0];
    this._isoValue.set(dateOnly);
    this.onChange(dateOnly);
  }

  confirm(): void {
    this.closePicker();
  }

  cancel(): void {
    this.closePicker();
  }

  onChange = (_: any) => {};
  onTouched = () => {};

  writeValue(value: any): void { this._isoValue.set(value ?? ''); }
  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled.set(isDisabled); }
}