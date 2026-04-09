import { Component, inject, computed, input, output } from '@angular/core';
import { AuthService } from '@frontend/shared-auth';
import { LucideAngularModule } from 'lucide-angular';
import { ThemeToggleComponent } from '@frontend/shared-ui';

@Component({
  selector: 'app-header',
  templateUrl: 'header.component.html',
  styleUrl: 'header.component.scss',
  standalone: true,
  imports: [LucideAngularModule, ThemeToggleComponent],
})
export class HeaderComponent {
  private authService = inject(AuthService);

  readonly showMenuButton = input(false);
  readonly menuToggle = output();

  readonly role = computed(() => this.authService.getRole());
  readonly roleLabel = computed(() =>
    this.role() === 'ADMIN' ? 'Administrador' : 'Organización'
  );

  logout(): void {
    this.authService.logout('/auth/login');
  }
}
