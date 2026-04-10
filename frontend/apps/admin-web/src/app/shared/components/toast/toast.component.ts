import { Component, inject } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  templateUrl: 'toast.component.html',
  styleUrl: 'toast.component.scss',
  standalone: true,
  imports: [LucideAngularModule],
})
export class ToastComponent {
  readonly toastService = inject(ToastService);
}
