import { inject, Injectable, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BreakpointObserver } from '@angular/cdk/layout';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

const BREAKPOINT_MD = '(min-width: 768px)';
const BREAKPOINT_LG = '(min-width: 1024px)';

function matchesNow(query: string): boolean {
  return typeof window !== 'undefined' && window.matchMedia(query).matches;
}

@Injectable({ providedIn: 'root' })
export class BreakpointService {
  private observer   = inject(BreakpointObserver);
  private platformId = inject(PLATFORM_ID);

  readonly isTablet  = signal(matchesNow(BREAKPOINT_MD));
  readonly isDesktop = signal(matchesNow(BREAKPOINT_LG));

  constructor() {
    if (!isPlatformBrowser(this.platformId)) return;

    this.observer
      .observe(BREAKPOINT_MD)
      .pipe(takeUntilDestroyed())
      .subscribe(({ matches }) => this.isTablet.set(matches));

    this.observer
      .observe(BREAKPOINT_LG)
      .pipe(takeUntilDestroyed())
      .subscribe(({ matches }) => this.isDesktop.set(matches));
  }
}