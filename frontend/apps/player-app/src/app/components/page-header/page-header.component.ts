import { Component, inject, input, output } from '@angular/core';
import { NavController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
  standalone: true,
  imports: [LucideAngularModule],
})
export class PageHeaderComponent {
  readonly title      = input.required<string>();
  readonly backUrl    = input<string | null>(null);
  readonly customBack = input(false);
  readonly backClick  = output<void>();

  private navCtrl = inject(NavController);

  goBack(): void {
    if (this.customBack()) {
      this.backClick.emit();
    } else if (this.backUrl()) {
      this.navCtrl.navigateBack(this.backUrl()!);
    } else {
      this.navCtrl.back();
    }
  }
}
