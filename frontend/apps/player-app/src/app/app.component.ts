import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { IonApp, IonRouterOutlet, Platform, NavController } from '@ionic/angular/standalone';
import { App } from '@capacitor/app';
import { PushNotifications } from '@capacitor/push-notifications';
import { StatusBar, Style } from '@capacitor/status-bar';
import { NavigationBar } from '@hugotomazi/capacitor-navigation-bar';
import { NotificationService } from '@frontend/shared-core';
import { AuthService } from '@frontend/shared-auth';
import { ThemeService } from '@frontend/shared-ui';

const TAB_ROOTS = ['/home', '/explore', '/bookings', '/social', '/profile'];

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  standalone: true,
  imports: [IonApp, IonRouterOutlet],
})
export class AppComponent {
  private platform            = inject(Platform);
  private router              = inject(Router);
  private navCtrl             = inject(NavController);
  private notificationService = inject(NotificationService);
  private authService         = inject(AuthService);
  private themeService        = inject(ThemeService);

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
        const dark = this.themeService.isDark();
        StatusBar.setOverlaysWebView({ overlay: false });
        StatusBar.setBackgroundColor({ color: dark ? '#1C2B33' : '#F7F7F7' });
        StatusBar.setStyle({ style: dark ? Style.Dark : Style.Light });
        NavigationBar.setColor({ color: dark ? '#1C2B33' : '#F7F7F7', darkButtons: !dark });
      }
    });
  }

  private async initPushNotifications(): Promise<void> {
    const permission = await PushNotifications.requestPermissions();
    console.log('[FCM] permission:', permission.receive);
    if (permission.receive !== 'granted') return;

    await PushNotifications.register();
    console.log('[FCM] registered');

    PushNotifications.addListener('registration', ({ value }) => {
      console.log('[FCM] token received:', value.slice(0, 20) + '...');
      localStorage.setItem('fcm_pending_token', value);
      if (this.authService.isLoggedIn()) {
        console.log('[FCM] user logged in, sending token to backend');
        this.notificationService.registerFcmToken(value).subscribe({
          next: () => console.log('[FCM] token saved in backend'),
          error: (e) => console.error('[FCM] error saving token', e),
        });
      } else {
        console.log('[FCM] user not logged in, token saved for later');
      }
    });

    PushNotifications.addListener('registrationError', (err) => {
      console.error('[FCM] registration error:', err);
    });

    // Registrar callback para cuando el usuario haga login
    this.authService.onLoginSuccess = () => {
      const token = localStorage.getItem('fcm_pending_token');
      console.log('[FCM] onLoginSuccess, pending token:', !!token);
      if (token) {
        this.notificationService.registerFcmToken(token).subscribe({
          next: () => console.log('[FCM] token saved after login'),
          error: (e) => console.error('[FCM] error saving token after login', e),
        });
      }
    };
  }
}
