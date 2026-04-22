import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { SportAdminService } from '../../core/services/sport-admin.service';
import { SportResponse, SportRequest } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { TableColumn } from '@frontend/shared-core';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { getFirstError } from '@frontend/shared-core';
import { ImagePickerComponent, ColorPickerComponent } from '@frontend/shared-ui';
import { ToastService } from '../../shared/services/toast.service';
import { AuthService } from '@frontend/shared-auth';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-sports',
  templateUrl: 'sports.component.html',
  styleUrl: 'sports.component.scss',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    AdminPageHeaderComponent,
    AdminTableComponent,
    AdminModalComponent,
    ConfirmModalComponent,
    ImagePickerComponent,
    ColorPickerComponent,
  ],
})
export class SportsComponent implements OnInit {
  private service = inject(SportAdminService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);
  private auth = inject(AuthService);

  protected getFirstError = getFirstError;

  readonly canForceDelete = computed(() => this.auth.hasAuthority('FORCE_DELETE'));

  readonly sports = signal<SportResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly showModal = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly deletingItem = signal<SportResponse | null>(null);
  readonly forceDeleteItem = signal<SportResponse | null>(null);

  readonly modalTitle = computed(() =>
    this.editingId() ? 'Editar deporte' : 'Nuevo deporte'
  );

  readonly columns: TableColumn<SportResponse>[] = [
    { key: 'name', label: 'Nombre' },
    { key: 'playersPerMatch', label: 'Jugadores por partido' },
    { key: 'teamSport', label: 'Deporte de equipo', render: (r) => r.teamSport ? 'Sí' : 'No' },
    {
      key: 'color',
      label: 'Color',
      isHtml: true,
      render: (r) => r.color
        ? `<span style="display:inline-flex;align-items:center;gap:6px">
             <span style="width:16px;height:16px;border-radius:50%;background:${r.color};border:1.5px solid rgba(0,0,0,.15);flex-shrink:0;display:inline-block"></span>
             ${r.color}
           </span>`
        : '—',
    },
  ];

  readonly form: FormGroup = this.fb.group({
    name:                ['', [Validators.required, Validators.maxLength(50)]],
    playersPerTeam:      [2,  [Validators.required, Validators.min(0)]],
    playersPerMatch:     [4,  [Validators.required, Validators.min(1)]],
    iconUrl:             ['', [Validators.required]],
    color:               ['#58CC02', []],
    teamSport:           [true, [Validators.required]],
  });

  ngOnInit(): void {
    this.loadSports();
  }

  private loadSports(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: (data) => { this.sports.set(data); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Error al cargar los deportes'); },
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.form.reset({ playersPerTeam: 2, playersPerMatch: 4, teamSport: true, color: '#58CC02' });
    this.showModal.set(true);
  }

  openEdit(sport: SportResponse): void {
    this.editingId.set(sport.id);
    this.form.patchValue(sport);
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
    const body = this.form.value as SportRequest;
    const id = this.editingId();
    const req = id ? this.service.update(id, body) : this.service.create(body);

    req.subscribe({
      next: (saved) => {
        this.sports.update((list) =>
          id ? list.map((s) => (s.id === id ? saved : s)) : [...list, saved]
        );
        this.saving.set(false);
        this.showModal.set(false);
        this.toast.success(id ? 'Deporte actualizado' : 'Deporte creado');
      },
      error: () => {
        this.saving.set(false);
        this.toast.error('Error al guardar el deporte');
      },
    });
  }

  onDelete(sport: SportResponse): void {
    this.deletingItem.set(sport);
  }

  confirmDelete(): void {
    const sport = this.deletingItem();
    if (!sport) return;
    this.deletingItem.set(null);
    this.service.delete(sport.id).subscribe({
      next: () => {
        this.sports.update((list) => list.filter((s) => s.id !== sport.id));
        this.toast.success(`"${sport.name}" eliminado`);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 409 && this.canForceDelete()) {
          this.forceDeleteItem.set(sport);
        } else {
          this.toast.error(err.error?.message ?? 'Error al eliminar el deporte');
        }
      },
    });
  }

  confirmForceDelete(): void {
    const sport = this.forceDeleteItem();
    if (!sport) return;
    this.forceDeleteItem.set(null);
    this.service.forceDelete(sport.id).subscribe({
      next: () => {
        this.sports.update((list) => list.filter((s) => s.id !== sport.id));
        this.toast.success(`"${sport.name}" eliminado con sus dependencias`);
      },
      error: (err: HttpErrorResponse) =>
        this.toast.error(err.error?.message ?? 'Error al eliminar el deporte'),
    });
  }

  field(name: string) {
    return this.form.get(name)!;
  }
}
