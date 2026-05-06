import { Component, inject, signal, computed, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BookingAdminService } from '../../core/services/booking-admin.service';
import { ClubAdminService } from '../../core/services/club-admin.service';
import { OrgAdminService } from '../../core/services/org-admin.service';
import { AuthService } from '@frontend/shared-auth';
import { BookingAdminResponse, BookingAdminDetailResponse, CourtAdminResponse, TableColumn } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { CourtSchedulePickerComponent, ScheduleSelection } from '../../shared/components/court-schedule-picker/court-schedule-picker.component';
import { ToastService } from '../../shared/services/toast.service';
import { SelectComponent, SelectOption } from '@frontend/shared-ui';

@Component({
  selector: 'app-bookings',
  templateUrl: 'bookings.component.html',
  styleUrl: 'bookings.component.scss',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    AdminPageHeaderComponent,
    AdminTableComponent,
    AdminModalComponent,
    CourtSchedulePickerComponent,
    SelectComponent,
  ],
})
export class BookingsComponent implements OnInit, OnDestroy {
  private subs: Subscription[] = [];
  private service    = inject(BookingAdminService);
  private clubService = inject(ClubAdminService);
  private orgService = inject(OrgAdminService);
  private auth       = inject(AuthService);
  private toast      = inject(ToastService);
  private fb         = inject(FormBuilder);

  readonly isSuperAdmin = computed(() => this.auth.getRole() === 'SUPERADMIN' || this.auth.getRole() === 'ADMIN');
  readonly isOrg        = computed(() => this.auth.getRole() === 'ORGANIZATION');

  // ── Create presencial ────────────────────────────────────────────────────
  readonly showCreateModal  = signal(false);
  readonly orgs             = signal<{ value: number; label: string }[]>([]);
  readonly clubs            = signal<{ value: number; label: string }[]>([]);
  readonly courts           = signal<SelectOption<number>[]>([]);
  private courtsData: CourtAdminResponse[] = [];
  readonly creating          = signal(false);
  readonly selectedCourtId   = signal<number | null>(null);
  readonly selectedCourtSlot = signal<number>(60);
  private scheduleSelection: ScheduleSelection | null = null;

  readonly paymentOptions: SelectOption<string>[] = [
    { value: 'CASH',        label: 'Efectivo' },
    { value: 'CREDIT_CARD', label: 'Tarjeta física' },
  ];

  readonly createForm: FormGroup = this.fb.group({
    orgId:         [null],
    clubId:        [null, Validators.required],
    courtId:       [null, Validators.required],
    paymentMethod: ['CASH', Validators.required],
    notes:         [''],
  });

  // ── Table ────────────────────────────────────────────────────────────────
  readonly bookings = signal<BookingAdminResponse[]>([]);
  readonly totalBookings = signal<number | null>(null);
  readonly currentPage = signal(0);
  readonly searchQuery = signal('');
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly detail = signal<BookingAdminDetailResponse | null>(null);
  readonly showCancelConfirm = signal(false);

  readonly isCancelled = computed(() => this.detail()?.bookingStatus === 'CANCELLED');

  readonly statusOptions: SelectOption<string>[] = [
    { value: 'PENDING',   label: 'Pendiente' },
    { value: 'CONFIRMED', label: 'Confirmada' },
    { value: 'COMPLETED', label: 'Completada' },
    { value: 'CANCELLED', label: 'Cancelada' },
  ];

  readonly editForm: FormGroup = this.fb.group({
    bookingStatus: [''],
    notes:         [''],
  });

  readonly cancelForm: FormGroup = this.fb.group({
    cancelReason: [''],
  });

  readonly neverDelete = () => false;

  readonly columns: TableColumn<BookingAdminResponse>[] = [
    { key: 'date',      label: 'Fecha', render: (row) => new Date(row.date + 'T00:00:00').toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' }) },
    { key: 'startTime', label: 'Hora',  render: (row) => `${row.startTime.slice(0, 5)} – ${row.endTime.slice(0, 5)}` },
    { key: 'clubName',         label: 'Club' },
    { key: 'courtName',        label: 'Pista' },
    { key: 'ownerUsername',    label: 'Propietario' },
    { key: 'bookingStatus',    label: 'Estado', render: (row) => this.statusLabel(row.bookingStatus) },
    { key: 'totalPrice',       label: 'Precio', render: (row) => `${row.totalPrice} €` },
    { key: 'participantCount', label: 'Jugadores' },
  ];

  ngOnInit(): void {
    this.loadBookings();
    if (this.isSuperAdmin()) {
      this.orgService.getAll().subscribe(orgs =>
        this.orgs.set(orgs.map(o => ({ value: o.id, label: o.businessName })))
      );
    } else {
      this.clubService.getAll().subscribe(clubs =>
        this.clubs.set(clubs.map(c => ({ value: c.id, label: c.name })))
      );
    }
  }

  openCreate(): void {
    this.createForm.reset({ paymentMethod: 'CASH' });
    this.courts.set([]);
    this.courtsData = [];
    this.selectedCourtId.set(null);
    this.scheduleSelection = null;
    if (this.isSuperAdmin()) this.clubs.set([]);
    this.showCreateModal.set(true);

    if (this.isSuperAdmin()) {
      this.subs.push(
        this.createForm.get('orgId')!.valueChanges.subscribe((orgId: number | null) => {
          if (orgId == null) return;
          this.createForm.patchValue({ clubId: null, courtId: null }, { emitEvent: false });
          this.courts.set([]);
          this.clubService.getByOrg(orgId).subscribe(clubs =>
            this.clubs.set(clubs.map(c => ({ value: c.id, label: c.name })))
          );
        }),
      );
    }

    this.subs.push(
      this.createForm.get('clubId')!.valueChanges.subscribe((clubId: number | null) => {
        if (clubId == null) return;
        this.createForm.patchValue({ courtId: null }, { emitEvent: false });
        this.selectedCourtId.set(null);
        this.scheduleSelection = null;
        this.service.getCourtsByClub(clubId).subscribe(courts => {
          this.courtsData = courts;
          this.courts.set(courts.map(c => ({ value: c.id, label: `${c.name} (${c.sportName})` })));
        });
      }),
      this.createForm.get('courtId')!.valueChanges.subscribe((courtId: number | null) => {
        this.selectedCourtId.set(courtId);
        this.scheduleSelection = null;
        const court = this.courtsData.find(c => c.id === courtId);
        this.selectedCourtSlot.set(court?.slotDurationMinutes ?? 60);
      }),
    );
  }

  onScheduleChange(sel: ScheduleSelection | null): void {
    this.scheduleSelection = sel;
  }

  submitCreate(): void {
    if (this.createForm.invalid || this.creating()) {
      this.createForm.markAllAsTouched();
      return;
    }
    if (!this.scheduleSelection) {
      this.toast.error('Selecciona un horario disponible');
      return;
    }
    const v = this.createForm.value;
    this.creating.set(true);
    this.service.create({
      courtId:       v.courtId,
      date:          this.scheduleSelection.date,
      startTime:     this.scheduleSelection.startTime,
      endTime:       this.scheduleSelection.endTime,
      paymentMethod: v.paymentMethod,
      notes:         v.notes || undefined,
    }).subscribe({
      next: (created) => {
        this.creating.set(false);
        this.showCreateModal.set(false);
        this.loadBookings(this.currentPage(), this.searchQuery());
        this.toast.success('Reserva presencial creada');
      },
      error: (err) => {
        this.creating.set(false);
        this.toast.error(err.error?.message ?? 'Error al crear la reserva');
      },
    });
  }

  closeCreate(): void {
    this.showCreateModal.set(false);
    this.subs.forEach(s => s.unsubscribe());
    this.subs = [];
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  private loadBookings(page = 0, search = ''): void {
    this.loading.set(true);
    this.service.getAll(page, 10, search).subscribe({
      next: (data) => { this.bookings.set(data.content); this.totalBookings.set(data.page.totalElements); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Error al cargar las reservas'); },
    });
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadBookings(page, this.searchQuery());
  }

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    this.loadBookings(0, query);
  }

  openDetail(booking: BookingAdminResponse): void {
    this.service.getById(booking.id).subscribe({
      next: (detailData) => {
        this.detail.set(detailData);
        this.editForm.patchValue({
          bookingStatus: detailData.bookingStatus,
          notes: detailData.notes ?? '',
        });
      },
      error: () => this.toast.error('Error al cargar el detalle'),
    });
  }

  closeDetail(): void {
    this.detail.set(null);
    this.showCancelConfirm.set(false);
    this.cancelForm.reset();
  }

  saveChanges(): void {
    const currentDetail = this.detail();
    if (!currentDetail || this.saving()) return;

    const formValue = this.editForm.value;
    const statusChanged = formValue.bookingStatus !== currentDetail.bookingStatus;
    const isCancelling = statusChanged && formValue.bookingStatus === 'CANCELLED';

    if (isCancelling) {
      this.showCancelConfirm.set(true);
    } else {
      this.saving.set(true);
      this.service.update(currentDetail.id, {
        bookingStatus: statusChanged ? formValue.bookingStatus : undefined,
        notes: formValue.notes,
      }).subscribe({
        next: (updated) => this.onUpdateSuccess(updated),
        error: () => { this.saving.set(false); this.toast.error('Error al guardar los cambios'); },
      });
    }
  }

  confirmCancel(): void {
    const currentDetail = this.detail();
    if (!currentDetail) return;
    this.saving.set(true);
    this.showCancelConfirm.set(false);
    this.service.update(currentDetail.id, {
      bookingStatus: 'CANCELLED',
      notes: this.editForm.value.notes,
      cancelReason: this.cancelForm.value.cancelReason ?? '',
    }).subscribe({
      next: (updated) => { this.cancelForm.reset(); this.onUpdateSuccess(updated); },
      error: () => { this.saving.set(false); this.toast.error('Error al cancelar la reserva'); },
    });
  }

  private onUpdateSuccess(updated: BookingAdminDetailResponse): void {
    this.detail.set(updated);
    this.editForm.patchValue({ bookingStatus: updated.bookingStatus, notes: updated.notes ?? '' });
    this.bookings.update((list) => list.map((item) =>
      item.id === updated.id ? { ...item, bookingStatus: updated.bookingStatus, notes: updated.notes } : item
    ));
    this.saving.set(false);
    this.toast.success('Reserva actualizada');
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      PENDING:   'Pendiente',
      CONFIRMED: 'Confirmada',
      COMPLETED: 'Completada',
      CANCELLED: 'Cancelada',
    };
    return labels[status] ?? status;
  }
}
