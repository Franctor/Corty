import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BookingAdminService } from '../../core/services/booking-admin.service';
import { BookingAdminResponse, BookingAdminDetailResponse, TableColumn } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
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
    SelectComponent,
  ],
})
export class BookingsComponent implements OnInit {
  private service = inject(BookingAdminService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);

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
  }

  private loadBookings(page = 0, search = ''): void {
    this.loading.set(true);
    this.service.getAll(page, 10, search).subscribe({
      next: (data) => { this.bookings.set(data.content); this.totalBookings.set(data.totalElements); this.loading.set(false); },
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
