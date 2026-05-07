import { Component, inject, input, output } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { closeOutline, personRemoveOutline, warningOutline, trashOutline } from 'ionicons/icons';
import { BreakpointService } from '@frontend/shared-ui';

export type ConfirmSheetColor = 'danger' | 'warning' | 'primary';

@Component({
  selector: 'app-confirm-sheet',
  templateUrl: './confirm-sheet.component.html',
  styleUrl: './confirm-sheet.component.scss',
  standalone: true,
  imports: [IonIcon],
})
export class ConfirmSheetComponent {
  readonly title         = input.required<string>();
  readonly message       = input.required<string>();
  readonly confirmLabel  = input<string>('Confirmar');
  readonly cancelLabel   = input<string>('Cancelar');
  readonly confirmColor  = input<ConfirmSheetColor>('danger');
  readonly icon          = input<string | null>(null);

  readonly confirmed = output<void>();
  readonly cancelled = output<void>();

  readonly isDesktop = inject(BreakpointService).isTablet;

  constructor() {
    addIcons({ closeOutline, personRemoveOutline, warningOutline, trashOutline });
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('confirm-sheet__backdrop')) {
      this.cancelled.emit();
    }
  }
}
