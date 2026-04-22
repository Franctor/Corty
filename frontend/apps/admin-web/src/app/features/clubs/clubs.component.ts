import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ClubAdminService } from '../../core/services/club-admin.service';
import { OrgAdminService } from '../../core/services/org-admin.service';
import { ClubRequest, ClubResponse } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { TableColumn } from '@frontend/shared-core';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { getFirstError } from '@frontend/shared-core';
import { ImagePickerComponent, MapPickerComponent, MapPickerValue, SelectComponent } from '@frontend/shared-ui';
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
    MapPickerComponent,
    SelectComponent,
  ],
})
export class ClubsComponent implements OnInit {
  private service = inject(ClubAdminService);
  private orgService = inject(OrgAdminService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);
  private auth = inject(AuthService);

  protected getFirstError = getFirstError;

  readonly canForceDelete = computed(() => this.auth.hasAuthority('FORCE_DELETE'));
  readonly isOrg = this.auth.getRole() === 'ORGANIZATION';

  readonly orgOptions = signal<{ value: number; label: string }[]>([]);

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
    { key: 'name',             label: 'Nombre' },
    { key: 'organizationName', label: 'Organización', render: (r) => r.organizationName ?? '—' },
    { key: 'cityName',         label: 'Ciudad' },
    { key: 'phone',            label: 'Teléfono' },
    { key: 'contactEmail',     label: 'Email de contacto' },
  ];

  readonly form: FormGroup = this.fb.group({
    name:           ['', [Validators.required, Validators.maxLength(50)]],
    description:    [''],
    phone:          ['', [Validators.required, Validators.maxLength(20)]],
    contactEmail:   ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    nif:            ['', [Validators.required, Validators.maxLength(9)]],
    organizationId: [null, this.isOrg ? [] : [Validators.required]],
    location:       [null, [Validators.required]],
    logoUrl:        [null],
  });

  ngOnInit(): void {
    this.loadClubs();
    this.orgService.getAll().subscribe(orgs =>
      this.orgOptions.set(orgs.map(o => ({ value: o.id, label: o.businessName })))
    );
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
    this.form.reset({ organizationId: null, location: null, logoUrl: null });
    this.showModal.set(true);
  }

  openEdit(club: ClubResponse): void {
    this.editingId.set(club.id);
    const location: MapPickerValue | null = (club.geoLat != null && club.geoLong != null)
      ? { lat: Number(club.geoLat), lng: Number(club.geoLong), cityId: club.cityId, cityName: club.cityName, address: club.address }
      : null;
    this.form.patchValue({ ...club, organizationId: club.organizationId, location });
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
    const formValue = this.form.value;
    const location = formValue.location as MapPickerValue;
    const body: ClubRequest = {
      name:           formValue.name,
      description:    formValue.description,
      phone:          formValue.phone,
      contactEmail:   formValue.contactEmail,
      nif:            formValue.nif,
      organizationId: formValue.organizationId,
      logoUrl:        formValue.logoUrl,
      address:        location?.address ?? '',
      cityId:         location?.cityId ?? null,
      geoLat:         location?.lat ?? null,
      geoLong:        location?.lng ?? null,
    };
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
