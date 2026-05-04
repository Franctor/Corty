import { Component, computed, inject, input, output, signal } from '@angular/core';
import { BookingService, MediaService, PublicBookingResponse } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';
import { Router } from '@angular/router';
import { ToastController } from '@ionic/angular/standalone';
import { JoinConfirmModalComponent, JoinConfirmData } from '../../../../../../components/join-confirm-modal/join-confirm-modal.component';

@Component({
  selector: 'app-match-card',
  templateUrl: './match-card.component.html',
  styleUrls: ['./match-card.component.scss'],
  standalone: true,
  imports: [LucideAngularModule, JoinConfirmModalComponent],
})
export class MatchCardComponent {
  private bookingService  = inject(BookingService);
  private mediaService    = inject(MediaService);
  private router          = inject(Router);
  private toastController = inject(ToastController);

  readonly match    = input.required<PublicBookingResponse>();
  readonly joinSent = output<number>();

  readonly confirmOpen     = signal(false);
  readonly requiresPayment = signal(false);
  readonly paymentAmount   = signal(0);
  readonly joining         = signal(false);

  readonly confirmData = computed<JoinConfirmData>(() => ({
    courtName:       this.match().courtName,
    clubName:        this.match().clubName,
    dateLabel:       this.dateLabel(),
    timeRange:       this.timeRange(),
    requiresPayment: this.requiresPayment(),
    paymentAmount:   this.paymentAmount(),
  }));

  readonly logoUrl = computed(() =>
    this.mediaService.getFullUrl(this.match().clubLogoUrl ?? '')
  );

  readonly timeRange = computed(() => {
    const m = this.match();
    return `${m.startTime.slice(0, 5)} – ${m.endTime.slice(0, 5)}`;
  });

  readonly dateLabel = computed(() => {
    const d = this.match().date;
    return new Date(d + 'T00:00:00').toLocaleDateString('es-ES', {
      weekday: 'short', day: 'numeric', month: 'short',
    });
  });

  readonly spotsLeft = computed(() =>
    this.match().maxPlayers - this.match().currentPlayers
  );

  readonly isFull = computed(() => this.spotsLeft() <= 0);

  readonly pricePerPlayerLabel = computed(() => this.formatPrice(this.match().pricePerPlayer));
  readonly totalPriceLabel     = computed(() => this.formatPrice(this.match().totalPrice));

  private formatPrice(value: number): string {
    return Number.isInteger(value) ? `${value}` : value.toFixed(2);
  }

  readonly distanceLabel = computed(() => {
    const km = this.match().distanceKm;
    if (km == null) return null;
    return km < 1 ? `${Math.round(km * 1000)} m` : `${km.toFixed(1)} km`;
  });

  openJoinFlow(event: Event): void {
    event.stopPropagation();
    this.bookingService.joinPaymentCheck(this.match().id).subscribe({
      next: (check) => {
        if (check.requiresPayment && !check.hasPaymentMethod) {
          const returnUrl = this.router.url;
          void this.router.navigate(['/profile/settings/payment-method'], { queryParams: { returnUrl } });
          return;
        }
        this.requiresPayment.set(check.requiresPayment);
        this.paymentAmount.set(check.amount);
        this.confirmOpen.set(true);
      },
      error: (err) => this.showToast(err?.error?.message ?? 'Error al verificar el pago'),
    });
  }

  confirmJoin(): void {
    this.joining.set(true);
    this.bookingService.sendJoinRequest(this.match().id).subscribe({
      next: () => {
        this.confirmOpen.set(false);
        this.joining.set(false);
        this.joinSent.emit(this.match().id);
      },
      error: (err) => {
        this.confirmOpen.set(false);
        this.joining.set(false);
        this.showToast(err?.error?.message ?? 'No se pudo enviar la solicitud');
      },
    });
  }

  cancelJoin(): void {
    this.confirmOpen.set(false);
  }

  goToDetail(): void {
    this.router.navigate(['/bookings', this.match().id]);
  }

  private async showToast(message: string): Promise<void> {
    const toast = await this.toastController.create({
      message,
      duration: 3500,
      position: 'bottom',
      color: 'danger',
    });
    await toast.present();
  }
}
