import {
  Component,
  Input,
  OnInit,
  forwardRef,
  inject,
  signal,
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  NgControl,
} from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'lib-image-picker',
  templateUrl: './image-picker.component.html',
  styleUrl: './image-picker.component.scss',
  standalone: true,
  imports: [LucideAngularModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ImagePickerComponent),
      multi: true,
    },
  ],
})
export class ImagePickerComponent implements ControlValueAccessor, OnInit {
  /** 'circle' for avatars, 'square' for icons/thumbnails */
  @Input() shape: 'circle' | 'square' = 'square';
  /** Label shown above the picker */
  @Input() label = 'Imagen';
  /** Accepted MIME types */
  @Input() accept = 'image/*';

  readonly previewUrl = signal<string | null>(null);
  readonly disabled = signal(false);

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  /** Current string value (URL or data URL) exposed to the form */
  private currentValue = '';

  ngOnInit(): void {
    if (this.currentValue) {
      this.previewUrl.set(this.currentValue);
    }
  }

  // ControlValueAccessor

  writeValue(value: string): void {
    this.currentValue = value ?? '';
    this.previewUrl.set(this.currentValue || null);
  }

  registerOnChange(fn: (v: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.disabled.set(disabled);
  }

  // Interactions

  triggerPicker(input: HTMLInputElement): void {
    if (!this.disabled()) input.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result as string;
      this.previewUrl.set(dataUrl);
      this.currentValue = dataUrl;
      this.onChange(dataUrl);
      this.onTouched();
    };
    reader.readAsDataURL(file);
    // Reset so same file can be re-selected
    input.value = '';
  }

  clear(): void {
    this.previewUrl.set(null);
    this.currentValue = '';
    this.onChange('');
    this.onTouched();
  }
}
