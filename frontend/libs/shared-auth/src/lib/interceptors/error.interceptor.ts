import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { TokenService } from '../services/token.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const tokenService = inject(TokenService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      switch (error.status) {
        case 401:
          // Don't redirect if the user is trying to log in
          if (!req.url.includes('/auth/login') && !req.url.includes('/auth/register')) {
            tokenService.remove();
            router.navigate(['/auth/login']);
          }
          break;
        case 403:
          router.navigate(['/forbidden']);
          break;
        case 0:
          console.error('[Corty] Sin conexión con el servidor');
          break;
        default:
          console.error(`[Corty] Error ${error.status}:`, error.message);
      }
      return throwError(() => error);
    })
  );
};