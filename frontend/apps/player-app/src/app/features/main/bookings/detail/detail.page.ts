import { Component, computed, effect, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JoinConfirmModalComponent, JoinConfirmData } from '../../../../components/join-confirm-modal/join-confirm-modal.component';
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
import { BookingService, BookingDetailResponse, JoinRequestResponse, MediaUrlPipe, NotificationService } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';
import { BookingStatusBadgeComponent } from '../components/booking-status-badge/booking-status-badge.component';
import { BookingInfoGridComponent } from '../components/booking-info-grid/booking-info-grid.component';
import { BookingParticipantItemComponent } from '../components/booking-participant-item/booking-participant-item.component';
import { BookingPriceBreakdownComponent } from '../components/booking-price-breakdown/booking-price-breakdown.component';
import { ConfirmSheetComponent } from '../../../../components/confirm-sheet/confirm-sheet.component';
import { BookingResultModalComponent } from '../components/booking-result-modal/booking-result-modal.component';

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
    BookingResultModalComponent,
    JoinConfirmModalComponent,
    MediaUrlPipe, LucideAngularModule,
  ],
})
export class DetailPage {
  private route                = inject(ActivatedRoute);
  private router               = inject(Router);
  private bookingService       = inject(BookingService);
  private toastCtrl            = inject(ToastController);
  private notificationService  = inject(NotificationService);

  readonly booking      = signal<BookingDetailResponse | null>(null);
  readonly loading      = signal(true);
  readonly error        = signal<string | null>(null);
  readonly cancelling   = signal(false);
  readonly showConfirm  = signal(false);
  readonly showResultModal = signal(false);
  readonly requesting      = signal(false);
  readonly joinRequests    = signal<JoinRequestResponse[]>([]);
  readonly joinModalOpen   = signal(false);
  readonly joinRequiresPayment = signal(false);
  readonly joinPaymentAmount   = signal(0);

  readonly joinConfirmData = computed<JoinConfirmData>(() => {
    const b = this.booking();
    return {
      courtName:       b?.courtName ?? '',
      clubName:        b?.clubName ?? '',
      dateLabel:       b ? new Date(b.date + 'T00:00:00').toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', month: 'short' }) : '',
      timeRange:       b ? `${b.startTime.slice(0, 5)} – ${b.endTime.slice(0, 5)}` : '',
      requiresPayment: this.joinRequiresPayment(),
      paymentAmount:   this.joinPaymentAmount(),
    };
  });
  readonly processingId  = signal<number | null>(null); // requestId being accepted/rejected

  readonly isPast = computed(() => {
    const s = this.booking()?.bookingStatus;
    return s === 'COMPLETED' || s === 'CANCELLED';
  });

  readonly canRegisterResult = computed(() => {
    const b = this.booking();
    return b?.bookingStatus === 'COMPLETED' && b.currentUserOwner && !b.result;
  });

  readonly isOwner       = computed(() => this.booking()?.currentUserOwner ?? false);
  readonly isParticipant = computed(() => this.booking()?.currentUserParticipant ?? false);
  readonly isPublic      = computed(() => this.booking()?.bookingType === 'PUBLIC');
  readonly joinStatus    = computed(() => this.booking()?.myJoinRequestStatus ?? null);

  readonly mySplitPrice = computed(() => {
    const booking = this.booking();
    if (!booking) return null;
    if (!booking.splitPayment) return null;
    return booking.participants.find(p => p.currentUser)?.splitPrice ?? null;
  });

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

  protected bookingId!: number;

  constructor() {
    addIcons({
      locationOutline, peopleOutline, trophyOutline,
      calendarNumberOutline, closeCircleOutline,
    });

    this.bookingId = Number(this.route.snapshot.paramMap.get('id'));

    effect(() => {
      const notif = this.notificationService.lastEvent();
      if (!notif || Number(notif.referenceId) !== this.bookingId) return;

      if (notif.type === 'JOIN_REQUEST' && this.isOwner()) {
        this.loadJoinRequests();
      } else if (notif.type === 'JOIN_ACCEPTED' || notif.type === 'PARTICIPANT_JOINED' || notif.type === 'PARTICIPANT_LEFT' || notif.type === 'BOOKING_CANCELLED') {
        this.loadBooking();
      } else if (notif.type === 'JOIN_REJECTED') {
        this.booking.update(b => b ? { ...b, myJoinRequestStatus: 'REJECTED' } : b);
      }
    });
  }

  ionViewWillEnter(): void {
    this.loadBooking();
  }

  private loadBooking(): void {
    this.bookingService.getBookingDetail(this.bookingId).subscribe({
      next: b => {
        this.booking.set(b);
        this.loading.set(false);
        if (b.currentUserOwner && b.bookingType === 'PUBLIC') {
          this.loadJoinRequests();
        }
      },
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

  private loadJoinRequests(): void {
    this.bookingService.getJoinRequests(this.bookingId).subscribe({
      next: reqs => this.joinRequests.set(reqs),
    });
  }

  onAccept(requestId: number): void {
    if (this.processingId() != null) return;
    this.processingId.set(requestId);
    this.bookingService.acceptJoinRequest(this.bookingId, requestId).subscribe({
      next: async () => {
        this.processingId.set(null);
        this.joinRequests.update(rs => rs.filter(r => r.id !== requestId));
        // Recargar booking para actualizar participantes y splitPrice
        this.bookingService.getBookingDetail(this.bookingId).subscribe({
          next: b => this.booking.set(b),
        });
        const toast = await this.toastCtrl.create({
          message: 'Jugador aceptado.', duration: 2500, color: 'success', position: 'top',
        });
        await toast.present();
      },
      error: async err => {
        this.processingId.set(null);
        const toast = await this.toastCtrl.create({
          message: err?.error?.message ?? 'No se pudo aceptar.', duration: 3000, color: 'danger', position: 'top',
        });
        await toast.present();
      },
    });
  }

  onReject(requestId: number): void {
    if (this.processingId() != null) return;
    this.processingId.set(requestId);
    this.bookingService.rejectJoinRequest(this.bookingId, requestId).subscribe({
      next: async () => {
        this.processingId.set(null);
        this.joinRequests.update(rs => rs.filter(r => r.id !== requestId));
        const toast = await this.toastCtrl.create({
          message: 'Petición rechazada.', duration: 2500, color: 'medium', position: 'top',
        });
        await toast.present();
      },
      error: async err => {
        this.processingId.set(null);
        const toast = await this.toastCtrl.create({
          message: err?.error?.message ?? 'No se pudo rechazar.', duration: 3000, color: 'danger', position: 'top',
        });
        await toast.present();
      },
    });
  }

  onJoinRequest(): void {
    if (this.requesting()) return;
    this.bookingService.joinPaymentCheck(this.bookingId).subscribe({
      next: (check) => {
        if (check.requiresPayment && !check.hasPaymentMethod) {
          const returnUrl = this.router.url;
          void this.router.navigate(['/profile/settings/payment-method'], { queryParams: { returnUrl } });
          return;
        }
        this.joinRequiresPayment.set(check.requiresPayment);
        this.joinPaymentAmount.set(check.amount);
        this.joinModalOpen.set(true);
      },
      error: () => this.sendJoin(),
    });
  }

  confirmJoinFromModal(): void {
    this.joinModalOpen.set(false);
    this.sendJoin();
  }

  private sendJoin(): void {
    this.requesting.set(true);
    this.bookingService.sendJoinRequest(this.bookingId).subscribe({
      next: () => {
        this.requesting.set(false);
        this.booking.update(b => b ? { ...b, myJoinRequestStatus: 'PENDING' } : b);
      },
      error: async err => {
        this.requesting.set(false);
        const toast = await this.toastCtrl.create({
          message: err?.error?.message ?? 'No se pudo enviar la petición.',
          duration: 3000, color: 'danger', position: 'top',
        });
        await toast.present();
      },
    });
  }

  onCancelJoinRequest(): void {
    if (this.requesting()) return;
    this.requesting.set(true);
    this.bookingService.cancelJoinRequest(this.bookingId).subscribe({
      next: () => {
        this.requesting.set(false);
        this.booking.update(b => b ? { ...b, myJoinRequestStatus: null } : b);
      },
      error: async err => {
        this.requesting.set(false);
        const toast = await this.toastCtrl.create({
          message: err?.error?.message ?? 'No se pudo cancelar la petición.',
          duration: 3000, color: 'danger', position: 'top',
        });
        await toast.present();
      },
    });
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
        this.router.navigate(['/bookings']);
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

  onRegisterResult(): void {
    this.showResultModal.set(true);
  }

  onResultSaved(): void {
    this.showResultModal.set(false);
    this.loadBooking();
    this.toastCtrl.create({
      message: 'Resultado registrado correctamente.',
      duration: 3000,
      color: 'success',
      position: 'top',
    }).then(t => t.present());
  }
}
