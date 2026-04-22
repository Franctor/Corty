import { Component, inject } from '@angular/core';
import { ThemeService } from '../../services/theme.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'lib-theme-toggle',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: 'theme-toggle.component.html',
  styleUrl: 'theme-toggle.component.scss',
})
export class ThemeToggleComponent {
  protected theme = inject(ThemeService);
}