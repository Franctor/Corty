import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { PageHeaderComponent } from '../../../components/page-header/page-header.component';
import {
  BookingService, CourtDetailResponse, BookingType, PaymentMethod,
  PaymentService, SavedCardResponse,
} from '@frontend/shared-core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';
import { BookingStepDateComponent } from './steps/step-date/step-date.component';
import { BookingStepConfigComponent } from './steps/step-config/step-config.component';
import { BookingStepConfirmComponent } from './steps/step-confirm/step-confirm.component';
import { JoinConfirmModalComponent, JoinConfirmData } from '../../../components/join-confirm-modal/join-confirm-modal.component';

export interface BookingState {
  courtId: number;
  date: string;
  startTime: string;
  endTime: string;
  bookingType: BookingType;
  paymentMethod: PaymentMethod;
  splitPayment: boolean;
  notes: string;
}

@Component({
  selector: 'app-booking',
  templateUrl: './booking.page.html',
  styleUrls: ['./booking.page.scss'],
  standalone: true,
  imports: [
    IonContent, IonSpinner,
    PageHeaderComponent, LucideAngularModule,
    BookingStepDateComponent, BookingStepConfigComponent,
    BookingStepConfirmComponent, JoinConfirmModalComponent,
  ],
})
export class BookingPage {
  private route          = inject(ActivatedRoute);
  private router         = inject(Router);
  private http           = inject(HttpClient);
  private apiUrl         = inject(API_URL);
  private bookingService = inject(BookingService);
  private paymentService = inject(PaymentService);
  private toastCtrl      = inject(ToastController);

  readonly court      = signal<CourtDetailResponse | null>(null);
  readonly loading    = signal(true);
  readonly step       = signal<1 | 2 | 3>(1);
  readonly submitting = signal(false);
  readonly savedCard  = signal<SavedCardResponse | null>(null);
  readonly savedCardLoaded = signal(false);
  readonly payModalOpen = signal(false);

  private createdBookingId: number | null = null;

  readonly state = signal<BookingState>({
    courtId: 0,
    date: '',
    startTime: '',
    endTime: '',
    bookingType: 'PRIVATE',
    paymentMethod: 'CREDIT_CARD',
    splitPayment: true,
    notes: '',
  });

  readonly payModalData = computed<JoinConfirmData>(() => {
    const s = this.state();
    const c = this.court();
    const card = this.savedCard();
    const durationMin = (() => {
      if (!s.startTime || !s.endTime) return 0;
      const [sh, sm] = s.startTime.split(':').map(Number);
      const [eh, em] = s.endTime.split(':').map(Number);
      return (eh * 60 + em) - (sh * 60 + sm);
    })();
    const totalPrice = c ? (c.pricePerHour * durationMin) / 60 : 0;
    const cardLabel = card ? `${card.brand.charAt(0).toUpperCase() + card.brand.slice(1)} ···· ${card.last4}` : '';
    return {
      courtName:       c?.name ?? '',
      clubName:        c?.clubName ?? '',
      dateLabel:       s.date ? new Date(s.date + 'T00:00:00').toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short' }) : '',
      timeRange:       s.startTime && s.endTime ? `${s.startTime.slice(0, 5)} – ${s.endTime.slice(0, 5)}` : '',
      requiresPayment: s.paymentMethod === 'CREDIT_CARD',
      paymentAmount:   totalPrice,
      cardLabel,
      confirmLabel:    'Confirmar y pagar',
    };
  });

  readonly stepTitle = computed(() => {
    switch (this.step()) {
      case 1: return 'Seleccionar horario';
      case 2: return 'Configurar reserva';
      case 3: return 'Confirmar reserva';
    }
  });

  private static readonly DRAFT_KEY = 'booking_draft';

  constructor() {
    const courtId = Number(this.route.snapshot.paramMap.get('courtId'));

    // Restaurar borrador si venimos de añadir tarjeta
    const raw = sessionStorage.getItem(BookingPage.DRAFT_KEY);
    if (raw) {
      try {
        const draft = JSON.parse(raw) as { state: BookingState; step: 1 | 2 | 3 };
        if (draft.state.courtId === courtId) {
          this.state.set(draft.state);
          this.step.set(draft.step);
        }
      } catch { /* ignorar */ }
      sessionStorage.removeItem(BookingPage.DRAFT_KEY);
    } else {
      this.state.update(s => ({ ...s, courtId }));
    }

    this.http.get<CourtDetailResponse>(`${this.apiUrl}/courts/${courtId}`).subscribe({
      next: c  => { this.court.set(c); this.loading.set(false); },
      error: () => { this.loading.set(false); },
    });
    this.paymentService.getSavedCard().subscribe({
      next:  card => { this.savedCard.set(card); this.savedCardLoaded.set(true); },
      error: ()   => { this.savedCard.set(null); this.savedCardLoaded.set(true); },
    });
  }

  onDateSelected(slot: { date: string; startTime: string; endTime: string }): void {
    this.state.update(s => ({ ...s, ...slot }));
    this.step.set(2);
  }

  onConfigDone(config: { bookingType: BookingType; paymentMethod: PaymentMethod; splitPayment: boolean; notes: string }): void {
    this.state.update(s => ({ ...s, ...config }));
    this.step.set(3);
  }

  onBack(): void {
    const current = this.step();
    if (current === 1) {
      this.router.navigate(['/explore/court', this.state().courtId]);
    } else {
      this.step.set((current - 1) as 1 | 2 | 3);
    }
  }

  // Llamado desde el botón del step-confirm: abre modal de resumen o redirige si no hay tarjeta
  async onConfirmClick(): Promise<void> {
    const s = this.state();
    if (s.paymentMethod === 'CREDIT_CARD') {
      if (this.savedCardLoaded() && this.savedCard() == null) {
        sessionStorage.setItem(BookingPage.DRAFT_KEY, JSON.stringify({ state: this.state(), step: this.step() }));
        void this.router.navigate(['/profile/settings/payment-method'], { queryParams: { returnUrl: this.router.url } });
        return;
      }
      this.payModalOpen.set(true);
      return;
    }
    await this.onConfirm();
  }

  // Llamado tras confirmar en el modal (o directamente si es pago en efectivo)
  async onConfirm(): Promise<void> {
    this.payModalOpen.set(false);
    const s = this.state();
    this.submitting.set(true);
    this.bookingService.createBooking({
      courtId:       s.courtId,
      date:          s.date,
      startTime:     s.startTime,
      endTime:       s.endTime,
      bookingType:   s.bookingType,
      paymentMethod: s.paymentMethod,
      splitPayment:  s.splitPayment,
      notes:         s.notes || undefined,
    }).subscribe({
      next: async res => {
        this.createdBookingId = res.bookingId;

        if (s.paymentMethod === 'CASH') {
          this.submitting.set(false);
          await this.onPaid();
          return;
        }

        // Cobrar con tarjeta guardada a través del backend
        this.paymentService.createIntent(res.bookingId).subscribe({
          next: async intent => {
            // El webhook de Stripe confirmará la reserva, pero también
            // la confirmamos desde el frontend usando confirmCardPayment off-session
            const { loadStripe } = await import('@stripe/stripe-js');
            const stripe = await loadStripe(intent.publishableKey);
            if (!stripe) {
              this.submitting.set(false);
              await this.showToast('Error al conectar con Stripe.', 'danger');
              return;
            }
            const card = this.savedCard() as SavedCardResponse;
            const { error, paymentIntent } = await stripe.confirmCardPayment(
              intent.clientSecret,
              { payment_method: card.paymentMethodId }
            );
            this.submitting.set(false);
            if (error) {
              await this.showToast(error.message ?? 'Error al procesar el pago.', 'danger');
            } else if (paymentIntent?.status === 'succeeded') {
              await this.onPaid();
            }
          },
          error: async () => {
            this.submitting.set(false);
            await this.showToast('No se pudo iniciar el pago. Inténtalo de nuevo.', 'danger');
          },
        });
      },
      error: async () => {
        this.submitting.set(false);
        await this.showToast('No se pudo crear la reserva. Inténtalo de nuevo.', 'danger');
      },
    });
  }

  async onPaid(): Promise<void> {
    await this.showToast('¡Reserva confirmada!', 'success');
    void this.router.navigate(['/bookings', this.createdBookingId]);
  }

  private async showToast(message: string, color: 'success' | 'danger'): Promise<void> {
    const toast = await this.toastCtrl.create({ message, duration: 2500, color, position: 'top' });
    await toast.present();
  }
}
