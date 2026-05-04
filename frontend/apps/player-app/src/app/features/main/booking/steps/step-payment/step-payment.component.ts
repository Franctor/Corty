import {
  Component, input, output, signal, OnDestroy, ElementRef, ViewChild,
  afterNextRender, Injector, inject
} from '@angular/core';
import { IonSpinner } from '@ionic/angular/standalone';
import { loadStripe, Stripe, StripeCardElement } from '@stripe/stripe-js';
import { PaymentIntentResponse, SavedCardResponse } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-booking-step-payment',
  templateUrl: './step-payment.component.html',
  styleUrls: ['./step-payment.component.scss'],
  standalone: true,
  imports: [IonSpinner, LucideAngularModule],
})
export class BookingStepPaymentComponent implements OnDestroy {
  @ViewChild('cardMount', { static: false }) cardMountRef?: ElementRef<HTMLDivElement>;

  readonly intent    = input.required<PaymentIntentResponse>();
  readonly savedCard = input<SavedCardResponse | null>(null);
  readonly paid      = output<void>();

  readonly loading = signal(true);
  readonly paying  = signal(false);
  readonly error   = signal<string | null>(null);

  private stripe?: Stripe;
  private card?: StripeCardElement;
  private injector = inject(Injector);

  get hasSavedCard(): boolean {
    return this.savedCard() != null;
  }

  get cardBrandLabel(): string {
    const card = this.savedCard();
    if (!card) return '';
    const brand = card.brand.charAt(0).toUpperCase() + card.brand.slice(1);
    return `${brand} ···· ${card.last4}`;
  }

  get cardExpiry(): string {
    const card = this.savedCard();
    if (!card) return '';
    return `${String(card.expMonth).padStart(2, '0')}/${String(card.expYear).slice(-2)}`;
  }

  constructor() {
    afterNextRender(() => {
      void this.initStripe();
    }, { injector: this.injector });
  }

  private async initStripe(): Promise<void> {
    const stripe = await loadStripe(this.intent().publishableKey);
    if (!stripe) {
      this.error.set('No se pudo cargar Stripe');
      this.loading.set(false);
      return;
    }
    this.stripe = stripe;

    if (this.hasSavedCard) {
      this.loading.set(false);
      return;
    }

    if (!this.cardMountRef) {
      this.error.set('Error al montar el formulario de pago');
      this.loading.set(false);
      return;
    }

    const elements = stripe.elements();
    this.card = elements.create('card', {
      style: {
        base: {
          fontFamily: 'Nunito, sans-serif',
          fontSize: '16px',
          color: '#1a1a2e',
          '::placeholder': { color: '#9ca3af' },
        },
        invalid: { color: '#ef4444' },
      },
    });
    this.card.mount(this.cardMountRef.nativeElement);
    this.card.on('change', event => {
      this.error.set(event.error ? event.error.message : null);
    });
    this.loading.set(false);
  }

  ngOnDestroy(): void {
    this.card?.destroy();
  }

  async pay(): Promise<void> {
    if (!this.stripe) return;
    this.paying.set(true);
    this.error.set(null);

    if (this.hasSavedCard) {
      const { error, paymentIntent } = await this.stripe.confirmCardPayment(
        this.intent().clientSecret,
        { payment_method: this.savedCard()!.paymentMethodId }
      );
      if (error) {
        this.error.set(error.message ?? 'Error al procesar el pago');
        this.paying.set(false);
      } else if (paymentIntent?.status === 'succeeded') {
        this.paid.emit();
      }
    } else {
      const { error, paymentIntent } = await this.stripe.confirmCardPayment(
        this.intent().clientSecret,
        { payment_method: { card: this.card! } }
      );
      if (error) {
        this.error.set(error.message ?? 'Error al procesar el pago');
        this.paying.set(false);
      } else if (paymentIntent?.status === 'succeeded') {
        this.paid.emit();
      }
    }
  }
}
