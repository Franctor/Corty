import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButton,
  IonInput,
  IonInputPasswordToggle,
  IonItem,
  IonLabel,
  IonSpinner,
  IonText,
  ToastController,
} from '@ionic/angular/standalone';
import { AuthService } from '@frontend/shared-auth';

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

  isLoading = false;

  form: FormGroup = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(1)]],
  });

  get username() { return this.form.get('username')!; }
  get password() { return this.form.get('password')!; }

  onSubmit(): void {
    if (this.form.invalid || this.isLoading) return;

    this.isLoading = true;

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/tabs/tab1']);
      },
      error: async (err) => {
        this.isLoading = false;
        const message = err?.error?.message ?? 'Usuario o contraseña incorrectos';
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