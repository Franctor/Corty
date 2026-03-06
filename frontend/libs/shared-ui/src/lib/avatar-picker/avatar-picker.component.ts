import { Component, inject, output, signal } from '@angular/core';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import {
  IonButton, IonIcon, IonModal, IonContent,
  IonHeader, IonToolbar, IonTitle, IonButtons, IonSpinner
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { camera, image, close } from 'ionicons/icons';
import { MediaService } from '@frontend/shared-core';

@Component({
  selector: 'lib-avatar-picker',
  templateUrl: './avatar-picker.component.html',
  styleUrls: ['./avatar-picker.component.scss'],
  standalone: true,
  imports: [
    IonButton, IonIcon, IonModal, IonContent,
    IonHeader, IonToolbar, IonTitle, IonButtons, IonSpinner,
  ],
})
export class AvatarPickerComponent {
  private mediaService = inject(MediaService);

  // Emits the uploaded avatar URL to the parent
  readonly avatarUrl = output<string>();

  readonly previewUrl = signal<string | null>(null);
  readonly isUploading = signal(false);
  readonly isModalOpen = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    addIcons({ camera, image, close });
  }

  openModal(): void {
    this.isModalOpen.set(true);
  }

  closeModal(): void {
    this.isModalOpen.set(false);
  }

  // Opens native camera via Capacitor
  async takePhoto(): Promise<void> {
    await this.captureImage(CameraSource.Camera);
  }

  // Opens device gallery via Capacitor
  async pickFromGallery(): Promise<void> {
    await this.captureImage(CameraSource.Photos);
  }

  // Handles file input for web fallback
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.uploadFile(file);
  }

  private async captureImage(source: CameraSource): Promise<void> {
    try {
      const photo = await Camera.getPhoto({
        resultType: CameraResultType.DataUrl,
        source,
        quality: 80,
        allowEditing: false,
      });

      if (!photo.dataUrl) return;

      // Convert dataUrl to File for upload
      const file = this.dataUrlToFile(photo.dataUrl, 'avatar.jpg');
      this.previewUrl.set(photo.dataUrl);
      this.closeModal();
      this.uploadFile(file);
    } catch {
      // User cancelled — do nothing
    }
  }

  private uploadFile(file: File): void {
    this.isUploading.set(true);
    this.errorMessage.set(null);

    this.mediaService.uploadAvatar(file).subscribe({
      next: (url) => {
        this.isUploading.set(false);
        this.previewUrl.set(url);
        this.avatarUrl.emit(url);
      },
      error: (err) => {
        this.isUploading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Error al subir la imagen');
      },
    });
  }

  // Converts a dataUrl string to a File object
  private dataUrlToFile(dataUrl: string, filename: string): File {
    const [header, data] = dataUrl.split(',');
    const mime = header.match(/:(.*?);/)?.[1] ?? 'image/jpeg';
    const bytes = atob(data);
    const buffer = new Uint8Array(bytes.length);
    for (let i = 0; i < bytes.length; i++) {
      buffer[i] = bytes.charCodeAt(i);
    }
    return new File([buffer], filename, { type: mime });
  }
}