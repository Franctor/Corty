import { Component, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { getFirstError } from '@frontend/shared-core';

@Component({
  selector: 'ui-form-field',
  templateUrl: './form-field.component.html',
  standalone: true,

})
export class FormFieldComponent {

  @Input() label!: string;
  @Input() optional = false;
  @Input() control!: AbstractControl | null;

  get showError(): boolean {
    return !!this.control?.invalid && !!this.control?.touched;
  }

  get error(): string | null {
    if (!this.control?.errors) return null;
    return getFirstError(this.control);
  }
}