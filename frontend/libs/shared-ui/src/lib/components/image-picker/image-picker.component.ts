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
} from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { MediaService, MediaFolder } from '@frontend/shared-core';
import { Capacitor } from '@capacitor/core';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';

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
  @Input() shape: 'circle' | 'square' = 'square';
  @Input() label = 'Imagen';
  @Input() accept = 'image/*';
  @Input() folder: MediaFolder = 'general';
  @Input() entityId?: number;

  readonly previewUrl = signal<string | null>(null);
  readonly disabled = signal(false);
  readonly uploading = signal(false);
  readonly uploadError = signal<string | null>(null);

  private mediaService = inject(MediaService);
  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;
  private currentValue = '';

  ngOnInit(): void {
    if (this.currentValue) {
      this.previewUrl.set(this.mediaService.getFullUrl(this.currentValue));
    }
  }


  writeValue(value: string): void {
    this.currentValue = value ?? '';
    this.previewUrl.set(this.currentValue ? this.mediaService.getFullUrl(this.currentValue) : null);
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

  triggerPicker(input: HTMLInputElement): void {
    if (this.disabled() || this.uploading()) return;
    if (Capacitor.isNativePlatform()) {
      this.pickNative();
    } else {
      input.click();
    }
  }

  private async pickNative(): Promise<void> {
    try {
      const photo = await Camera.getPhoto({
        resultType: CameraResultType.Base64,
        source: CameraSource.Prompt,
        quality: 80,
      });
      if (!photo.base64String) return;
      const mimeType = photo.format === 'png' ? 'image/png' : 'image/jpeg';
      const byteChars = atob(photo.base64String);
      const byteArr = new Uint8Array(byteChars.length);
      for (let i = 0; i < byteChars.length; i++) byteArr[i] = byteChars.charCodeAt(i);
      const blob = new Blob([byteArr], { type: mimeType });
      const file = new File([blob], `photo.${photo.format ?? 'jpg'}`, { type: mimeType });
      this.uploadFile(file, `data:${mimeType};base64,${photo.base64String}`);
    } catch {
      // usuario canceló o error de permisos
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => this.uploadFile(file, reader.result as string);
    reader.readAsDataURL(file);
    input.value = '';
  }

  private uploadFile(file: File, previewDataUrl: string): void {
    this.uploadError.set(null);
    this.uploading.set(true);
    this.previewUrl.set(previewDataUrl);

    this.mediaService.uploadFile(file, this.folder, this.entityId).subscribe({
      next: (url) => {
        this.currentValue = url;
        this.onChange(url);
        this.onTouched();
        this.previewUrl.set(this.mediaService.getFullUrl(url));
        this.uploading.set(false);
      },
      error: () => {
        this.uploadError.set('Error al subir la imagen');
        this.previewUrl.set(null);
        this.uploading.set(false);
        this.onTouched();
      },
    });
  }

  clear(): void {
    this.previewUrl.set(null);
    this.currentValue = '';
    this.uploadError.set(null);
    this.onChange('');
    this.onTouched();
  }
}
