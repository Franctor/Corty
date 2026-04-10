import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { CourtAdminService } from '../../core/services/court-admin.service';
import { SportAdminService } from '../../core/services/sport-admin.service';
import { SurfaceAdminService } from '../../core/services/surface-admin.service';
import { ClubAdminService } from '../../core/services/club-admin.service';
import { CourtAdminResponse, CourtRequest, SportResponse, SurfaceResponse, ClubResponse } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { TableColumn } from '@frontend/shared-core';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { getFirstError } from '@frontend/shared-core';
import { ImagePickerComponent } from '@frontend/shared-ui';
import { ToastService } from '../../shared/services/toast.service';
import { AuthService } from '@frontend/shared-auth';

@Component({
  selector: 'app-courts',
  templateUrl: 'courts.component.html',
  styleUrl: 'courts.component.scss',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    AdminPageHeaderComponent,
    AdminTableComponent,
    AdminModalComponent,
    ConfirmModalComponent,
    ImagePickerComponent,
  ],
})
export class CourtsComponent implements OnInit {
  private service = inject(CourtAdminService);
  private sportService = inject(SportAdminService);
  private surfaceService = inject(SurfaceAdminService);
  private clubService = inject(ClubAdminService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);
  private auth = inject(AuthService);

  protected getFirstError = getFirstError;

  readonly canForceDelete = computed(() => this.auth.hasAuthority('FORCE_DELETE'));

  readonly courts = signal<CourtAdminResponse[]>([]);
  readonly clubs = signal<ClubResponse[]>([]);
  readonly sports = signal<SportResponse[]>([]);
  readonly surfaces = signal<SurfaceResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly showModal = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly deletingItem = signal<CourtAdminResponse | null>(null);
  readonly forceDeleteItem = signal<CourtAdminResponse | null>(null);

  readonly modalTitle = computed(() =>
    this.editingId() ? 'Editar pista' : 'Nueva pista'
  );

  readonly columns: TableColumn<CourtAdminResponse>[] = [
    { key: 'name', label: 'Nombre' },
    { key: 'clubName', label: 'Club' },
    { key: 'sportName', label: 'Deporte' },
    { key: 'surfaceName', label: 'Superficie' },
    { key: 'pricePerHour', label: 'Precio/hora' },
    { key: 'active', label: 'Activa', render: (r) => r.active ? 'Sí' : 'No' },
    { key: 'covered', label: 'Cubierta', render: (r) => r.covered ? 'Sí' : 'No' },
  ];

  readonly form: FormGroup = this.fb.group({
    name:         ['', [Validators.required, Validators.maxLength(50)]],
    pricePerHour: [null, [Validators.required, Validators.min(0)]],
    clubId:       [null, [Validators.required]],
    sportId:      [null, [Validators.required]],
    surfaceId:    [null],
    active:       [true],
    covered:      [false],
    lighting:     [false],
    imageUrl:     [null],
  });

  ngOnInit(): void {
    this.loadCourts();
    this.loadSelects();
  }

  private loadCourts(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: (data) => { this.courts.set(data); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Error al cargar las pistas'); },
    });
  }

  private loadSelects(): void {
    this.clubService.getAll().subscribe({
      next: (data) => this.clubs.set(data),
      error: () => this.toast.error('Error al cargar los clubs'),
    });
    this.sportService.getAll().subscribe({
      next: (data) => this.sports.set(data),
      error: () => this.toast.error('Error al cargar los deportes'),
    });
    this.surfaceService.getAll().subscribe({
      next: (data) => this.surfaces.set(data),
      error: () => this.toast.error('Error al cargar las superficies'),
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.form.reset({ active: true, covered: false, lighting: false, clubId: null, sportId: null, surfaceId: null, imageUrl: null });
    this.showModal.set(true);
  }

  openEdit(court: CourtAdminResponse): void {
    this.editingId.set(court.id);
    const club = this.clubs().find((c) => c.name === court.clubName);
    const sport = this.sports().find((s) => s.name === court.sportName);
    const surface = this.surfaces().find((s) => s.name === court.surfaceName) ?? null;
    this.form.patchValue({
      ...court,
      clubId: club?.id ?? null,
      sportId: sport?.id ?? null,
      surfaceId: surface?.id ?? null,
    });
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
    const body = this.form.value as CourtRequest;
    const id = this.editingId();
    const req = id ? this.service.update(id, body) : this.service.create(body);

    req.subscribe({
      next: (saved) => {
        this.courts.update((list) =>
          id ? list.map((c) => (c.id === id ? saved : c)) : [...list, saved]
        );
        this.saving.set(false);
        this.showModal.set(false);
        this.toast.success(id ? 'Pista actualizada' : 'Pista creada');
      },
      error: () => {
        this.saving.set(false);
        this.toast.error('Error al guardar la pista');
      },
    });
  }

  onDelete(court: CourtAdminResponse): void {
    this.deletingItem.set(court);
  }

  confirmDelete(): void {
    const court = this.deletingItem();
    if (!court) return;
    this.deletingItem.set(null);
    this.service.delete(court.id).subscribe({
      next: () => {
        this.courts.update((list) => list.filter((c) => c.id !== court.id));
        this.toast.success(`"${court.name}" eliminada`);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 409 && this.canForceDelete()) {
          this.forceDeleteItem.set(court);
        } else {
          this.toast.error(err.error?.message ?? 'Error al eliminar la pista');
        }
      },
    });
  }

  confirmForceDelete(): void {
    const court = this.forceDeleteItem();
    if (!court) return;
    this.forceDeleteItem.set(null);
    this.service.forceDelete(court.id).subscribe({
      next: () => {
        this.courts.update((list) => list.filter((c) => c.id !== court.id));
        this.toast.success(`"${court.name}" eliminada con sus dependencias`);
      },
      error: (err: HttpErrorResponse) =>
        this.toast.error(err.error?.message ?? 'Error al eliminar la pista'),
    });
  }

  field(name: string) {
    return this.form.get(name)!;
  }
}
