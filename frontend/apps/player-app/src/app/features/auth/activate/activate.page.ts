import { Component, OnInit, OnDestroy, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subject, takeUntil } from 'rxjs';
import { IonContent, IonButton, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { CortyLogoComponent } from '@frontend/shared-ui';
import { TokenService } from '@frontend/shared-auth';
import { LocationService, CortyValidators, CityResponse, ProvinceResponse } from '@frontend/shared-core';
import { environment } from '../../../../environments/environment';
import { UiInputComponent } from '../../../components/forms/ui-input/ui-input.component';
import { UiPasswordComponent } from '../../../components/forms/ui-password/ui-password.component';
import { UiSelectComponent } from '../../../components/forms/ui-select/ui-select.component';
import { UiAutocompleteComponent } from '../../../components/forms/ui-autocomplete/ui-autocomplete.component';
import { UiTextareaComponent } from '../../../components/forms/ui-textarea/ui-textarea.component';
import { UiDatepickerComponent } from '../../../components/forms/ui-datepicker/ui-datepicker.component';

type ActivationStep = 'loading' | 'invalid' | 'confirm' | 'complete-profile' | 'complete-org' | 'success';

interface ActivationInfoResponse {
  token: string;
  username: string;
  email: string;
  needsProfile: boolean;
  userType: 'PLAYER' | 'ORGANIZATION';
}

@Component({
  selector: 'app-activate',
  templateUrl: 'activate.page.html',
  styleUrls: ['activate.page.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterLink,
    IonContent, IonButton, IonSpinner,
    CortyLogoComponent,
    UiInputComponent, UiPasswordComponent, UiSelectComponent,
    UiAutocompleteComponent, UiTextareaComponent, UiDatepickerComponent,
  ],
})
export class ActivatePage implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly tokenService = inject(TokenService);
  private readonly toastController = inject(ToastController);
  private readonly locationService = inject(LocationService);

  private readonly destroy$ = new Subject<void>();

  readonly step = signal<ActivationStep>('loading');
  readonly activationUsername = signal('');
  readonly isSaving = signal(false);

  readonly provinces = signal<ProvinceResponse[]>([]);
  readonly cities = signal<CityResponse[]>([]);

  readonly provinceOptions = computed(() =>
    this.provinces().map(province => ({ value: province.code, label: province.label }))
  );
  readonly cityOptions = computed(() =>
    this.cities().map(city => ({ value: city.idCity, label: city.label }))
  );

  readonly genderOptions = [
    { value: 'MALE',   label: 'Masculino' },
    { value: 'FEMALE', label: 'Femenina' },
    { value: 'OTHER',  label: 'Otro' },
  ];

  get maxBirthDate(): string {
    const date = new Date();
    date.setFullYear(date.getFullYear() - 16);
    return date.toISOString().split('T')[0];
  }

  private activationToken = '';

  readonly profileForm: FormGroup = this.fb.group({
    username:        ['', [Validators.required, Validators.maxLength(50)]],
    name:            ['', [Validators.required, Validators.maxLength(50)]],
    surname:         ['', [Validators.required, Validators.maxLength(50)]],
    phone:           ['', [Validators.required, CortyValidators.phoneEs]],
    gender:          ['', Validators.required],
    birthDate:       ['', [Validators.required, CortyValidators.minAge(16)]],
    biography:       ['', Validators.maxLength(500)],
    provinceCode:    ['', Validators.required],
    cityId:          [null, Validators.required],
    password:        ['', [Validators.required, CortyValidators.strongPassword]],
    confirmPassword: ['', Validators.required],
  }, { validators: CortyValidators.passwordMatch('password', 'confirmPassword') });

  readonly orgForm: FormGroup = this.fb.group({
    username:        ['', [Validators.required, Validators.maxLength(50)]],
    businessName:    ['', [Validators.required, Validators.maxLength(100)]],
    cif:             ['', [Validators.required, Validators.maxLength(9)]],
    provinceCode:    [''],
    fiscalCityId:    [null],
    password:        ['', [Validators.required, CortyValidators.strongPassword]],
    confirmPassword: ['', Validators.required],
  }, { validators: CortyValidators.passwordMatch('password', 'confirmPassword') });

  ngOnInit(): void {
    this.activationToken = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.activationToken) {
      this.step.set('invalid');
    } else {
      this.http.get<ActivationInfoResponse>(
        `${environment.apiUrl}/auth/activate?token=${this.activationToken}`
      ).subscribe({
        next: (info) => {
          this.activationUsername.set(info.username);
          if (info.needsProfile && info.userType === 'PLAYER') {
            this.profileForm.patchValue({ username: info.username });
            this.profileForm.get('cityId')!.disable();
            this.loadProvinces();
            this.watchProvinceChanges();
            this.step.set('complete-profile');
          } else if (info.needsProfile && info.userType === 'ORGANIZATION') {
            this.orgForm.patchValue({ username: info.username });
            this.orgForm.get('fiscalCityId')!.disable();
            this.loadProvinces();
            this.watchOrgProvinceChanges();
            this.step.set('complete-org');
          } else {
            this.step.set('confirm');
          }
        },
        error: (err) => {
          if (err.status === 409) {
            this.router.navigate(['/auth/login'], { queryParams: { activated: 'already' } });
          } else {
            this.step.set('invalid');
          }
        },
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadProvinces(): void {
    this.locationService.getProvinces()
      .pipe(takeUntil(this.destroy$))
      .subscribe(provinceList => this.provinces.set(provinceList));
  }

  private watchProvinceChanges(): void {
    this.profileForm.get('provinceCode')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(selectedProvinceCode => this.onProvinceChange(selectedProvinceCode));
  }

  private onProvinceChange(provinceCode: string): void {
    const cityControl = this.profileForm.get('cityId')!;
    this.profileForm.patchValue({ cityId: null });
    this.cities.set([]);
    if (!provinceCode) {
      cityControl.disable();
    } else {
      cityControl.enable();
      this.locationService.getCitiesByProvince(provinceCode)
        .pipe(takeUntil(this.destroy$))
        .subscribe(cityList => this.cities.set(cityList));
    }
  }

  private watchOrgProvinceChanges(): void {
    this.orgForm.get('provinceCode')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(selectedProvinceCode => this.onOrgProvinceChange(selectedProvinceCode));
  }

  private onOrgProvinceChange(provinceCode: string): void {
    const cityControl = this.orgForm.get('fiscalCityId')!;
    this.orgForm.patchValue({ fiscalCityId: null });
    this.cities.set([]);
    if (!provinceCode) {
      cityControl.disable();
    } else {
      cityControl.enable();
      this.locationService.getCitiesByProvince(provinceCode)
        .pipe(takeUntil(this.destroy$))
        .subscribe(cityList => this.cities.set(cityList));
    }
  }

  confirmActivation(): void {
    if (!this.isSaving()) {
      this.isSaving.set(true);
      this.http.post<{ token: string }>(
        `${environment.apiUrl}/auth/activate`,
        { token: this.activationToken }
      ).subscribe({
        next: (response) => {
          this.tokenService.save(response.token);
          this.isSaving.set(false);
          this.step.set('success');
        },
        error: () => {
          this.isSaving.set(false);
          this.showErrorToast();
        },
      });
    }
  }

  submitOrgProfile(): void {
    if (this.orgForm.invalid || this.isSaving()) {
      this.orgForm.markAllAsTouched();
    } else {
      this.isSaving.set(true);
      const { provinceCode, confirmPassword: _confirmPassword, ...orgData } = this.orgForm.getRawValue();
      this.http.post<{ token: string }>(
        `${environment.apiUrl}/auth/activate`,
        { token: this.activationToken, ...orgData }
      ).subscribe({
        next: (response) => {
          this.tokenService.save(response.token);
          this.isSaving.set(false);
          this.step.set('success');
        },
        error: () => {
          this.isSaving.set(false);
          this.showErrorToast();
        },
      });
    }
  }

  submitProfile(): void {
    if (this.profileForm.invalid || this.isSaving()) {
      this.profileForm.markAllAsTouched();
    } else {
      this.isSaving.set(true);
      const { provinceCode, confirmPassword: _confirmPassword, ...profileData } = this.profileForm.getRawValue();
      if (profileData.phone) {
        profileData.phone = (profileData.phone as string).replace(/[\s.\-()]/g, '');
      }
      this.http.post<{ token: string }>(
        `${environment.apiUrl}/auth/activate`,
        { token: this.activationToken, ...profileData }
      ).subscribe({
        next: (response) => {
          this.tokenService.save(response.token);
          this.isSaving.set(false);
          this.step.set('success');
        },
        error: () => {
          this.isSaving.set(false);
          this.showErrorToast();
        },
      });
    }
  }

  goToApp(): void {
    this.router.navigate(['/home']);
  }

  private showErrorToast(): void {
    this.toastController.create({
      message: 'Error al activar la cuenta. Inténtalo de nuevo.',
      duration: 3000,
      color: 'danger',
      position: 'top',
    }).then(toast => toast.present());
  }
}
