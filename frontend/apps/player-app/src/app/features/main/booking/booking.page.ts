import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { PageHeaderComponent } from '../../../components/page-header/page-header.component';
import { BookingService, CourtDetailResponse, SlotResponse, BookingType } from '@frontend/shared-core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';
import { BookingStepDateComponent } from './steps/step-date/step-date.component';
import { BookingStepConfigComponent } from './steps/step-config/step-config.component';
import { BookingStepConfirmComponent } from './steps/step-confirm/step-confirm.component';

export interface BookingState {
  courtId: number;
  date: string;
  startTime: string;
  endTime: string;
  bookingType: BookingType;
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
    BookingStepDateComponent, BookingStepConfigComponent, BookingStepConfirmComponent,
  ],
})
export class BookingPage {
  private route          = inject(ActivatedRoute);
  private router         = inject(Router);
  private http           = inject(HttpClient);
  private apiUrl         = inject(API_URL);
  private bookingService = inject(BookingService);
  private toastCtrl      = inject(ToastController);

  readonly court     = signal<CourtDetailResponse | null>(null);
  readonly loading   = signal(true);
  readonly step      = signal<1 | 2 | 3>(1);
  readonly submitting = signal(false);

  readonly state = signal<BookingState>({
    courtId: 0,
    date: '',
    startTime: '',
    endTime: '',
    bookingType: 'PRIVATE',
    splitPayment: true,
    notes: '',
  });

  readonly stepTitle = computed(() => {
    switch (this.step()) {
      case 1: return 'Seleccionar horario';
      case 2: return 'Configurar reserva';
      case 3: return 'Confirmar reserva';
    }
  });

  constructor() {
    const courtId = Number(this.route.snapshot.paramMap.get('courtId'));
    this.state.update(s => ({ ...s, courtId }));
    this.http.get<CourtDetailResponse>(`${this.apiUrl}/courts/${courtId}`).subscribe({
      next: c  => { this.court.set(c); this.loading.set(false); },
      error: () => { this.loading.set(false); },
    });
  }

  onDateSelected(slot: { date: string; startTime: string; endTime: string }): void {
    this.state.update(s => ({ ...s, ...slot }));
    this.step.set(2);
  }

  onConfigDone(config: { bookingType: BookingType; splitPayment: boolean; notes: string }): void {
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

  onConfirm(): void {
    const s = this.state();
    this.submitting.set(true);
    this.bookingService.createBooking({
      courtId:     s.courtId,
      date:        s.date,
      startTime:   s.startTime,
      endTime:     s.endTime,
      bookingType: s.bookingType,
      splitPayment: s.splitPayment,
      notes:       s.notes || undefined,
    }).subscribe({
      next: async res => {
        const toast = await this.toastCtrl.create({
          message: '¡Reserva confirmada!',
          duration: 2000,
          color: 'success',
          position: 'top',
        });
        await toast.present();
        this.router.navigate(['/bookings', res.bookingId]);
      },
      error: async () => {
        this.submitting.set(false);
        const toast = await this.toastCtrl.create({
          message: 'No se pudo crear la reserva. Inténtalo de nuevo.',
          duration: 3000,
          color: 'danger',
          position: 'top',
        });
        await toast.present();
      },
    });
  }
}
