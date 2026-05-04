import { Component, ElementRef, inject, signal, ViewChild } from '@angular/core';
import { TitleCasePipe } from '@angular/common';
import { IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { ActivatedRoute, Router } from '@angular/router';
import { PageHeaderComponent } from '../../../../../components/page-header/page-header.component';
import { PaymentService, SavedCardResponse } from '@frontend/shared-core';

declare const Stripe: any;

@Component({
  selector: 'app-payment-method',
  templateUrl: './payment-method.page.html',
  styleUrl: './payment-method.page.scss',
  standalone: true,
  imports: [IonContent, IonSpinner, LucideAngularModule, PageHeaderComponent, TitleCasePipe],
})
export class PaymentMethodPage {
  private paymentService = inject(PaymentService);
  private toastCtrl      = inject(ToastController);
  private route          = inject(ActivatedRoute);
  private router         = inject(Router);

  private returnUrl: string | null = null;

  @ViewChild('cardElement') cardElementRef!: ElementRef;

  readonly loading   = signal(true);
  readonly saving    = signal(false);
  readonly removing  = signal(false);
  readonly savedCard = signal<SavedCardResponse | null>(null);
  readonly showForm  = signal(false);

  private stripe: any;
  private elements: any;
  private cardElement: any;

  ionViewWillEnter(): void {
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    this.showForm.set(false);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.paymentService.getSavedCard().subscribe({
      next: card => { this.savedCard.set(card); this.loading.set(false); },
      error: () => { this.savedCard.set(null); this.loading.set(false); },
    });
  }

  openForm(): void {
    this.showForm.set(true);
    this.paymentService.createSetupIntent().subscribe({
      next: ({ clientSecret, publishableKey }) => {
        this.mountStripe(publishableKey, clientSecret);
      },
      error: async () => {
        this.showForm.set(false);
        const t = await this.toastCtrl.create({ message: 'No se pudo iniciar el formulario', duration: 2500, color: 'danger', position: 'top' });
        await t.present();
      },
    });
  }

  private mountStripe(publishableKey: string, clientSecret: string): void {
    this.loadStripeScript().then(() => {
      this.stripe   = Stripe(publishableKey);
      this.elements = this.stripe.elements({
        clientSecret,
        appearance: { theme: 'stripe' },
      });
      this.cardElement = this.elements.create('payment', {
        wallets: { applePay: 'never', googlePay: 'never' },
      });
      setTimeout(() => this.cardElement.mount(this.cardElementRef.nativeElement), 100);
    });
  }

  async confirmCard(): Promise<void> {
    if (this.saving() || !this.stripe || !this.elements) return;
    this.saving.set(true);

    const result = await this.stripe.confirmSetup({
      elements: this.elements,
      confirmParams: { return_url: window.location.href },
      redirect: 'if_required',
    });

    if (result.error) {
      this.saving.set(false);
      const t = await this.toastCtrl.create({ message: result.error.message ?? 'Error al guardar', duration: 3000, color: 'danger', position: 'top' });
      await t.present();
      return;
    }

    const pmId = result.setupIntent?.payment_method;
    if (pmId) {
      this.paymentService.savePaymentMethod(pmId).subscribe({
        next: async () => {
          this.saving.set(false);
          this.showForm.set(false);
          const t = await this.toastCtrl.create({ message: 'Tarjeta guardada', duration: 2000, color: 'success', position: 'top' });
          await t.present();
          if (this.returnUrl) {
            void this.router.navigateByUrl(this.returnUrl);
          } else {
            this.load();
          }
        },
        error: async () => {
          this.saving.set(false);
          const t = await this.toastCtrl.create({ message: 'No se pudo guardar la tarjeta', duration: 2500, color: 'danger', position: 'top' });
          await t.present();
        },
      });
    } else {
      this.saving.set(false);
    }
  }

  removeCard(): void {
    if (this.removing()) return;
    this.removing.set(true);
    this.paymentService.deletePaymentMethod().subscribe({
      next: async () => {
        this.removing.set(false);
        this.savedCard.set(null);
        const t = await this.toastCtrl.create({ message: 'Tarjeta eliminada', duration: 2000, color: 'success', position: 'top' });
        await t.present();
      },
      error: async () => {
        this.removing.set(false);
        const t = await this.toastCtrl.create({ message: 'No se pudo eliminar', duration: 2500, color: 'danger', position: 'top' });
        await t.present();
      },
    });
  }

  cancelForm(): void {
    this.showForm.set(false);
    if (this.cardElement) { this.cardElement.destroy(); this.cardElement = null; }
    this.elements = null;
  }

  brandIcon(brand: string): string {
    const map: Record<string, string> = { visa: 'visa', mastercard: 'mastercard', amex: 'amex' };
    return map[brand.toLowerCase()] ?? 'credit-card';
  }

  private loadStripeScript(): Promise<void> {
    if ((window as any).Stripe) return Promise.resolve();
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://js.stripe.com/v3/';
      script.onload = () => resolve();
      script.onerror = reject;
      document.head.appendChild(script);
    });
  }
}
