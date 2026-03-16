import { Component, Input } from '@angular/core';

@Component({
  selector: 'lib-wizard',
  templateUrl: './wizard.component.html',
  styleUrls: ['./wizard.component.scss'],
  standalone: true,
})
export class WizardComponent {

  @Input() totalSteps!: number;
  @Input() currentStep!: number;

  get progressPercent(): number {
    return (this.currentStep / this.totalSteps) * 100;
  }
}