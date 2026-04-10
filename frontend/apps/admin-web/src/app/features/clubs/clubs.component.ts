import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ClubAdminService } from '../../core/services/club-admin.service';
import { ClubRequest, ClubResponse } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { TableColumn } from '@frontend/shared-core';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { getFirstError } from '@frontend/shared-core';
import { ImagePickerComponent, LocationSelectComponent } from '@frontend/shared-ui';
import { ToastService } from '../../shared/services/toast.service';
import { AuthService } from '@frontend/shared-auth';

@Component({
  selector: 'app-clubs',
  templateUrl: 'clubs.component.html',
  styleUrl: 'clubs.component.scss',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    AdminPageHeaderComponent,
    AdminTableComponent,
    AdminModalComponent,
    ConfirmModalComponent,
    ImagePickerComponent,
    LocationSelectComponent,
  ],
})
export class ClubsComponent implements OnInit {
  private service = inject(ClubAdminService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);
  private auth = inject(AuthService);

  protected getFirstError = getFirstError;

  readonly canForceDelete = computed(() => this.auth.hasAuthority('FORCE_DELETE'));

  readonly clubs = signal<ClubResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly showModal = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly deletingItem = signal<ClubResponse | null>(null);
  readonly forceDeleteItem = signal<ClubResponse | null>(null);

  readonly modalTitle = computed(() =>
    this.editingId() ? 'Editar club' : 'Nuevo club'
  );

  readonly columns: TableColumn<ClubResponse>[] = [
    { key: 'name', label: 'Nombre' },
    { key: 'cityName', label: 'Ciudad' },
    { key: 'address', label: 'Dirección' },
    { key: 'phone', label: 'Teléfono' },
    { key: 'contactEmail', label: 'Email de contacto' },
  ];

  readonly form: FormGroup = this.fb.group({
    name:         ['', [Validators.required, Validators.maxLength(50)]],
    description:  [''],
    phone:        ['', [Validators.required, Validators.maxLength(20)]],
    contactEmail: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    address:      ['', [Validators.required, Validators.maxLength(100)]],
    nif:          ['', [Validators.required, Validators.maxLength(9)]],
    cityId:       [null, [Validators.required]],
    logoUrl:      [null],
    geoLat:       [null],
    geoLong:      [null],
  });

  ngOnInit(): void {
    this.loadClubs();
  }

  private loadClubs(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: (data) => { this.clubs.set(data); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Error al cargar los clubs'); },
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.form.reset({ cityId: null, logoUrl: null, geoLat: null, geoLong: null });
    this.showModal.set(true);
  }

  openEdit(club: ClubResponse): void {
    this.editingId.set(club.id);
    this.form.patchValue({ ...club });
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  onSubmit(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const body = this.form.value as ClubRequest;
    const id = this.editingId();
    const req = id ? this.service.update(id, body) : this.service.create(body);

    req.subscribe({
      next: (saved) => {
        this.clubs.update((list) =>
          id ? list.map((c) => (c.id === id ? saved : c)) : [...list, saved]
        );
        this.saving.set(false);
        this.showModal.set(false);
        this.toast.success(id ? 'Club actualizado' : 'Club creado');
      },
      error: () => {
        this.saving.set(false);
        this.toast.error('Error al guardar el club');
      },
    });
  }

  onDelete(club: ClubResponse): void {
    this.deletingItem.set(club);
  }

  confirmDelete(): void {
    const club = this.deletingItem();
    if (!club) return;
    this.deletingItem.set(null);
    this.service.delete(club.id).subscribe({
      next: () => {
        this.clubs.update((list) => list.filter((c) => c.id !== club.id));
        this.toast.success(`"${club.name}" eliminado`);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 409 && this.canForceDelete()) {
          this.forceDeleteItem.set(club);
        } else {
          this.toast.error(err.error?.message ?? 'Error al eliminar el club');
        }
      },
    });
  }

  confirmForceDelete(): void {
    const club = this.forceDeleteItem();
    if (!club) return;
    this.forceDeleteItem.set(null);
    this.service.forceDelete(club.id).subscribe({
      next: () => {
        this.clubs.update((list) => list.filter((c) => c.id !== club.id));
        this.toast.success(`"${club.name}" eliminado con sus dependencias`);
      },
      error: (err: HttpErrorResponse) =>
        this.toast.error(err.error?.message ?? 'Error al eliminar el club'),
    });
  }

  field(name: string) {
    return this.form.get(name)!;
  }
}
