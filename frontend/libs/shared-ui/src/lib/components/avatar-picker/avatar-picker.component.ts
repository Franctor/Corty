import {
  Component, ElementRef, OnDestroy, ViewChild,
  inject, output, signal
} from '@angular/core';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { Platform } from '@ionic/angular/standalone';
import {
  IonButton, IonIcon, IonModal, IonContent,
  IonHeader, IonToolbar, IonTitle, IonButtons, IonSpinner
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { camera, image, close } from 'ionicons/icons';

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
export class AvatarPickerComponent implements OnDestroy {
  private platform = inject(Platform);

  @ViewChild('videoElement') videoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvasElement') canvasRef!: ElementRef<HTMLCanvasElement>;

  // Emits the selected File — parent decides when to upload
  readonly fileSelected = output<File>();

  readonly previewUrl = signal<string | null>(null);
  readonly isModalOpen = signal(false);
  readonly isCameraModalOpen = signal(false);
  readonly isCameraReady = signal(false);
  readonly errorMessage = signal<string | null>(null);

  private stream: MediaStream | null = null;

  constructor() {
    addIcons({ camera, image, close });
  }

  get isNative(): boolean {
    return this.platform.is('capacitor');
  }

  openModal(): void {
    this.isModalOpen.set(true);
  }

  closeModal(): void {
    this.isModalOpen.set(false);
  }

  async takePhoto(): Promise<void> {
    this.closeModal();
    if (this.isNative) {
      await this.captureWithCapacitor(CameraSource.Camera);
    } else {
      await this.openWebCamera();
    }
  }

  async pickFromGallery(): Promise<void> {
    this.closeModal();
    if (this.isNative) {
      await this.captureWithCapacitor(CameraSource.Photos);
    } else {
      document.getElementById('avatar-file-input')?.click();
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.setFile(file);
  }

  async openWebCamera(): Promise<void> {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: 640, height: 480 },
        audio: false,
      });
      this.isCameraModalOpen.set(true);
      setTimeout(() => {
        if (this.videoRef?.nativeElement) {
          this.videoRef.nativeElement.srcObject = this.stream;
          this.videoRef.nativeElement.play();
          this.isCameraReady.set(true);
        }
      }, 300);
    } catch {
      this.errorMessage.set('No se pudo acceder a la cámara');
    }
  }

  capturePhoto(): void {
    const video = this.videoRef.nativeElement;
    const canvas = this.canvasRef.nativeElement;
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    canvas.getContext('2d')!.drawImage(video, 0, 0);

    canvas.toBlob((blob) => {
      if (!blob) return;
      const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' });
      this.closeCameraModal();
      this.setFile(file);
    }, 'image/jpeg', 0.85);
  }

  closeCameraModal(): void {
    this.stopStream();
    this.isCameraReady.set(false);
    this.isCameraModalOpen.set(false);
  }

  private async captureWithCapacitor(source: CameraSource): Promise<void> {
    try {
      const photo = await Camera.getPhoto({
        resultType: CameraResultType.DataUrl,
        source,
        quality: 80,
        allowEditing: false,
      });
      if (!photo.dataUrl) return;
      const file = this.dataUrlToFile(photo.dataUrl, 'avatar.jpg');
      this.setFile(file);
    } catch {
      // User cancelled — do nothing
    }
  }

  // Sets preview and notifies parent with the File object
  private setFile(file: File): void {
    this.errorMessage.set(null);
    const reader = new FileReader();
    reader.onload = () => this.previewUrl.set(reader.result as string);
    reader.readAsDataURL(file);
    this.fileSelected.emit(file);
  }

  private stopStream(): void {
    this.stream?.getTracks().forEach(t => t.stop());
    this.stream = null;
  }

  private dataUrlToFile(dataUrl: string, filename: string): File {
    const [header, data] = dataUrl.split(',');
    const mime = header.match(/:(.*?);/)?.[1] ?? 'image/jpeg';
    const bytes = atob(data);
    const buffer = new Uint8Array(bytes.length);
    for (let i = 0; i < bytes.length; i++) buffer[i] = bytes.charCodeAt(i);
    return new File([buffer], filename, { type: mime });
  }

  ngOnDestroy(): void {
    this.stopStream();
  }
}