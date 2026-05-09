import { Component, inject, signal } from '@angular/core';
import { IonContent, NavController, ToastController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { ThemeService } from '@frontend/shared-ui';
import { AuthService } from '@frontend/shared-auth';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';
import { ConfirmSheetComponent } from '../../../../components/confirm-sheet/confirm-sheet.component';
import { StatusBar, Style } from '@capacitor/status-bar';
import { NavigationBar } from '@hugotomazi/capacitor-navigation-bar';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.scss',
  standalone: true,
  imports: [IonContent, LucideAngularModule, PageHeaderComponent, ConfirmSheetComponent],
})
export class SettingsPage {
  readonly themeService       = inject(ThemeService);
  private navCtrl             = inject(NavController);
  private authService         = inject(AuthService);
  private toastCtrl           = inject(ToastController);
  readonly isDeleting         = signal(false);
  readonly showDeleteConfirm  = signal(false);

  navigate(path: string): void {
    this.navCtrl.navigateForward(['/profile/settings', path]);
  }

  logout(): void {
    this.authService.logout();
  }

  async toggleTheme(): Promise<void> {
    const wasDark = this.themeService.isDark();
    this.themeService.toggle();
    const nowDark = !wasDark;
    try {
      await StatusBar.setBackgroundColor({ color: nowDark ? '#1C2B33' : '#F7F7F7' });
      await StatusBar.setStyle({ style: nowDark ? Style.Dark : Style.Light });
      await NavigationBar.setColor({ color: nowDark ? '#1C2B33' : '#F7F7F7', darkButtons: !nowDark });
    } catch {  }
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
