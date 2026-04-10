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
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  isExpired(): boolean {
    const payload = this.decode();
    if (!payload || typeof payload['exp'] !== 'number') return true;
    return Date.now() >= payload['exp'] * 1000;
  }

  getRole(): string | null {
    const payload = this.decode();
    if (!payload || !Array.isArray(payload['authorities'])) return null;
    const roleAuthority = (payload['authorities'] as string[]).find((a) =>
      a.startsWith('ROLE_')
    );
    return roleAuthority ? roleAuthority.replace('ROLE_', '') : null;
  }

  getUserId(): number | null {
    const payload = this.decode();
    return payload ? (payload['id'] as number) : null;
  }

  getAuthorities(): string[] {
    const payload = this.decode();
    if (!payload || !Array.isArray(payload['authorities'])) return [];
    return payload['authorities'] as string[];
  }

  hasAuthority(authority: string): boolean {
    return this.getAuthorities().includes(authority);
  }
}