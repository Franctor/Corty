import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { SurfaceAdminService } from '../../core/services/surface-admin.service';
import { SurfaceResponse, SurfaceRequest } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { TableColumn } from '@frontend/shared-core';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { getFirstError } from '@frontend/shared-core';
import { ImagePickerComponent } from '@frontend/shared-ui';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-surfaces',
  templateUrl: 'surfaces.component.html',
  styleUrl: 'surfaces.component.scss',
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
export class SurfacesComponent implements OnInit {
  private service = inject(SurfaceAdminService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);

  protected getFirstError = getFirstError;

  readonly surfaces = signal<SurfaceResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly showModal = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly deletingItem = signal<SurfaceResponse | null>(null);

  readonly modalTitle = computed(() =>
    this.editingId() ? 'Editar superficie' : 'Nueva superficie'
  );

  readonly columns: TableColumn<SurfaceResponse>[] = [
    { key: 'name', label: 'Nombre', sortable: true },
    { key: 'description', label: 'Descripción' },
  ];

  readonly form: FormGroup = this.fb.group({
    name:        ['', [Validators.required, Validators.maxLength(50)]],
    description: ['', [Validators.maxLength(100)]],
    iconUrl:     [''],
  });

  ngOnInit(): void {
    this.loadSurfaces();
  }

  private loadSurfaces(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: (data) => { this.surfaces.set(data); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Error al cargar las superficies'); },
    });
  }

  openCreate(): void {
    this.editingId.set(null);
    this.form.reset();
    this.showModal.set(true);
  }

  openEdit(surface: SurfaceResponse): void {
    this.editingId.set(surface.id);
    this.form.patchValue(surface);
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
    const body = this.form.value as SurfaceRequest;
    const id = this.editingId();
    const req = id ? this.service.update(id, body) : this.service.create(body);

    req.subscribe({
      next: (saved) => {
        this.surfaces.update((list) =>
          id ? list.map((s) => (s.id === id ? saved : s)) : [...list, saved]
        );
        this.saving.set(false);
        this.showModal.set(false);
        this.toast.success(id ? 'Superficie actualizada' : 'Superficie creada');
      },
      error: () => {
        this.saving.set(false);
        this.toast.error('Error al guardar la superficie');
      },
    });
  }

  onDelete(surface: SurfaceResponse): void {
    this.deletingItem.set(surface);
  }

  confirmDelete(): void {
    const surface = this.deletingItem();
    if (!surface) return;
    this.deletingItem.set(null);
    this.service.delete(surface.id).subscribe({
      next: () => {
        this.surfaces.update((list) => list.filter((s) => s.id !== surface.id));
        this.toast.success(`"${surface.name}" eliminada`);
      },
      error: (err: HttpErrorResponse) =>
        this.toast.error(err.error?.message ?? 'Error al eliminar la superficie'),
    });
  }

  field(name: string) {
    return this.form.get(name)!;
  }
}
