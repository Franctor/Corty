import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent, ToastController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { ThemeService } from '@frontend/shared-ui';
import { AuthService } from '@frontend/shared-auth';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';
import { ConfirmSheetComponent } from '../../../../components/confirm-sheet/confirm-sheet.component';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.scss',
  standalone: true,
  imports: [IonContent, LucideAngularModule, PageHeaderComponent, ConfirmSheetComponent],
})
export class SettingsPage {
  readonly themeService       = inject(ThemeService);
  private router              = inject(Router);
  private authService         = inject(AuthService);
  private toastCtrl           = inject(ToastController);
  readonly isDeleting         = signal(false);
  readonly showDeleteConfirm  = signal(false);

  navigate(path: string): void {
    this.router.navigate(['/profile/settings', path]);
  }

  confirmDeleteAccount(): void {
    this.showDeleteConfirm.set(true);
  }

  deleteAccount(): void {
    this.showDeleteConfirm.set(false);
    if (this.isDeleting()) return;
    this.isDeleting.set(true);
    this.authService.deleteAccount().subscribe({
      error: async (err) => {
        this.isDeleting.set(false);
        const msg = err?.error?.message ?? 'No se pudo cerrar la cuenta. Inténtalo más tarde.';
        const toast = await this.toastCtrl.create({ message: msg, duration: 3000, color: 'danger', position: 'top' });
        await toast.present();
      },
    });
  }
}
