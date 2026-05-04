import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { ThemeService } from '@frontend/shared-ui';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.scss',
  standalone: true,
  imports: [IonContent, LucideAngularModule, PageHeaderComponent],
})
export class SettingsPage {
  readonly themeService = inject(ThemeService);
  private router        = inject(Router);

  navigate(path: string): void {
    this.router.navigate(['/profile/settings', path]);
  }
}
