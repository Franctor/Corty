import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AvatarPickerComponent } from '@frontend/shared-ui';
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

@Component({
  selector: 'app-register',
  templateUrl: 'register.page.html',
  styleUrls: ['register.page.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterLink,
    IonContent, IonButton, IonInput, IonInputPasswordToggle,
    IonSpinner, IonSelect, IonSelectOption, AvatarPickerComponent
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

  // --- Wizard state ---
  readonly totalSteps = 2;
  readonly currentStep = signal(1);
  readonly avatarFile = signal<File | null>(null);

  // --- UI state ---
  readonly isLoading = signal(false);

  // --- Location signals ---
  readonly provinces = signal<ProvinceResponse[]>([]);
  readonly cities = signal<CityResponse[]>([]);
  readonly citySearchTerm = signal('');

  // Derived: filters cities client-side as user types
  readonly filteredCities = computed(() => {
    const term = this.citySearchTerm().toLowerCase();
    if (!term) return this.cities();
    return this.cities().filter(c => c.label.toLowerCase().includes(term));
  });

  // Derived: shows dropdown only when there are results and no city selected yet
  readonly showCityDropdown = computed(() =>
    this.filteredCities().length > 0 &&
    this.citySearchTerm().length > 0 &&
    !this.cityId.value
  );

  readonly citySelected = signal(false);
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
  }

  onProvinceChange(provinceCode: string): void {
    this.step2.patchValue({ cityId: null });
    this.cities.set([]);
    this.citySearchTerm.set('');
    if (!provinceCode) return;
    this.locationService.getCitiesByProvince(provinceCode).subscribe(c => this.cities.set(c));
  }

  onCitySearch(term: string): void {
    this.citySearchTerm.set(term);
    this.citySelected.set(false);
    if (this.cityId.value) {
      this.step2.patchValue({ cityId: null });
    }
  }

  onAvatarSelected(file: File): void {
    this.avatarFile.set(file);
  }

  selectCity(city: CityResponse): void {
    this.step2.patchValue({ cityId: city.idCity });
    this.citySearchTerm.set(city.label);
    this.citySelected.set(true);
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