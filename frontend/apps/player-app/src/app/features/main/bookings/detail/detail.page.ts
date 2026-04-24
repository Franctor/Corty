import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  IonContent, IonIcon, IonSpinner,
  ToastController,
} from '@ionic/angular/standalone';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';
import { addIcons } from 'ionicons';
import {
  locationOutline, peopleOutline, trophyOutline,
  calendarNumberOutline, closeCircleOutline,
} from 'ionicons/icons';
import { BookingService, BookingDetailResponse, MediaUrlPipe } from '@frontend/shared-core';
import { BookingStatusBadgeComponent } from '../components/booking-status-badge/booking-status-badge.component';
import { BookingInfoGridComponent } from '../components/booking-info-grid/booking-info-grid.component';
import { BookingParticipantItemComponent } from '../components/booking-participant-item/booking-participant-item.component';
import { BookingPriceBreakdownComponent } from '../components/booking-price-breakdown/booking-price-breakdown.component';
import { ConfirmSheetComponent } from '../../../../components/confirm-sheet/confirm-sheet.component';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.page.html',
  styleUrl: './detail.page.scss',
  standalone: true,
  imports: [
    IonContent, IonIcon, IonSpinner,
    PageHeaderComponent,
    BookingStatusBadgeComponent,
    BookingInfoGridComponent,
    BookingParticipantItemComponent,
    BookingPriceBreakdownComponent,
    ConfirmSheetComponent,
    MediaUrlPipe,
  ],
})
export class DetailPage {
  private route          = inject(ActivatedRoute);
  private router         = inject(Router);
  private bookingService = inject(BookingService);
  private toastCtrl      = inject(ToastController);

  readonly booking      = signal<BookingDetailResponse | null>(null);
  readonly loading      = signal(true);
  readonly error        = signal<string | null>(null);
  readonly cancelling   = signal(false);
  readonly showConfirm  = signal(false);

  readonly isPast = computed(() => {
    const s = this.booking()?.bookingStatus;
    return s === 'COMPLETED' || s === 'CANCELLED';
  });

  readonly isOwner = computed(() => this.booking()?.currentUserOwner ?? false);

  readonly durationMin = computed(() => {
    const booking = this.booking();
    let durationMinutes = 0;
    if (booking) {
      const [startHour, startMin] = booking.startTime.split(':').map(Number);
      const [endHour, endMin] = booking.endTime.split(':').map(Number);
      durationMinutes = (endHour * 60 + endMin) - (startHour * 60 + startMin);
    }
    return durationMinutes;
  });

  readonly confirmTitle = computed(() =>
    this.isOwner() ? 'Cancelar reserva' : 'Abandonar reserva'
  );

  readonly confirmMessage = computed(() => {
    if (this.isOwner()) {
      return 'Se cancelará la reserva para todos los participantes. La penalización de karma depende del tiempo restante.';
    }
    const splitPayment = this.booking()?.splitPayment ?? true;
    return splitPayment
      ? 'Perderás tu plaza en esta reserva. La penalización de karma depende del tiempo restante.'
      : 'Perderás tu plaza en esta reserva. El propietario pagó la reserva completa, no se aplica reembolso.';
  });

  readonly confirmLabel = computed(() =>
    this.isOwner() ? 'Sí, cancelar' : 'Sí, abandonar'
  );

  private bookingId!: number;

  constructor() {
    addIcons({
      locationOutline, peopleOutline, trophyOutline,
      calendarNumberOutline, closeCircleOutline,
    });

    this.bookingId = Number(this.route.snapshot.paramMap.get('id'));
    this.bookingService.getBookingDetail(this.bookingId).subscribe({
      next: b  => { this.booking.set(b); this.loading.set(false); },
      error: () => { this.error.set('No se pudo cargar la reserva.'); this.loading.set(false); },
    });
  }

  onAddToCalendar(): void {
    const b = this.booking();
    if (!b) return;
    const start    = b.date.replace(/-/g, '') + 'T' + b.startTime.replace(/:/g, '').slice(0, 6);
    const end      = b.date.replace(/-/g, '') + 'T' + b.endTime.replace(/:/g, '').slice(0, 6);
    const title    = encodeURIComponent(`${b.sport} - ${b.clubName}`);
    const details  = encodeURIComponent(`${b.courtName} · ${b.bookingType === 'PRIVATE' ? 'Reserva Privada' : 'Reserva Pública'}`);
    const location = encodeURIComponent(b.clubAddress ?? '');
    window.open(
      `https://calendar.google.com/calendar/render?action=TEMPLATE&text=${title}&dates=${start}/${end}&details=${details}&location=${location}`,
      '_blank', 'noopener'
    );
  }

  onCancelClick(): void {
    this.showConfirm.set(true);
  }

  onConfirmed(): void {
    this.showConfirm.set(false);
    this.cancelling.set(true);

    const action$ = this.isOwner()
      ? this.bookingService.cancelBooking(this.bookingId)
      : this.bookingService.leaveBooking(this.bookingId);

    action$.subscribe({
      next: async result => {
        this.cancelling.set(false);
        const toast = await this.toastCtrl.create({
          message: result.message,
          duration: 4000,
          color: result.karmaDeducted > 0 ? 'warning' : 'success',
          position: 'top',
        });
        await toast.present();
        this.router.navigate(['/main/bookings']);
      },
      error: async err => {
        this.cancelling.set(false);
        const toast = await this.toastCtrl.create({
          message: err?.error?.message ?? 'No se pudo cancelar la reserva.',
          duration: 3000,
          color: 'danger',
          position: 'top',
        });
        await toast.present();
      },
    });
  }

  getMapUrl(lat: number | null, lng: number | null): string | null {
    return lat && lng ? `https://maps.google.com/?q=${lat},${lng}` : null;
  }
}
