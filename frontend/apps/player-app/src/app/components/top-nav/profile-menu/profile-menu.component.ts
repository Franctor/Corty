import { Component, inject, output } from '@angular/core';
import { Router } from '@angular/router';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { personOutline, logOutOutline } from 'ionicons/icons';
import { AuthService } from '@frontend/shared-auth';

@Component({
  selector: 'app-profile-menu',
  templateUrl: './profile-menu.component.html',
  styleUrls: ['./profile-menu.component.scss'],
  standalone: true,
  imports: [IonIcon],
})
export class ProfileMenuComponent {
  private router = inject(Router);
  private authService = inject(AuthService);

  readonly closed = output<void>();

  constructor() {
    addIcons({ personOutline, logOutOutline });
  }

  goToProfile(): void {
    this.router.navigate(['/profile']);
    this.closed.emit();
  }

  logout(): void {
    this.authService.logout();
    this.closed.emit();
  }
}