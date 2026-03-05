import { Injectable } from '@angular/core';

const TOKEN_KEY = 'corty_token';

@Injectable({ providedIn: 'root' })
export class TokenService {

  save(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  get(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  remove(): void {
    localStorage.removeItem(TOKEN_KEY);
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
}