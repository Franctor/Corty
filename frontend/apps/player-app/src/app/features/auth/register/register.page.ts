import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  IonContent,
  IonButton,
  IonInput,
  IonInputPasswordToggle,
  IonSpinner,
  ToastController,
} from '@ionic/angular/standalone';
import { AuthService } from '@frontend/shared-auth';

// Validador personalizado: confirmar contraseña
function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirm = control.get('confirmPassword')?.value;
  return password === confirm ? null : { passwordMismatch: true };
}

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
    IonInput,
    IonInputPasswordToggle,
    IonSpinner,
  ],
})
export class RegisterPage {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastController = inject(ToastController);

  isLoading = false;

  form: FormGroup = this.fb.group(
    {
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatchValidator }
  );

  get username() { return this.form.get('username')!; }
  get email() { return this.form.get('email')!; }
  get password() { return this.form.get('password')!; }
  get confirmPassword() { return this.form.get('confirmPassword')!; }
  get passwordMismatch() { return this.form.errors?.['passwordMismatch'] && this.confirmPassword.touched; }

  onSubmit(): void {
    if (this.form.invalid || this.isLoading) return;

    this.isLoading = true;
    const { username, email, password } = this.form.value;

    this.authService.register({ username, email, password }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/tabs/tab1']);
      },
      error: async (err) => {
        this.isLoading = false;
        const message = err?.error?.message ?? 'Error al registrarse. Inténtalo de nuevo.';
        const toast = await this.toastController.create({
          message,
          duration: 3000,
          color: 'danger',
          position: 'top',
        });
        await toast.present();
      },
    });
  }
}