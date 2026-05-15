import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { IonButton, IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { AuthService } from '@frontend/shared-auth';
import { CortyValidators } from '@frontend/shared-core';
import { CortyLogoComponent } from '@frontend/shared-ui';
import { UiPasswordChecklistComponent } from '../../../components/forms/ui-password-checklist/ui-password-checklist.component';
import { UiPasswordComponent } from '../../../components/forms/ui-password/ui-password.component';

@Component({
  selector: 'app-reset-password',
  templateUrl: 'reset-password.page.html',
  styleUrls: ['reset-password.page.scss'],
  standalone: true,
  imports: [IonButton, IonContent, IonSpinner, ReactiveFormsModule, RouterLink, CortyLogoComponent, UiPasswordComponent, UiPasswordChecklistComponent],
})
export class ResetPasswordPage implements OnInit {
  private fb          = inject(FormBuilder);
  private authService = inject(AuthService);
  private route       = inject(ActivatedRoute);
  private router      = inject(Router);
  private toast       = inject(ToastController);

  readonly loading  = signal(false);
  readonly done     = signal(false);
  private token     = '';

  readonly form = this.fb.group(
    {
      newPassword:     ['', [Validators.required, CortyValidators.strongPassword]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: CortyValidators.passwordMatch('newPassword', 'confirmPassword') }
  );

  get newPassword()     { return this.form.get('newPassword') as FormControl; }
  get confirmPassword() { return this.form.get('confirmPassword') as FormControl; }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.router.navigate(['/auth/login']);
    }
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.authService.resetPassword(this.token, this.newPassword.value).subscribe({
      next: () => {
        this.loading.set(false);
        this.done.set(true);
      },
      error: (err) => {
        this.loading.set(false);
        const message = err?.error?.message ?? 'El enlace no es válido o ha expirado';
        this.showToast(message);
      },
    });
  }

  private async showToast(message: string): Promise<void> {
    const t = await this.toast.create({ message, duration: 3500, color: 'danger', position: 'top' });
    await t.present();
  }
}
