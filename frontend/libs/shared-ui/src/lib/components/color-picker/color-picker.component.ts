import {
  Component,
  forwardRef,
  inject,
  signal,
  HostListener,
  ElementRef,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule, FormControl } from '@angular/forms';

const PRESETS = [
  '#58CC02', '#1CB0F6', '#FF4B4B', '#FF9600',
  '#CE82FF', '#FF86D0', '#2B70C9', '#00CD9C',
  '#FFDE00', '#7A7A7A',
];

@Component({
  selector: 'ui-color-picker',
  templateUrl: './color-picker.component.html',
  styleUrl: './color-picker.component.scss',
  standalone: true,
  imports: [ReactiveFormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ColorPickerComponent),
      multi: true,
    },
  ],
})
export class ColorPickerComponent implements ControlValueAccessor {
  private el = inject(ElementRef);

  readonly color = signal('#58CC02');
  readonly open = signal(false);
  readonly disabled = signal(false);
  readonly presets = PRESETS;

  // Needed to bind native color input two-way
  readonly nativeControl = new FormControl('#58CC02');

  private onChange: (v: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  writeValue(value: string): void {
    const v = value || '#58CC02';
    this.color.set(v);
    this.nativeControl.setValue(v, { emitEvent: false });
  }

  registerOnChange(fn: (v: string) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(disabled: boolean): void { this.disabled.set(disabled); }

  toggleOpen(): void {
    if (!this.disabled()) {
      this.open.set(!this.open());
    }
  }

  selectPreset(c: string): void {
    this.color.set(c);
    this.nativeControl.setValue(c, { emitEvent: false });
    this.onChange(c);
    this.onTouched();
    this.open.set(false);
  }

  onNativeChange(event: Event): void {
    const v = (event.target as HTMLInputElement).value;
    this.color.set(v);
    this.onChange(v);
    this.onTouched();
    this.open.set(false);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.el.nativeElement.contains(event.target as Node)) {
      this.open.set(false);
    }
  }
}
