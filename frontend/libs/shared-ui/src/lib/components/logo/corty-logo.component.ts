import { Component, computed, input } from '@angular/core';

export type LogoMode = 'icon' | 'full';

@Component({
  selector: 'lib-corty-logo',
  standalone: true,
  templateUrl: 'corty-logo.component.html',
  styleUrl: 'corty-logo.component.scss',
})
export class CortyLogoComponent {
  readonly mode = input<LogoMode>('icon');
  readonly size = input<number>(36);

  // Font size scales proportionally with the icon size
  readonly fontSize = computed(() => `${Math.round(this.size() * 0.57)}px`);
}