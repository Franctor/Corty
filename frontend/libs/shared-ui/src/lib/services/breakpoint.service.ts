import { inject, Injectable, signal } from '@angular/core';
import { BreakpointObserver } from '@angular/cdk/layout';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

const BREAKPOINT_MD = '(min-width: 768px)';   
const BREAKPOINT_LG = '(min-width: 1024px)';  

@Injectable({ providedIn: 'root' })
export class BreakpointService {
  private observer = inject(BreakpointObserver);

  readonly isTablet = signal(false);

  readonly isDesktop = signal(false);

  constructor() {
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