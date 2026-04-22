import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '../services/token.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const token = tokenService.get();
  const hasValidToken = token != null && !tokenService.isExpired();
  const outgoingRequest = hasValidToken
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;
  return next(outgoingRequest);
};