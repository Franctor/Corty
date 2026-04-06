import { Component, computed, inject } from '@angular/core';
import { IonRouterOutlet } from '@ionic/angular/standalone';
import { BreakpointService } from '@frontend/shared-ui';
import { TopnavComponent } from '../../components/top-nav/topnav.component';
import { BottomnavComponent } from '../../components/bottom-nav/bottomnav.component';



@Component({
  selector: 'app-main',
  templateUrl: 'main.page.html',
  styleUrls: ['main.page.scss'],
  standalone: true,
  imports: [
    IonRouterOutlet, TopnavComponent, BottomnavComponent
  ],
})
export class MainPage {
  private bp = inject(BreakpointService);

  readonly isDesktop = computed(() => this.bp.isTablet());

  constructor() { };
}