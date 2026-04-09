import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { SportAdminService } from '../../core/services/sport-admin.service';
import { SportResponse, SportRequest } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { TableColumn } from '@frontend/shared-core';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { getFirstError } from '@frontend/shared-core';
import { ImagePickerComponent } from '@frontend/shared-ui';

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
    ImagePickerComponent,
  ],
})
export class SportsComponent implements OnInit {
  private service = inject(SportAdminService);
  private fb = inject(FormBuilder);

  protected getFirstError = getFirstError;

  readonly sports = signal<SportResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly showModal = signal(false);
  readonly editingId = signal<number | null>(null);

  readonly modalTitle = computed(() =>
    this.editingId() ? 'Editar deporte' : 'Nuevo deporte'
  );

  readonly columns: TableColumn<SportResponse>[] = [
    { key: 'name', label: 'Nombre' },
    { key: 'playersPerMatch', label: 'Jugadores por partido' },
    { key: 'defaultDurationMins', label: 'Duración (min)' },
    { key: 'teamSport', label: 'Deporte de equipo', render: (r) => r.teamSport ? 'Sí' : 'No' },
    { key: 'color', label: 'Color' },
  ];

  readonly form: FormGroup = this.fb.group({
    name:                ['', [Validators.required, Validators.maxLength(50)]],
    playersPerTeam:      [2,  [Validators.required, Validators.min(0)]],
    playersPerMatch:     [4,  [Validators.required, Validators.min(1)]],
    iconUrl:             ['', [Validators.required]],
    color:               ['#58CC02', []],
    teamSport:         [true, [Validators.required]],
    defaultDurationMins: [60, [Validators.min(0)]],
  });

  ngOnInit(): void {
    this.loadSports();
  }

  private loadSports(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: (data) => { this.sports.set(data); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.form.reset({ playersPerTeam: 2, playersPerMatch: 4, teamSport: true, defaultDurationMins: 60, color: '#58CC02' });
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
      },
      error: () => this.saving.set(false),
    });
  }

  onDelete(sport: SportResponse): void {
    if (!confirm(`¿Eliminar "${sport.name}"?`)) return;
    this.service.delete(sport.id).subscribe({
      next: () => this.sports.update((list) => list.filter((s) => s.id !== sport.id)),
    });
  }

  field(name: string) {
    return this.form.get(name)!;
  }
}
