import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

/** Protege rutas privadas. Redirige a /auth/login si no hay sesión válida. */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (authService.isLoggedIn() && !tokenService.isExpired()) {
    return true;
  }
  tokenService.remove();
  return router.createUrlTree(['/auth/login']);
};

/** Evita que un usuario autenticado vea /auth/login o /auth/register. */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (authService.isLoggedIn() && !tokenService.isExpired()) {
    return router.createUrlTree(['/tabs/tab1']); // player-app home
  }
  return true;
};

/**
 * Protege rutas por rol.
 * Uso: { canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } }
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const requiredRoles: string[] = route.data['roles'] ?? [];
  const userRole = authService.getRole();

  if (userRole && requiredRoles.includes(userRole)) {
    return true;
  }
  return router.createUrlTree(['/forbidden']);
};