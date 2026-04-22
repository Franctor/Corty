import { Component, input, output } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-admin-page-header',
  templateUrl: 'admin-page-header.component.html',
  styleUrl: 'admin-page-header.component.scss',
  standalone: true,
  imports: [LucideAngularModule],
})
export class AdminPageHeaderComponent {
  readonly title = input.required<string>();
  readonly subtitle = input<string>('');
  readonly buttonLabel = input<string>('Nuevo');
  readonly showButton = input<boolean>(true);
  readonly secondButtonLabel = input<string>('');
  readonly showSecondButton = input<boolean>(false);

  readonly buttonClick = output();
  readonly secondButtonClick = output();
}
