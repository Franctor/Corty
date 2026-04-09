import { Component, input, output } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-admin-modal',
  templateUrl: 'admin-modal.component.html',
  styleUrl: 'admin-modal.component.scss',
  standalone: true,
  imports: [LucideAngularModule],
})
export class AdminModalComponent {
  readonly title = input.required<string>();
  readonly closeModal = output();
}
