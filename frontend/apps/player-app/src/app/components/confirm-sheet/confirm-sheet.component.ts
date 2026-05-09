import { Component, ElementRef, inject, input, output, viewChild } from '@angular/core';
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

  private panelRef = viewChild<ElementRef<HTMLElement>>('panel');
  private dragStartY = 0;
  private dragging   = false;

  constructor() {
    addIcons({ closeOutline, personRemoveOutline, warningOutline, trashOutline });
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('confirm-sheet__backdrop')) {
      this.cancelled.emit();
    }
  }

  onPanelTouchStart(event: TouchEvent): void {
    if (this.isDesktop()) return;
    this.dragStartY = event.touches[0].clientY;
    this.dragging   = true;
  }

  onPanelTouchMove(event: TouchEvent): void {
    if (!this.dragging) return;
    const dy = event.touches[0].clientY - this.dragStartY;
    if (dy > 0) {
      const el = this.panelRef()?.nativeElement;
      if (el) el.style.transform = `translateY(${dy}px)`;
    }
  }

  onPanelTouchEnd(event: TouchEvent): void {
    if (!this.dragging) return;
    this.dragging = false;
    const dy = event.changedTouches[0].clientY - this.dragStartY;
    const el = this.panelRef()?.nativeElement;
    if (dy > 80) {
      this.cancelled.emit();
    } else {
      if (el) el.style.transform = '';
    }
  }
}
