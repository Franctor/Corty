import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AvatarPickerComponent, WizardComponent } from '@frontend/shared-ui';
import { Router, RouterLink } from '@angular/router';
import {
  IonContent, IonButton, IonInput, IonInputPasswordToggle,
  IonSpinner, IonSelect, IonSelectOption, ToastController,
} from '@ionic/angular/standalone';
import { AuthService } from '@frontend/shared-auth';
import { LocationService, CortyValidators, getFirstError } from '@frontend/shared-core';
import { CityResponse, ProvinceResponse } from '@frontend/shared-core';
import { MediaService } from '@frontend/shared-core';
import { BreakpointService } from '@frontend/shared-ui';
import { CortyLogoComponent } from '@frontend/shared-ui';
import { UiInputComponent } from "../../../components/forms/ui-input/ui-input.component";
import { UiPasswordComponent } from "../../../components/forms/ui-password/ui-password.component";
import { UiPasswordChecklistComponent } from "../../../components/forms/ui-password-checklist/ui-password-checklist.component";
import { UiSelectComponent } from "../../../components/forms/ui-select/ui-select.component";
import { UiDatepickerComponent } from "../../../components/forms/ui-datepicker/ui-datepicker.component";
import { UiAutocompleteComponent } from "../../../components/forms/ui-autocomplete/ui-autocomplete.component";
import { UiTextareaComponent } from "../../../components/forms/ui-textarea/ui-textarea.component";

@Component({
  selector: 'app-register',
  templateUrl: 'register.page.html',
  styleUrls: ['register.page.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterLink,
    IonContent, IonButton,
    IonSpinner, AvatarPickerComponent,
    CortyLogoComponent,
    UiInputComponent,
    UiPasswordComponent,
    UiPasswordChecklistComponent,
    UiSelectComponent,
    UiDatepickerComponent,
    UiAutocompleteComponent,
    UiTextareaComponent,
    WizardComponent
],
})
export class RegisterPage implements OnInit {
  private bp = inject(BreakpointService);
  readonly selectInterface = computed<'popover' | 'action-sheet'>(() =>
    this.bp.isTablet() ? 'popover' : 'action-sheet'
  );
  get selectOptions() {
    return {
      cssClass: 'corty-select',
      size: 'cover'
    };
  }
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private locationService = inject(LocationService);
  private router = inject(Router);
  private toastController = inject(ToastController);
  private mediaService = inject(MediaService);


  protected getFirstError = getFirstError;

  readonly totalSteps = 2;
  readonly currentStep = signal(1);
  readonly avatarFile = signal<File | null>(null);

  readonly isLoading = signal(false);

  readonly provinces = signal<ProvinceResponse[]>([]);
  readonly cities = signal<CityResponse[]>([]);
  readonly cityDisabled = computed(() => !this.provinceCode.value);


  readonly genderOptions = [
    { value: 'MALE', label: 'Masculino' },
    { value: 'FEMALE', label: 'Femenino' },
    { value: 'OTHER', label: 'Otro' },
  ];

  readonly provinceOptions = computed(() =>
    this.provinces().map(p => ({ value: p.code, label: p.label }))
  );

  readonly cityOptions = computed(() =>
    this.cities().map(c => ({ value: c.idCity, label: c.label }))
  );
  // --- Step 1: Account ---
  readonly step1: FormGroup = this.fb.group(
    {
      username: ['', [Validators.required, Validators.minLength(3), CortyValidators.noWhitespace]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, CortyValidators.strongPassword]],
      confirmPassword: ['', Validators.required],
    },
    { validators: CortyValidators.passwordMatch('password', 'confirmPassword') }
  );

  // --- Step 2: Profile ---
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

  // --- Step 1 getters ---
  get username() { return this.step1.get('username')!; }
  get email() { return this.step1.get('email')!; }
  get password() { return this.step1.get('password')!; }
  get confirmPassword() { return this.step1.get('confirmPassword')!; }
  get passwordMatch() { return this.step1.errors?.['passwordMatch'] && this.confirmPassword.touched; }

  // --- Step 2 getters ---
  get name() { return this.step2.get('name')!; }
  get surname() { return this.step2.get('surname')!; }
  get phone() { return this.step2.get('phone')!; }
  get gender() { return this.step2.get('gender')!; }
  get birthDate() { return this.step2.get('birthDate')!; }
  get biography() { return this.step2.get('biography')!; }
  get provinceCode() { return this.step2.get('provinceCode')!; }
  get cityId() { return this.step2.get('cityId')!; }

  // Max date for birthDate — must be 18 years ago from today
  get maxBirthDate(): string {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    return d.toISOString().split('T')[0];
  }

  ngOnInit(): void {
    this.locationService.getProvinces().subscribe(p => this.provinces.set(p));
    this.provinceCode.valueChanges.subscribe(code => this.onProvinceChange(code));

  }

  onProvinceChange(provinceCode: string): void {
    this.step2.patchValue({ cityId: null });
    this.cities.set([]);
    if (!provinceCode) return;
    this.locationService.getCitiesByProvince(provinceCode).subscribe(c => this.cities.set(c));
  }

  onAvatarSelected(file: File): void {
    this.avatarFile.set(file);
  }


  goNext(): void {
    if (this.step1.invalid) {
      this.step1.markAllAsTouched();
      return;
    }
    this.currentStep.set(2);
  }

  goBack(): void {
    this.currentStep.set(1);
  }

  onSubmit(): void {
    if (this.step2.invalid || this.isLoading()) {
      this.step2.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const { username, email, password } = this.step1.value;
    const { name, surname, phone, gender, birthDate, biography, cityId } = this.step2.value;

    const doRegister = (avatarUrl: string | null) => {
      this.authService.register({
        username, email, password,
        name, surname, phone, gender, birthDate, biography, cityId,
        avatarUrl,
      }).subscribe({
        next: () => {
          this.isLoading.set(false);
          this.router.navigate(['/home']);
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
      // Upload avatar first, then register with the returned URL
      this.mediaService.uploadAvatar(file).subscribe({
        next: (url) => doRegister(url),
        error: async (err) => {
          this.isLoading.set(false);
          const message = err?.error?.message ?? 'Error al subir la imagen';
          const toast = await this.toastController.create({
            message, duration: 3000, color: 'danger', position: 'top',
          });
          await toast.present();
        },
      });
    } else {
      // No avatar selected — register without it
      doRegister(null);
    }
  }
}