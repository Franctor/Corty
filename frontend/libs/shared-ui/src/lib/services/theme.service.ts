import { effect, inject, Injectable, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';

export type ThemeMode = 'light' | 'dark' | 'system';
const STORAGE_KEY = 'corty-theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly html = inject(DOCUMENT).documentElement;

  readonly mode   = signal<ThemeMode>(this.loadPreference());
  readonly isDark = signal<boolean>(this.resolveIsDark(this.mode()));

  constructor() {
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    mq.addEventListener('change', ({ matches }) => {
      if (this.mode() === 'system') this.apply(matches);
    });

    effect(() => {
      const dark = this.resolveIsDark(this.mode());
      this.apply(dark);
      this.savePreference(this.mode());
    });
  }

  toggle(): void {
    this.mode.set(this.isDark() ? 'light' : 'dark');
  }

  setMode(mode: ThemeMode): void {
    this.mode.set(mode);
  }

  private apply(dark: boolean): void {
    this.isDark.set(dark);
    this.html.classList.remove('light-theme', 'dark-theme');
    this.html.classList.add(dark ? 'dark-theme' : 'light-theme');
  }

  private resolveIsDark(mode: ThemeMode): boolean {
    if (mode === 'dark')  return true;
    if (mode === 'light') return false;
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  private loadPreference(): ThemeMode {
    return (localStorage.getItem(STORAGE_KEY) as ThemeMode | null) ?? 'system';
  }

  private savePreference(mode: ThemeMode): void {
    localStorage.setItem(STORAGE_KEY, mode);
  }
}