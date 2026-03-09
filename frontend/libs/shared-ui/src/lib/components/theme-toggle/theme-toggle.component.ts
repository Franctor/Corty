import { Component, inject } from '@angular/core';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'lib-theme-toggle',
  standalone: true,
  templateUrl: 'theme-toggle.component.html',
  styleUrl: 'theme-toggle.component.scss',
})
export class ThemeToggleComponent {
  protected theme = inject(ThemeService);
}