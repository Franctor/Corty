import { effect, inject, Injectable, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';

export type ThemeMode = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'corty-theme';

// Concrete values per theme — no var() references so shadow DOM resolves them correctly.
// Must mirror _variables.scss and ionic-overrides.scss token values.
const TOKENS = {
  light: {
    '--color-bg-base':          '#FFFFFF',
    '--color-bg-surface':       '#F7F7F7',
    '--color-bg-elevated':      '#FFFFFF',
    '--color-text-primary':     '#1A1A1A',
    '--color-text-secondary':   '#6B6B6B',
    '--color-text-muted':       '#AFAFAF',
    '--color-text-inverse':     '#FFFFFF',
    '--color-border':           '#E5E5E5',
    '--color-border-strong':    '#CCCCCC',
    // Ionic shadow DOM vars — concrete values required
    '--ion-background-color':       '#FFFFFF',
    '--ion-background-color-rgb':   '255, 255, 255',
    '--ion-text-color':             '#1A1A1A',
    '--ion-text-color-rgb':         '26, 26, 26',
    '--ion-border-color':           '#E5E5E5',
    '--ion-card-background':        '#FFFFFF',
    '--ion-item-background':        '#F7F7F7',
    '--ion-toolbar-background':     '#FFFFFF',
    '--ion-tab-bar-background':     '#FFFFFF',
    '--ion-tab-bar-border-color':   '#E5E5E5',
  },
  dark: {
    '--color-bg-base':          '#131F24',
    '--color-bg-surface':       '#1C2B33',
    '--color-bg-elevated':      '#233240',
    '--color-text-primary':     '#FFFFFF',
    '--color-text-secondary':   '#A0B3BF',
    '--color-text-muted':       '#5C7A8A',
    '--color-text-inverse':     '#1A1A1A',
    '--color-border':           'rgba(255, 255, 255, 0.08)',
    '--color-border-strong':    'rgba(255, 255, 255, 0.16)',
    // Ionic shadow DOM vars — concrete values required
    '--ion-background-color':       '#131F24',
    '--ion-background-color-rgb':   '19, 31, 36',
    '--ion-text-color':             '#FFFFFF',
    '--ion-text-color-rgb':         '255, 255, 255',
    '--ion-border-color':           'rgba(255, 255, 255, 0.08)',
    '--ion-card-background':        '#1C2B33',
    '--ion-item-background':        '#1C2B33',
    '--ion-toolbar-background':     '#233240',
    '--ion-tab-bar-background':     '#233240',
    '--ion-tab-bar-border-color':   'rgba(255, 255, 255, 0.08)',
  },
} as const;

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly html = inject(DOCUMENT).documentElement;

  readonly mode   = signal<ThemeMode>(this.loadPreference());
  readonly isDark = signal<boolean>(this.resolveIsDark(this.mode()));

  constructor() {
    // React to OS preference changes when mode is 'system'
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    mq.addEventListener('change', ({ matches }) => {
      if (this.mode() === 'system') this.apply(matches);
    });

    // Apply on every mode signal change
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

  // ── Private ──────────────────────────────────────────────

  private apply(dark: boolean): void {
    this.isDark.set(dark);

    // Write tokens directly as inline styles on <html>.
    // Ionic overwrites classList on mobile but never touches inline style —
    // and inline style on :root is inherited by all shadow DOM components.
    const tokens = dark ? TOKENS.dark : TOKENS.light;
    Object.entries(tokens).forEach(([prop, value]) =>
      this.html.style.setProperty(prop, value)
    );
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