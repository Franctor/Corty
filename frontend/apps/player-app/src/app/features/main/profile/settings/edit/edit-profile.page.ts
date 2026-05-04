import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { PageHeaderComponent } from '../../../../../components/page-header/page-header.component';
import { AvatarPickerComponent } from '../../../../../components/avatar-picker/avatar-picker.component';
import { MediaService, MediaUrlPipe, PlayerProfileResponse, PlayerService } from '@frontend/shared-core';

@Component({
  selector: 'app-edit-profile',
  templateUrl: './edit-profile.page.html',
  styleUrl: './edit-profile.page.scss',
  standalone: true,
  imports: [IonContent, IonSpinner, ReactiveFormsModule, LucideAngularModule, PageHeaderComponent, AvatarPickerComponent, MediaUrlPipe],
})
export class EditProfilePage {
  private playerService = inject(PlayerService);
  private mediaService  = inject(MediaService);
  private router        = inject(Router);
  private toastCtrl     = inject(ToastController);

  readonly loading     = signal(true);
  readonly saving      = signal(false);
  readonly currentAvatar = signal<string | null>(null);
  private pendingFile: File | null = null;
  private profile!: PlayerProfileResponse;

  readonly form = new FormGroup({
    name:      new FormControl('', [Validators.required, Validators.maxLength(50)]),
    surname:   new FormControl('', [Validators.required, Validators.maxLength(50)]),
    biography: new FormControl('', [Validators.maxLength(500)]),
  });

  ionViewWillEnter(): void {
    this.loading.set(true);
    this.pendingFile = null;
    this.playerService.getMyProfile().subscribe({
      next: p => {
        this.profile = p;
        this.currentAvatar.set(p.avatarUrl);
        this.form.patchValue({ name: p.name, surname: p.surname, biography: p.biography ?? '' });
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onFileSelected(file: File): void {
    this.pendingFile = file;
  }

  onFileRemoved(): void {
    this.pendingFile = null;
    this.currentAvatar.set(null);
  }

  async onSave(): Promise<void> {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);

    const doSave = (avatarUrl: string | null) => {
      const { name, surname, biography } = this.form.getRawValue();
      this.playerService.updateMyProfile({
        name:          name!,
        surname:       surname!,
        biography:     biography ?? '',
        avatarUrl:     avatarUrl ?? undefined,
        publicProfile: this.profile.publicProfile,
      }).subscribe({
        next: async () => {
          this.saving.set(false);
          const t = await this.toastCtrl.create({ message: 'Perfil actualizado', duration: 2000, color: 'success', position: 'top' });
          await t.present();
          this.router.navigate(['/profile/settings']);
        },
        error: async () => {
          this.saving.set(false);
          const t = await this.toastCtrl.create({ message: 'No se pudo guardar', duration: 2500, color: 'danger', position: 'top' });
          await t.present();
        },
      });
    };

    if (this.pendingFile) {
      this.mediaService.uploadAvatar(this.pendingFile, this.profile.id).subscribe({
        next: url => doSave(url),
        error: async () => {
          this.saving.set(false);
          const t = await this.toastCtrl.create({ message: 'Error al subir la imagen', duration: 2500, color: 'danger', position: 'top' });
          await t.present();
        },
      });
    } else {
      doSave(this.currentAvatar());
    }
  }
}
