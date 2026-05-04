import { Component, inject, signal } from '@angular/core';
import { IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { PageHeaderComponent } from '../../../../../components/page-header/page-header.component';
import { PlayerProfileResponse, PlayerService } from '@frontend/shared-core';

@Component({
  selector: 'app-privacy',
  templateUrl: './privacy.page.html',
  styleUrl: './privacy.page.scss',
  standalone: true,
  imports: [IonContent, IonSpinner, LucideAngularModule, PageHeaderComponent],
})
export class PrivacyPage {
  private playerService = inject(PlayerService);
  private toastCtrl     = inject(ToastController);

  readonly loading = signal(true);
  readonly saving  = signal(false);
  private profile!: PlayerProfileResponse;

  readonly isPublic = signal(true);

  ionViewWillEnter(): void {
    this.loading.set(true);
    this.playerService.getMyProfile().subscribe({
      next: p => {
        this.profile = p;
        this.isPublic.set(p.publicProfile);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  toggle(): void {
    if (this.saving()) return;
    const next = !this.isPublic();
    this.isPublic.set(next);
    this.saving.set(true);

    this.playerService.updateMyProfile({
      name:          this.profile.name,
      surname:       this.profile.surname,
      biography:     this.profile.biography ?? '',
      publicProfile: next,
    }).subscribe({
      next: p => {
        this.profile = p;
        this.saving.set(false);
      },
      error: async () => {
        this.isPublic.set(!next);
        this.saving.set(false);
        const t = await this.toastCtrl.create({ message: 'No se pudo guardar', duration: 2500, color: 'danger', position: 'top' });
        await t.present();
      },
    });
  }
}
