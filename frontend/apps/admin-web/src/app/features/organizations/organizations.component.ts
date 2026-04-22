import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { OrgAdminService } from '../../core/services/org-admin.service';
import { OrgAdminResponse, OrgAdminCreateRequest, TableColumn } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { ToastService } from '../../shared/services/toast.service';
import { AuthService } from '@frontend/shared-auth';
import { LocationSelectComponent } from '@frontend/shared-ui';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-organizations',
  templateUrl: 'organizations.component.html',
  styleUrl: 'organizations.component.scss',
  standalone: true,
  imports: [
    AdminPageHeaderComponent,
    AdminTableComponent,
    AdminModalComponent,
    ReactiveFormsModule,
    LocationSelectComponent,
    LucideAngularModule,
  ],
})
export class OrganizationsComponent implements OnInit {
  private service = inject(OrgAdminService);
  private toast = inject(ToastService);
  private auth = inject(AuthService);
  private fb = inject(FormBuilder);

  readonly canEdit = computed(() =>
    this.auth.getRole() === 'SUPERADMIN' || this.auth.getRole() === 'ADMIN'
  );
  readonly canEditRow = (_: OrgAdminResponse) => this.canEdit();
  readonly canDeleteRow = (_: OrgAdminResponse) => false;

  readonly organizations = signal<OrgAdminResponse[]>([]);
  readonly loading = signal(false);
  readonly editItem = signal<OrgAdminResponse | null>(null);
  readonly saving = signal(false);

  readonly createOpen = signal(false);
  readonly csvOpen = signal(false);
  readonly csvUploading = signal(false);
  csvFile: File | null = null;

  readonly columns: TableColumn<OrgAdminResponse>[] = [
    { key: 'username',     label: 'Usuario' },
    { key: 'businessName', label: 'Razón social' },
    { key: 'cif',          label: 'CIF' },
    { key: 'email',        label: 'Email' },
    { key: 'city',         label: 'Ciudad', render: (r) => r.city ?? '—' },
  ];

  editForm!: FormGroup;
  createForm!: FormGroup;

  ngOnInit(): void {
    this.loadOrganizations();
  }

  private loadOrganizations(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: (data) => { this.organizations.set(data); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Error al cargar las organizaciones'); },
    });
  }

  openCreate(): void {
    this.createForm = this.fb.group({
      username:     ['', [Validators.required, Validators.minLength(3)]],
      email:        ['', [Validators.required, Validators.email]],
      password:     ['', [Validators.required, Validators.minLength(8)]],
      businessName: ['', Validators.required],
      cif:          ['', [Validators.required, Validators.minLength(9), Validators.maxLength(9)]],
      cityId:       [null],
      verified:     [false],
    });
    this.createOpen.set(true);
  }

  openCsv(): void {
    this.csvFile = null;
    this.csvOpen.set(true);
  }

  openEdit(org: OrgAdminResponse): void {
    this.editItem.set(org);
    this.editForm = this.fb.group({
      businessName: [org.businessName, Validators.required],
      cif:          [org.cif, [Validators.required, Validators.minLength(9), Validators.maxLength(9)]],
      cityId:       [org.cityId],
    });
  }

  saveCreate(): void {
    if (this.createForm.invalid || this.saving()) return;
    this.saving.set(true);
    const req: OrgAdminCreateRequest = this.createForm.value;
    this.service.create(req).subscribe({
      next: (created) => {
        this.organizations.update(list => [created, ...list]);
        this.createOpen.set(false);
        this.saving.set(false);
        this.toast.success('Organización creada correctamente');
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.toast.error(err.error?.message ?? 'Error al crear la organización');
      },
    });
  }

  saveEdit(): void {
    if (this.editForm.invalid || this.saving()) return;
    const org = this.editItem();
    if (!org) return;
    this.saving.set(true);
    this.service.update(org.id, this.editForm.value).subscribe({
      next: (updated) => {
        this.organizations.update(list => list.map(o => o.id === updated.id ? updated : o));
        this.editItem.set(null);
        this.saving.set(false);
        this.toast.success('Organización actualizada');
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.toast.error(err.error?.message ?? 'Error al guardar');
      },
    });
  }

  onCsvSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.csvFile = input.files?.[0] ?? null;
  }

  uploadCsv(): void {
    if (!this.csvFile || this.csvUploading()) return;
    this.csvUploading.set(true);
    this.service.createBatch(this.csvFile).subscribe({
      next: (created) => {
        this.organizations.update(list => [...created, ...list]);
        this.csvOpen.set(false);
        this.csvUploading.set(false);
        this.toast.success(`${created.length} organización(es) importada(s) correctamente`);
      },
      error: (err: HttpErrorResponse) => {
        this.csvUploading.set(false);
        this.toast.error(err.error?.message ?? 'Error al procesar el CSV');
      },
    });
  }
}
