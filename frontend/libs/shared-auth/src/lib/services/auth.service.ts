import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { TokenService } from './token.service';
import { AuthResponse, LoginRequest, RegisterRequest } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private tokenService = inject(TokenService);

  // Cada app inyecta su propio apiUrl mediante el token API_URL
  // Ver shared-auth/src/lib/auth.providers.ts
  private apiUrl = inject(AUTH_API_URL);

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
        })
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
}

// InjectionToken para que cada app pueda pasar su propio apiUrl
import { InjectionToken } from '@angular/core';
export const AUTH_API_URL = new InjectionToken<string>('AUTH_API_URL');