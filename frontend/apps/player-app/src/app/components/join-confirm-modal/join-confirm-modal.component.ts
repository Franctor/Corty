import { Component, computed, input, output } from '@angular/core';
import { IonModal, IonSpinner } from '@ionic/angular/standalone';
import { DecimalPipe, NgTemplateOutlet } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

const CORTY_FEE = 0.05;

export interface JoinConfirmData {
  courtName: string;
  clubName: string;
  dateLabel: string;
  timeRange: string;
  requiresPayment: boolean;
  paymentAmount: number;
  cardLabel?: string;
  confirmLabel?: string;
}

@Component({
  selector: 'app-join-confirm-modal',
  templateUrl: './join-confirm-modal.component.html',
  styleUrl: './join-confirm-modal.component.scss',
  standalone: true,
  imports: [IonModal, IonSpinner, DecimalPipe, NgTemplateOutlet, LucideAngularModule],
})
export class JoinConfirmModalComponent {
  readonly data       = input.required<JoinConfirmData>();
  readonly isOpen     = input(false);
  readonly confirming = input(false);
  readonly confirmed  = output<void>();
  readonly cancelled  = output<void>();

  readonly fee = computed(() => Math.round(this.data().paymentAmount * CORTY_FEE * 100) / 100);
  readonly total = computed(() => Math.round((this.data().paymentAmount + this.fee()) * 100) / 100);
}
