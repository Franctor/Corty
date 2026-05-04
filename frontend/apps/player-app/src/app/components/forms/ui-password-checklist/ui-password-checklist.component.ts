import { Component, input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

@Component({
  selector: 'ui-password-checklist',
  templateUrl: './ui-password-checklist.component.html',
  styleUrls: ['./ui-password-checklist.component.scss'],
  standalone: true,
})
export class UiPasswordChecklistComponent {

  readonly control = input<AbstractControl | null>(null);

  get visible(): boolean {
    return !!this.control()?.dirty && !!this.control()?.value?.length;
  }

  get errors() {
    return this.control()?.errors?.['strongPassword'] ?? {};
  }
}