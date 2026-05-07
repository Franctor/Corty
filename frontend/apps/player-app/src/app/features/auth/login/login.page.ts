import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UiInputComponent } from '../../../components/forms/ui-input/ui-input.component';
import { UiPasswordComponent } from '../../../components/forms/ui-password/ui-password.component';
import { CortyLogoComponent } from '@frontend/shared-ui';
import { IonButton, IonSpinner, ToastController, IonContent } from '@ionic/angular/standalone';
import { AuthService } from '@frontend/shared-auth';
import { CortyValidators, getFirstError } from '@frontend/shared-core';
@Component({
  selector: 'app-login',
  templateUrl: 'login.page.html',
  styleUrls: ['login.page.scss'],
  standalone: true,
  imports: [IonContent,
    ReactiveFormsModule,
    IonButton,
    IonSpinner,
    UiInputComponent,
    UiPasswordComponent,
    CortyLogoComponent,
    RouterLink],
})
export class LoginPage implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toastController = inject(ToastController);
  protected getFirstError = getFirstError;
  isLoading = signal(false);

  ngOnInit(): void {
    if (this.route.snapshot.queryParamMap.get('activated') === 'already') {
      this.toastController.create({
        message: 'Tu cuenta ya está activada. Inicia sesión.',
        duration: 4000,
        color: 'success',
        position: 'top',
      }).then(toast => toast.present());
    }
  }

  form: FormGroup = this.fb.group({
    username: ['', [Validators.required, CortyValidators.noWhitespace]],
    password: ['', [Validators.required]],
  });

  get username() { return this.form.get('username') as FormControl; }
  get password() { return this.form.get('password') as FormControl; }

  onSubmit(): void {
    if (this.form.invalid || this.isLoading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/home']);
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