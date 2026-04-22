import { Injectable, signal } from '@angular/core';

const TOKEN_KEY = 'corty_token';

@Injectable({ providedIn: 'root' })
export class TokenService {

  // Signal reactiva — los computed que lean token() se recalculan al cambiar
  readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  save(value: string): void {
    localStorage.setItem(TOKEN_KEY, value);
    this.token.set(value);
  }

  get(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  remove(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.token.set(null);
  }

  isPresent(): boolean {
    return !!this.get();
  }

  decode(): Record<string, unknown> | null {
    const token = this.get();
    let result: Record<string, unknown> | null = null;
    if (token) {
      try {
        result = JSON.parse(atob(token.split('.')[1]));
      } catch {
        result = null;
      }
    }
    return result;
  }

  isExpired(): boolean {
    const payload = this.decode();
    const hasValidExp = payload != null && typeof payload['exp'] === 'number';
    return !hasValidExp || Date.now() >= (payload!['exp'] as number) * 1000;
  }

  getRole(): string | null {
    const payload = this.decode();
    const hasAuthorities = payload != null && Array.isArray(payload['authorities']);
    const roleAuthority = hasAuthorities
      ? (payload!['authorities'] as string[]).find((authority) => authority.startsWith('ROLE_'))
      : undefined;
    return roleAuthority ? roleAuthority.replace('ROLE_', '') : null;
  }

  getUserId(): number | null {
    const payload = this.decode();
    return payload ? (payload['id'] as number) : null;
  }

  getAuthorities(): string[] {
    const payload = this.decode();
    const hasAuthorities = payload != null && Array.isArray(payload['authorities']);
    return hasAuthorities ? (payload!['authorities'] as string[]) : [];
  }

  hasAuthority(authority: string): boolean {
    return this.getAuthorities().includes(authority);
  }
}