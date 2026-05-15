import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '@frontend/shared-auth';
import { CortyLogoComponent } from '@frontend/shared-ui';

@Component({
  selector: 'app-forgot-password',
  templateUrl: 'forgot-password.component.html',
  styleUrl: 'forgot-password.component.scss',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CortyLogoComponent],
})
export class ForgotPasswordComponent {
  private fb          = inject(FormBuilder);
  private authService = inject(AuthService);

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
      next:  () => { this.loading.set(false); this.sent.set(true); },
      error: () => { this.loading.set(false); this.sent.set(true); },
    });
  }
}
