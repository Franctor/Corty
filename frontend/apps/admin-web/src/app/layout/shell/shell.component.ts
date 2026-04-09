import { Component, inject, computed, signal, effect } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';
import { BreakpointService } from '@frontend/shared-ui';

@Component({
  selector: 'app-shell',
  templateUrl: 'shell.component.html',
  styleUrl: 'shell.component.scss',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, HeaderComponent],
})
export class ShellComponent {
  private breakpoint = inject(BreakpointService);

  readonly isDesktop = computed(() => this.breakpoint.isDesktop());
  readonly sidebarOpen = signal(false);

  constructor() {
    // Cierra el sidebar automáticamente al pasar a desktop
    effect(() => {
      if (this.isDesktop()) {
        this.sidebarOpen.set(false);
      }
    });
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }
}
