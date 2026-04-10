import { Component, input, output } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-confirm-modal',
  templateUrl: 'confirm-modal.component.html',
  styleUrl: 'confirm-modal.component.scss',
  standalone: true,
  imports: [LucideAngularModule],
})
export class ConfirmModalComponent {
  readonly title = input('¿Confirmar acción?');
  readonly message = input.required<string>();
  readonly confirmLabel = input('Eliminar');
  readonly cancelLabel = input('Cancelar');
  readonly danger = input(true);

  readonly confirmed = output();
  readonly cancelled = output();
}
