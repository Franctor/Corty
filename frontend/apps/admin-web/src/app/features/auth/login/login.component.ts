import { Component, inject, signal, computed } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '@frontend/shared-auth';
import { CortyValidators, getFirstError } from '@frontend/shared-core';
import { CortyLogoComponent } from '@frontend/shared-ui';

@Component({
  selector: 'app-login',
  templateUrl: 'login.component.html',
  styleUrl: 'login.component.scss',
  standalone: true,
  imports: [ReactiveFormsModule, CortyLogoComponent],
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  protected getFirstError = getFirstError;

  readonly isLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form: FormGroup = this.fb.group({
    username: ['', [Validators.required, CortyValidators.noWhitespace]],
    password: ['', [Validators.required]],
  });

  get username(): FormControl {
    return this.form.get('username') as FormControl;
  }

  get password(): FormControl {
    return this.form.get('password') as FormControl;
  }

  readonly canSubmit = computed(() => !this.isLoading());

  onSubmit(): void {
    if (this.form.invalid || this.isLoading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.isLoading.set(false);
        const role = this.authService.getRole();
        if (role === 'ADMIN' || role === 'SUPERADMIN' || role === 'ORGANIZATION') {
          this.router.navigate(['/dashboard']);
        } else {
          this.authService.logout('/forbidden');
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(
          err?.error?.message ?? 'Usuario o contraseña incorrectos'
        );
      },
    });
  }
}
