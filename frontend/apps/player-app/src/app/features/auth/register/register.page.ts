import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { Subject, takeUntil, startWith } from 'rxjs';

import {
  IonContent,
  IonButton,
  IonSpinner,
  ToastController,
} from '@ionic/angular/standalone';

import { BreakpointService, CortyLogoComponent, WizardComponent } from '@frontend/shared-ui';
import { AuthService } from '@frontend/shared-auth';
import { LocationService, CortyValidators, getFirstError, MediaService } from '@frontend/shared-core';
import { CityResponse, ProvinceResponse } from '@frontend/shared-core';

import { UiInputComponent } from '../../../components/forms/ui-input/ui-input.component';
import { UiPasswordComponent } from '../../../components/forms/ui-password/ui-password.component';
import { UiPasswordChecklistComponent } from '../../../components/forms/ui-password-checklist/ui-password-checklist.component';
import { UiSelectComponent } from '../../../components/forms/ui-select/ui-select.component';
import { UiDatepickerComponent } from '../../../components/forms/ui-datepicker/ui-datepicker.component';
import { UiAutocompleteComponent } from '../../../components/forms/ui-autocomplete/ui-autocomplete.component';
import { UiTextareaComponent } from '../../../components/forms/ui-textarea/ui-textarea.component';
import { AvatarPickerComponent } from '../../../components/avatar-picker/avatar-picker.component';

@Component({
  selector: 'app-register',
  templateUrl: 'register.page.html',
  styleUrls: ['register.page.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    IonContent,
    IonButton,
    IonSpinner,
    AvatarPickerComponent,
    CortyLogoComponent,
    WizardComponent,
    UiInputComponent,
    UiPasswordComponent,
    UiPasswordChecklistComponent,
    UiSelectComponent,
    UiDatepickerComponent,
    UiAutocompleteComponent,
    UiTextareaComponent,
  ],
})
export class RegisterPage implements OnInit, OnDestroy {

  @ViewChild(IonContent) private content!: IonContent;

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly locationService = inject(LocationService);
  private readonly mediaService = inject(MediaService);
  private readonly router = inject(Router);
  private readonly toastController = inject(ToastController);
  private readonly bp = inject(BreakpointService);

  private readonly destroy$ = new Subject<void>();

  protected readonly getFirstError = getFirstError;

  readonly totalSteps = 2;
  readonly currentStep = signal(1);
  readonly isLoading = signal(false);
  readonly avatarFile = signal<File | null>(null);

  readonly provinces = signal<ProvinceResponse[]>([]);
  readonly cities = signal<CityResponse[]>([]);

  readonly genderOptions = [
    { value: 'MALE', label: 'Masculino' },
    { value: 'FEMALE', label: 'Femenina' },
    { value: 'OTHER', label: 'Otro' },
  ];

  readonly provinceOptions = computed(() =>
    this.provinces().map(p => ({ value: p.code, label: p.label }))
  );

  readonly cityOptions = computed(() =>
    this.cities().map(c => ({ value: c.idCity, label: c.label }))
  );

  readonly step1: FormGroup = this.fb.group(
    {
      username: ['', [Validators.required, Validators.minLength(3), CortyValidators.noWhitespace]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, CortyValidators.strongPassword]],
      confirmPassword: ['', Validators.required],
    },
    { validators: CortyValidators.passwordMatch('password', 'confirmPassword') }
  );

  readonly step2: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(50)]],
    surname: ['', [Validators.required, Validators.maxLength(50)]],
    phone: ['', [Validators.required, CortyValidators.phoneEs]],
    gender: ['', Validators.required],
    birthDate: ['', [Validators.required, CortyValidators.minAge(18)]],
    biography: ['', Validators.maxLength(500)],
    provinceCode: ['', Validators.required],
    cityId: [null, Validators.required],
    avatarUrl: [''],
  });

  // ── Step 1 getters ───────────────────────────────────────────────
  get username() { return this.step1.get('username')!; }
  get email() { return this.step1.get('email')!; }
  get password() { return this.step1.get('password')!; }
  get confirmPassword() { return this.step1.get('confirmPassword')!; }
  get passwordMatch() { return this.step1.errors?.['passwordMatch'] && this.confirmPassword.touched; }

  // ── Step 2 getters ───────────────────────────────────────────────
  get name() { return this.step2.get('name')!; }
  get surname() { return this.step2.get('surname')!; }
  get phone() { return this.step2.get('phone')!; }
  get gender() { return this.step2.get('gender')!; }
  get birthDate() { return this.step2.get('birthDate')!; }
  get biography() { return this.step2.get('biography')!; }
  get provinceCode() { return this.step2.get('provinceCode')!; }
  get cityId() { return this.step2.get('cityId')!; }

  get maxBirthDate(): string {
    const cutoff = new Date();
    cutoff.setFullYear(cutoff.getFullYear() - 18);
    return cutoff.toISOString().split('T')[0];
  }

  ngOnInit(): void {
    this.step2.get('cityId')!.disable();
    this.locationService.getProvinces()
      .pipe(takeUntil(this.destroy$))
      .subscribe(p => this.provinces.set(p));

    this.provinceCode.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(code => this.onProvinceChange(code));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private onProvinceChange(provinceCode: string): void {
    const cityControl = this.step2.get('cityId')!;
    this.step2.patchValue({ cityId: null });
    this.cities.set([]);

    if (!provinceCode) {
      cityControl.disable();
      return;
    }

    cityControl.enable();
    this.locationService.getCitiesByProvince(provinceCode)
      .pipe(takeUntil(this.destroy$))
      .subscribe(c => this.cities.set(c));
  }

  onAvatarSelected(file: File): void {
    this.avatarFile.set(file);
  }

  onAvatarRemoved(): void {
    this.avatarFile.set(null);
    this.step2.patchValue({ avatarUrl: '' });
  }

  goNext(): void {
    if (this.step1.invalid) {
      this.step1.markAllAsTouched();
      return;
    }
    this.currentStep.set(2);
    this.content?.scrollToTop(0);
  }

  goBack(): void {
    this.currentStep.set(1);
    this.content?.scrollToTop(0);
  }

  onSubmit(): void {
    if (this.step2.invalid || this.isLoading()) {
      this.step2.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const { username, email, password } = this.step1.value;
    const { name, surname, phone, gender, birthDate, biography, cityId } = this.step2.value;
    const normalizedPhone = (phone as string).replace(/[\s.\-()]/g, '');

    const doRegister = (avatarUrl: string | null) => {
      this.authService.register({
        username, email, password,
        name, surname, phone: normalizedPhone, gender, birthDate, biography, cityId,
        avatarUrl,
      }).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => {
          this.isLoading.set(false);
          this.router.navigate(['/auth/check-email']);
        },
        error: async (err) => {
          this.isLoading.set(false);
          const message = err?.error?.message ?? 'Error al registrarse. Inténtalo de nuevo.';
          const toast = await this.toastController.create({
            message, duration: 3000, color: 'danger', position: 'top',
          });
          await toast.present();
        },
      });
    };

    const file = this.avatarFile();

    if (file) {
      this.mediaService.uploadAvatar(file)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (url) => doRegister(url),
          error: async (err) => {
            this.isLoading.set(false);
            const message = err?.error?.message ?? 'Error al subir la imagen. Inténtalo de nuevo.';
            const toast = await this.toastController.create({
              message, duration: 3000, color: 'danger', position: 'top',
            });
            await toast.present();
          },
        });
    } else {
      doRegister(null);
    }
  }
}