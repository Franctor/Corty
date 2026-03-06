import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
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
import { CortyValidators, getFirstError } from '@frontend/shared-core';

@Component({
  selector: 'app-login',
  templateUrl: 'login.page.html',
  styleUrls: ['login.page.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    IonContent,
    IonButton,
    IonInput,
    IonInputPasswordToggle,
    IonSpinner
  ],
})
export class LoginPage {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastController = inject(ToastController);
  protected getFirstError = getFirstError;
  isLoading = signal(false);

  form: FormGroup = this.fb.group({
    username: ['', [Validators.required, CortyValidators.noWhitespace]],
    password: ['', [Validators.required]],
  });

  get username() { return this.form.get('username')!; }
  get password() { return this.form.get('password')!; }

  onSubmit(): void {
    if (this.form.invalid || this.isLoading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/tabs/tab1']);
      },
      error: (err) => {
        this.isLoading.set(false);
        const message = err?.error?.message ?? 'Usuario o contraseña incorrectos';
        this.toastController.create({
          message,
          duration: 3000,
          color: 'danger',
          position: 'top',
        }).then(toast => toast.present());
      },
    });
  }
}