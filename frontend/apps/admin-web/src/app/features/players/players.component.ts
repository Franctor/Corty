import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { PlayerAdminService } from '../../core/services/player-admin.service';
import { PlayerAdminResponse, PlayerAdminCreateRequest, TableColumn } from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { ToastService } from '../../shared/services/toast.service';
import { AuthService } from '@frontend/shared-auth';
import { SelectComponent, SelectOption, LocationSelectComponent } from '@frontend/shared-ui';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-players',
  templateUrl: 'players.component.html',
  styleUrl: 'players.component.scss',
  standalone: true,
  imports: [
    AdminPageHeaderComponent,
    AdminTableComponent,
    AdminModalComponent,
    ReactiveFormsModule,
    SelectComponent,
    LocationSelectComponent,
    LucideAngularModule,
  ],
})
export class PlayersComponent implements OnInit {
  private service = inject(PlayerAdminService);
  private toast = inject(ToastService);
  private auth = inject(AuthService);
  private fb = inject(FormBuilder);
  readonly canEdit = computed(() =>
    this.auth.getRole() === 'SUPERADMIN' || this.auth.hasAuthority('MANAGE_STAFF')
  );
  readonly canEditRow = (_: PlayerAdminResponse) => this.canEdit();
  readonly canDeleteRow = (_: PlayerAdminResponse) => false;

  readonly players = signal<PlayerAdminResponse[]>([]);
  readonly loading = signal(false);
  readonly editItem = signal<PlayerAdminResponse | null>(null);
  readonly saving = signal(false);

  readonly createOpen = signal(false);
  readonly csvOpen = signal(false);
  readonly csvUploading = signal(false);
  csvFile: File | null = null;

  readonly genderOptions: SelectOption<string>[] = [
    { value: 'MALE',   label: 'Masculino' },
    { value: 'FEMALE', label: 'Femenino' },
    { value: 'OTHER',  label: 'Otro' },
  ];

  readonly columns: TableColumn<PlayerAdminResponse>[] = [
    { key: 'username', label: 'Usuario', sortable: true },
    { key: 'name',     label: 'Nombre',   sortable: true, render: (r) => `${r.name} ${r.surname}` },
    { key: 'email',    label: 'Email',    sortable: true },
    { key: 'phone',    label: 'Teléfono' },
    { key: 'gender',   label: 'Género',   render: (r) => ({ MALE: 'Masculino', FEMALE: 'Femenino', OTHER: 'Otro' })[r.gender] ?? r.gender },
    { key: 'karma',    label: 'Karma',    sortable: true },
    { key: 'city',     label: 'Ciudad',   sortable: true, render: (r) => r.city ?? '—' },
  ];

  editForm!: FormGroup;
  createForm!: FormGroup;


  ngOnInit(): void {
    this.loadPlayers();
  }

  private loadPlayers(): void {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: (data) => { this.players.set(data); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Error al cargar los jugadores'); },
    });
  }

  openCreate(): void {
    this.createForm = this.fb.group({
      username:  ['', [Validators.required, Validators.minLength(3)]],
      email:     ['', [Validators.required, Validators.email]],
      password:  ['', [Validators.required, Validators.minLength(8)]],
      name:      ['', Validators.required],
      surname:   ['', Validators.required],
      phone:     ['', Validators.required],
      gender:    ['', Validators.required],
      birthDate: ['', Validators.required],
      biography: [null],
      avatarUrl: [null],
      cityId:    [null],
      karma:     [50, [Validators.min(0), Validators.max(100)]],
      verified:  [false],
    });
    this.createOpen.set(true);
  }

  openCsv(): void {
    this.csvFile = null;
    this.csvOpen.set(true);
  }

  openEdit(player: PlayerAdminResponse): void {
    this.editItem.set(player);
    this.editForm = this.fb.group({
      name:      [player.name,      Validators.required],
      surname:   [player.surname,   Validators.required],
      phone:     [player.phone,     Validators.required],
      gender:    [player.gender,    Validators.required],
      birthDate: [player.birthDate, Validators.required],
      biography: [player.biography],
      karma:     [player.karma,     [Validators.min(0), Validators.max(100)]],
      cityId:    [player.cityId],
    });
  }

  saveCreate(): void {
    if (this.createForm.invalid || this.saving()) return;
    this.saving.set(true);
    const req: PlayerAdminCreateRequest = this.createForm.value;
    this.service.create(req).subscribe({
      next: (created) => {
        this.players.update(list => [created, ...list]);
        this.createOpen.set(false);
        this.saving.set(false);
        this.toast.success('Jugador creado correctamente');
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.toast.error(err.error?.message ?? 'Error al crear el jugador');
      },
    });
  }

  saveEdit(): void {
    if (this.editForm.invalid || this.saving()) return;
    const player = this.editItem();
    if (!player) return;
    this.saving.set(true);
    this.service.update(player.id, this.editForm.value).subscribe({
      next: (updated) => {
        this.players.update(list => list.map(p => p.id === updated.id ? updated : p));
        this.editItem.set(null);
        this.saving.set(false);
        this.toast.success('Jugador actualizado');
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
        this.players.update(list => [...created, ...list]);
        this.csvOpen.set(false);
        this.csvUploading.set(false);
        this.toast.success(`${created.length} jugador(es) importado(s) correctamente`);
      },
      error: (err: HttpErrorResponse) => {
        this.csvUploading.set(false);
        this.toast.error(err.error?.message ?? 'Error al procesar el CSV');
      },
    });
  }
}
