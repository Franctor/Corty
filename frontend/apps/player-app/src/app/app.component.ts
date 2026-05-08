import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ThemeService } from '@frontend/shared-ui';
import { IonApp, IonRouterOutlet, Platform, NavController } from '@ionic/angular/standalone';
import { App } from '@capacitor/app';
import { PushNotifications } from '@capacitor/push-notifications';
import { NotificationService } from '@frontend/shared-core';
import { AuthService } from '@frontend/shared-auth';

const TAB_ROOTS = ['/home', '/explore', '/bookings', '/social', '/profile'];

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  standalone: true,
  imports: [IonApp, IonRouterOutlet],
})
export class AppComponent {
  private themeService        = inject(ThemeService);
  private platform            = inject(Platform);
  private router              = inject(Router);
  private navCtrl             = inject(NavController);
  private notificationService = inject(NotificationService);
  private authService         = inject(AuthService);

  constructor() {
    this.platform.ready().then(() => {
      this.platform.backButton.subscribeWithPriority(10, async () => {
        const current = this.router.url.split('?')[0];
        const isTabRoot = TAB_ROOTS.some(r => current === r || current === '/');

        if (isTabRoot) {
          if (current === '/home' || current === '/') {
            App.exitApp();
          } else {
            this.navCtrl.navigateRoot('/home');
          }
        } else {
          this.navCtrl.pop();
        }
      });

      if (this.platform.is('capacitor')) {
        this.initPushNotifications();
      }
    });
  }

  private async initPushNotifications(): Promise<void> {
    const permission = await PushNotifications.requestPermissions();
    if (permission.receive !== 'granted') return;

    await PushNotifications.register();

    PushNotifications.addListener('registration', ({ value }) => {
      localStorage.setItem('fcm_pending_token', value);
      // Si ya hay sesión activa, enviar ahora
      if (this.authService.isLoggedIn()) {
        this.notificationService.registerFcmToken(value).subscribe();
      }
      // Si no, se enviará tras el login via authService.onLoginSuccess
    });

    // Registrar callback para cuando el usuario haga login
    this.authService.onLoginSuccess = () => {
      const token = localStorage.getItem('fcm_pending_token');
      if (token) {
        this.notificationService.registerFcmToken(token).subscribe();
      }
    };
  }
}
