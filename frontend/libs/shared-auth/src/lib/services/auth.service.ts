import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { TokenService } from './token.service';
import { AuthResponse, LoginRequest, RegisterRequest } from '@frontend/shared-core';
import { API_URL } from '@frontend/shared-core'
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http         = inject(HttpClient);
  private router       = inject(Router);
  private tokenService = inject(TokenService);
  private apiUrl       = inject(API_URL);

  // Callback opcional inyectado desde la app para enviar el FCM token tras login
  onLoginSuccess?: () => void;

  isLoggedIn = signal<boolean>(
    this.tokenService.isPresent() && !this.tokenService.isExpired()
  );

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/auth/login`, request)
      .pipe(
        tap((res) => {
          this.tokenService.save(res.token);
          this.isLoggedIn.set(true);
          this.onLoginSuccess?.();
        })
      );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/auth/register`, request)
      .pipe(
        tap((res) => {
          this.tokenService.save(res.token);
          this.isLoggedIn.set(true);
          this.onLoginSuccess?.();
        })
      );
  }

  deleteAccount(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/auth/me`).pipe(
      tap(() => this.logout())
    );
  }

  logout(redirectTo = '/auth/login'): void {
    this.tokenService.remove();
    this.isLoggedIn.set(false);
    this.router.navigate([redirectTo]);
  }

  getRole(): string | null {
    return this.tokenService.getRole();
  }

  getUserId(): number | null {
    return this.tokenService.getUserId();
  }

  hasAuthority(authority: string): boolean {
    return this.tokenService.hasAuthority(authority);
  }
}
