import { Component, computed, inject, input } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { fromEvent, startWith, map } from 'rxjs';

export type LogoMode = 'icon' | 'full';

@Component({
  selector: 'lib-corty-logo',
  standalone: true,
  templateUrl: 'corty-logo.component.html',
  styleUrl: 'corty-logo.component.scss',
})
export class CortyLogoComponent {
  private doc = inject(DOCUMENT);

  readonly mode = input<LogoMode>('icon');
  readonly size = input<number>(36);

  // Font size scales proportionally with the icon size
  readonly fontSize = computed(() => `${Math.round(this.size() * 0.57)}px`);

  // Reactive dark mode — checks Ionic class, manual class, then system preference
  private readonly mq = this.doc.defaultView?.matchMedia('(prefers-color-scheme: dark)');

  readonly isDark = toSignal(
    fromEvent<MediaQueryListEvent>(this.mq as EventTarget, 'change').pipe(
      startWith(null),
      map(() => {
        const body = this.doc.body;
        if (body.classList.contains('ion-palette-dark')) return true;
        if (body.classList.contains('dark-theme'))       return true;
        return this.mq?.matches ?? false;
      }),
    ),
    { initialValue: this.mq?.matches ?? false },
  );

  readonly strokeColor = computed<string>(() =>
    this.isDark() ? '#FFFFFF' : '#58CC02'
  );

  readonly textColor = computed<string>(() =>
    this.isDark() ? '#F0F0F0' : '#111111'
  );
}

// Usage examples:
// <lib-corty-logo />                          — icon only, green bg, 36px
// <lib-corty-logo mode="full" />               — icon + "Corty" text
// <lib-corty-logo variant="dark" [size]="48" /> — dark mode, 48px
// <lib-corty-logo variant="white" mode="full" /> — light bg with wordmark