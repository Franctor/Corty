import { Component, input, output } from '@angular/core';
import { Location } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss'],
  standalone: true,
  imports: [LucideAngularModule],
})
export class PageHeaderComponent {
  readonly title          = input.required<string>();
  readonly customBack     = input(false);
  readonly backClick      = output<void>();

  constructor(private location: Location) {}

  goBack(): void {
    if (this.customBack()) {
      this.backClick.emit();
    } else {
      this.location.back();
    }
  }
}
