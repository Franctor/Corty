import { Component, inject, computed } from '@angular/core';
import { AuthService } from '@frontend/shared-auth';

@Component({
  selector: 'app-dashboard',
  templateUrl: 'dashboard.component.html',
  styleUrl: 'dashboard.component.scss',
  standalone: true,
  imports: [],
})
export class DashboardComponent {
  private authService = inject(AuthService);

  readonly role = computed(() => this.authService.getRole());
  readonly greeting = computed(() =>
    this.role() === 'ADMIN'
      ? 'Bienvenido, administrador'
      : 'Bienvenido a tu panel'
  );
}
