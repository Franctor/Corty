import { Component, inject } from '@angular/core';
import { ThemeService } from '@frontend/shared-ui';
import { IonApp, IonRouterOutlet } from '@ionic/angular/standalone';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  standalone: true,
  imports: [IonApp, IonRouterOutlet],
})
export class AppComponent {
  private themeService = inject(ThemeService);
}
