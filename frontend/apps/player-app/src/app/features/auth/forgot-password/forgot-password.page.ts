import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { IonButton, IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { AuthService } from '@frontend/shared-auth';
import { CortyLogoComponent } from '@frontend/shared-ui';
import { UiInputComponent } from '../../../components/forms/ui-input/ui-input.component';

@Component({
  selector: 'app-forgot-password',
  templateUrl: 'forgot-password.page.html',
  styleUrls: ['forgot-password.page.scss'],
  standalone: true,
  imports: [IonButton, IonContent, IonSpinner, ReactiveFormsModule, RouterLink, CortyLogoComponent, UiInputComponent],
})
export class ForgotPasswordPage {
  private fb          = inject(FormBuilder);
  private authService = inject(AuthService);
  private toast       = inject(ToastController);

  readonly loading = signal(false);
  readonly sent    = signal(false);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });

  get email() { return this.form.get('email') as FormControl; }

  onSubmit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.authService.forgotPassword(this.email.value).subscribe({
      next: () => {
        this.loading.set(false);
        this.sent.set(true);
      },
      error: () => {
        this.loading.set(false);
        // Mostramos el mismo mensaje aunque falle para no revelar si el email existe
        this.sent.set(true);
      },
    });
  }
}
