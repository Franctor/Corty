import {
  Component, ElementRef, OnDestroy, OnInit, ViewChild,
  inject, input, output, signal
} from '@angular/core';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { Platform } from '@ionic/angular/standalone';
import {
  IonButton, IonIcon, IonModal, IonContent,
  IonHeader, IonToolbar, IonTitle, IonButtons, IonSpinner,
  IonPopover, IonList, IonItem, IonLabel,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { camera, image, close, trash } from 'ionicons/icons';
import { BreakpointService } from '@frontend/shared-ui';

@Component({
  selector: 'lib-avatar-picker',
  templateUrl: './avatar-picker.component.html',
  styleUrls: ['./avatar-picker.component.scss'],
  standalone: true,
  imports: [
    IonButton, IonIcon, IonModal, IonContent,
    IonHeader, IonToolbar, IonTitle, IonButtons, IonSpinner,
    IonPopover
  ],
})
export class AvatarPickerComponent implements OnInit, OnDestroy {
  private platform = inject(Platform);
  private bp = inject(BreakpointService);

  @ViewChild('videoElement') videoRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvasElement') canvasRef!: ElementRef<HTMLCanvasElement>;

  readonly initialUrl  = input<string | null>(null);
  readonly fileSelected = output<File>();
  readonly fileRemoved = output<void>();

  readonly previewUrl = signal<string | null>(null);

  ngOnInit(): void {
    if (this.initialUrl()) this.previewUrl.set(this.initialUrl());
  }
  readonly isModalOpen = signal(false);
  readonly isPopoverOpen = signal(false);
  readonly isCameraModalOpen = signal(false);
  readonly isCameraReady = signal(false);
  readonly errorMessage = signal<string | null>(null);

  private stream: MediaStream | null = null;

  constructor() {
    addIcons({ camera, image, close, trash });
  }

  get isNative(): boolean {
    return this.platform.is('capacitor');
  }

  get isDesktop(): boolean {
    return this.bp.isTablet();
  }

  openSourcePicker(): void {
    if (this.isNative) {
      this.isModalOpen.set(true);
      return;
    }
    if (this.isDesktop) {
      this.isPopoverOpen.set(true);
    } else {
      this.isModalOpen.set(true);
    }
  }

  removePhoto(): void {
    this.previewUrl.set(null);
    this.errorMessage.set(null);
    // Reset so the same file can be re-selected afterwards
    const input = document.getElementById('avatar-file-input') as HTMLInputElement | null;
    if (input) input.value = '';
    this.fileRemoved.emit();
  }

  closeModal(): void {
    this.isModalOpen.set(false);
  }

  closePopover(): void {
    this.isPopoverOpen.set(false);
  }

  async takePhoto(): Promise<void> {
    this.closeModal();
    this.closePopover();
    await this.delay(400);
    if (this.isNative) {
      await this.captureWithCapacitor(CameraSource.Camera);
    } else {
      await this.openWebCamera();
    }
  }

  async pickFromGallery(): Promise<void> {
    this.closeModal();
    this.closePopover();
    await this.delay(400);
    if (this.isNative) {
      await this.captureWithCapacitor(CameraSource.Photos);
    } else {
      document.getElementById('avatar-file-input')?.click();
    }
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
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
    } catch {
      this.errorMessage.set('No se pudo acceder a la cámara');
    }
  }

  onCameraModalPresented(): void {
    if (this.videoRef?.nativeElement && this.stream) {
      this.videoRef.nativeElement.srcObject = this.stream;
      this.videoRef.nativeElement.play();
      this.isCameraReady.set(true);
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
        correctOrientation: true,
      });
      if (!photo.dataUrl) return;
      const file = this.dataUrlToFile(photo.dataUrl, 'avatar.jpg');
      this.setFile(file);
    } catch (err) {
      const msg = String((err as any)?.message ?? '').toLowerCase();
      if (!msg.includes('cancel') && !msg.includes('dismiss') && !msg.includes('no image')) {
        this.errorMessage.set('Error: ' + ((err as any)?.message ?? 'desconocido'));
      }
    }
  }

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