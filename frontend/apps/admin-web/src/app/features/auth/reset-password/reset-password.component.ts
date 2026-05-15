import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '@frontend/shared-auth';
import { CortyValidators } from '@frontend/shared-core';
import { CortyLogoComponent } from '@frontend/shared-ui';

@Component({
  selector: 'app-reset-password',
  templateUrl: 'reset-password.component.html',
  styleUrl: 'reset-password.component.scss',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CortyLogoComponent],
})
export class ResetPasswordComponent implements OnInit {
  private fb          = inject(FormBuilder);
  private authService = inject(AuthService);
  private route       = inject(ActivatedRoute);
  private router      = inject(Router);

  readonly loading = signal(false);
  readonly done    = signal(false);
  readonly error   = signal('');
  private token    = '';

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
    if (this.form.hasError('passwordMatch')) {
      this.error.set('Las contraseñas no coinciden');
      return;
    }
    this.error.set('');
    this.loading.set(true);
    this.authService.resetPassword(this.token, this.newPassword.value).subscribe({
      next:  () => { this.loading.set(false); this.done.set(true); },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'El enlace no es válido o ha expirado');
      },
    });
  }
}
