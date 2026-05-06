import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { CourtAdminService } from '../../core/services/court-admin.service';
import { CourtScheduleService } from '../../core/services/court-schedule.service';
import { SportAdminService } from '../../core/services/sport-admin.service';
import { SurfaceAdminService } from '../../core/services/surface-admin.service';
import { ClubAdminService } from '../../core/services/club-admin.service';
import {
  CourtAdminResponse, CourtRequest, SportResponse, SurfaceResponse, ClubResponse,
  CourtScheduleEntry, CourtBlock, DayOfWeek,
} from '@frontend/shared-core';
import { AdminPageHeaderComponent } from '../../shared/components/admin-page-header/admin-page-header.component';
import { AdminTableComponent } from '../../shared/components/admin-table/admin-table.component';
import { TableColumn } from '@frontend/shared-core';
import { AdminModalComponent } from '../../shared/components/admin-modal/admin-modal.component';
import { ConfirmModalComponent } from '../../shared/components/confirm-modal/confirm-modal.component';
import { getFirstError } from '@frontend/shared-core';
import { ImagePickerComponent, SelectComponent, SelectOption, CheckboxComponent, DateInputComponent, TimeInputComponent } from '@frontend/shared-ui';
import { ToastService } from '../../shared/services/toast.service';
import { AuthService } from '@frontend/shared-auth';

type ModalTab = 'data' | 'schedule';

const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY:    'Lunes',
  TUESDAY:   'Martes',
  WEDNESDAY: 'Miércoles',
  THURSDAY:  'Jueves',
  FRIDAY:    'Viernes',
  SATURDAY:  'Sábado',
  SUNDAY:    'Domingo',
};

const ALL_DAYS: DayOfWeek[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

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
    SelectComponent,
    CheckboxComponent,
    DateInputComponent,
    TimeInputComponent,
  ],
})
export class CourtsComponent implements OnInit {
  private service = inject(CourtAdminService);
  private scheduleService = inject(CourtScheduleService);
  private sportService = inject(SportAdminService);
  private surfaceService = inject(SurfaceAdminService);
  private clubService = inject(ClubAdminService);
  private fb = inject(FormBuilder);
  private toast = inject(ToastService);
  private auth = inject(AuthService);

  protected getFirstError = getFirstError;
  readonly dayLabels = DAY_LABELS;
  readonly allDays = ALL_DAYS;

  readonly canForceDelete = computed(() => this.auth.hasAuthority('FORCE_DELETE'));

  readonly courts = signal<CourtAdminResponse[]>([]);
  readonly totalCourts = signal<number | null>(null);
  readonly currentPage = signal(0);
  readonly searchQuery = signal('');
  readonly clubs = signal<ClubResponse[]>([]);
  readonly sports = signal<SportResponse[]>([]);
  readonly surfaces = signal<SurfaceResponse[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly showModal = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly deletingItem = signal<CourtAdminResponse | null>(null);
  readonly forceDeleteItem = signal<CourtAdminResponse | null>(null);

  readonly activeTab = signal<ModalTab>('data');

  readonly schedules = signal<CourtScheduleEntry[]>([]);
  readonly blocks = signal<CourtBlock[]>([]);
  readonly savingSchedule = signal(false);
  readonly addingBlock = signal(false);

  readonly scheduleErrors = computed<Set<DayOfWeek>>(() => {
    const invalid = this.schedules()
      .filter((s) => !s.closed && !!s.openTime && !!s.closeTime && s.openTime >= s.closeTime)
      .map((s) => s.dayOfWeek);
    return new Set(invalid);
  });

  readonly clubOptions = computed<SelectOption<number>[]>(() =>
    this.clubs().map((c) => ({ value: c.id, label: c.name }))
  );
  readonly sportOptions = computed<SelectOption<number>[]>(() =>
    this.sports().map((s) => ({ value: s.id, label: s.name }))
  );
  readonly surfaceOptions = computed<SelectOption<number | null>[]>(() => [
    { value: null, label: 'Sin superficie' },
    ...this.surfaces().map((s) => ({ value: s.id, label: s.name })),
  ]);

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
    name:               ['', [Validators.required, Validators.maxLength(50)]],
    pricePerHour:       [null, [Validators.required, Validators.min(0)]],
    clubId:             [null, [Validators.required]],
    sportId:            [null, [Validators.required]],
    surfaceId:          [null],
    active:             [true],
    covered:            [false],
    lighting:           [false],
    imageUrl:           [null],
    useClubSchedule:    [true],
    slotDurationMinutes:[60, [Validators.required, Validators.min(15)]],
  });

  readonly today = new Date().toISOString().slice(0, 10);

  readonly blockForm: FormGroup = this.fb.group({
    blockDate:  ['', [Validators.required]],
    startTime:  ['', [Validators.required]],
    endTime:    ['', [Validators.required]],
    reason:     [''],
  }, { validators: this.blockTimeValidator });

  private blockTimeValidator(group: AbstractControl) {
    const start = group.get('startTime')?.value as string;
    const end   = group.get('endTime')?.value as string;
    if (start && end && start >= end) {
      return { timeOrder: true };
    }
    return null;
  }

  ngOnInit(): void {
    this.loadCourts();
    this.loadSelects();
  }

  private loadCourts(page = 0, search = ''): void {
    this.loading.set(true);
    this.service.getAll(page, 10, search).subscribe({
      next: (data) => { this.courts.set(data.content); this.totalCourts.set(data.page.totalElements); this.loading.set(false); },
      error: () => { this.loading.set(false); this.toast.error('Error al cargar las pistas'); },
    });
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadCourts(page, this.searchQuery());
  }

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    this.loadCourts(0, query);
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
    this.activeTab.set('data');
    this.form.reset({ active: true, covered: false, lighting: false, clubId: null, sportId: null, surfaceId: null, imageUrl: null, useClubSchedule: true, slotDurationMinutes: 60 });
    this.showModal.set(true);
  }

  openEdit(court: CourtAdminResponse): void {
    this.editingId.set(court.id);
    this.activeTab.set('data');
    const club = this.clubs().find((c) => c.name === court.clubName);
    const sport = this.sports().find((s) => s.name === court.sportName);
    const surface = this.surfaces().find((s) => s.name === court.surfaceName) ?? null;
    this.form.patchValue({
      ...court,
      clubId: club?.id ?? null,
      sportId: sport?.id ?? null,
      surfaceId: surface?.id ?? null,
    });
    this.loadScheduleData(court.id);
    this.showModal.set(true);
  }

  private loadScheduleData(courtId: number): void {
    this.scheduleService.getSchedules(courtId).subscribe({
      next: (data) => {
        const filled = this.allDays.map((day) => {
          const existing = data.find((s) => s.dayOfWeek === day);
          return existing ?? { dayOfWeek: day, openTime: '09:00', closeTime: '21:00', closed: false };
        });
        this.schedules.set(filled);
      },
      error: () => this.toast.error('Error al cargar el horario'),
    });
    this.scheduleService.getBlocks(courtId).subscribe({
      next: (data) => this.blocks.set(data),
      error: () => this.toast.error('Error al cargar los bloqueos'),
    });
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

  saveSchedules(): void {
    const id = this.editingId();
    if (!id || this.savingSchedule()) return;
    this.savingSchedule.set(true);
    const useClub = this.form.value.useClubSchedule as boolean;

    this.scheduleService.updateUseClubSchedule(id, useClub).subscribe({
      next: () => {
        const slotDuration = this.form.value.slotDurationMinutes as number;
        this.scheduleService.updateSlotDuration(id, slotDuration).subscribe({
          next: () => {
            if (useClub) {
              this.savingSchedule.set(false);
              this.toast.success('Configuración de horario guardada');
            } else {
              const payload = this.schedules().map((s) => ({
                ...s,
                openTime:  (s.closed || !s.openTime)  ? null : s.openTime,
                closeTime: (s.closed || !s.closeTime) ? null : s.closeTime,
              }));
              this.scheduleService.replaceSchedules(id, payload as any).subscribe({
                next: (saved) => {
                  this.schedules.set(saved);
                  this.savingSchedule.set(false);
                  this.toast.success('Horario guardado');
                },
                error: () => { this.savingSchedule.set(false); this.toast.error('Error al guardar el horario'); },
              });
            }
          },
          error: () => { this.savingSchedule.set(false); this.toast.error('Error al guardar la configuración'); },
        });
      },
      error: () => { this.savingSchedule.set(false); this.toast.error('Error al guardar la configuración'); },
    });
  }

  toggleDayClosed(dayOfWeek: DayOfWeek): void {
    this.schedules.update((list) =>
      list.map((s) => s.dayOfWeek === dayOfWeek ? { ...s, closed: !s.closed } : s)
    );
  }

  updateScheduleTime(dayOfWeek: DayOfWeek, field: 'openTime' | 'closeTime', value: string): void {
    this.schedules.update((list) =>
      list.map((s) => s.dayOfWeek === dayOfWeek ? { ...s, [field]: value } : s)
    );
  }

  addBlock(): void {
    const id = this.editingId();
    if (!id || this.blockForm.invalid || this.addingBlock()) {
      this.blockForm.markAllAsTouched();
      return;
    }
    this.addingBlock.set(true);
    const value = this.blockForm.value;
    this.scheduleService.addBlock(id, {
      blockDate: value.blockDate,
      startTime: value.startTime,
      endTime:   value.endTime,
      reason:    value.reason || null,
    }).subscribe({
      next: (block) => {
        this.blocks.update((list) => [...list, block]);
        this.blockForm.reset();
        this.addingBlock.set(false);
        this.toast.success('Bloqueo añadido');
      },
      error: () => { this.addingBlock.set(false); this.toast.error('Error al añadir el bloqueo'); },
    });
  }

  removeBlock(blockId: number): void {
    const id = this.editingId();
    if (!id) return;
    this.scheduleService.deleteBlock(id, blockId).subscribe({
      next: () => this.blocks.update((list) => list.filter((b) => b.id !== blockId)),
      error: () => this.toast.error('Error al eliminar el bloqueo'),
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

  blockField(name: string) {
    return this.blockForm.get(name)!;
  }
}
