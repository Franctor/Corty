import { Component, Input, Optional, Self, signal } from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { IonInput } from '@ionic/angular/standalone';
import { FormFieldComponent } from '@frontend/shared-ui';

@Component({
  selector: 'ui-input',
  templateUrl: './ui-input.component.html',
  styles: [`
    :host ::ng-deep .input-highlight {
      display: none !important;
    }
  `],
  imports: [IonInput, FormFieldComponent],
})
export class UiInputComponent implements ControlValueAccessor {

  @Input() label!: string;
  @Input() type: string = 'text';
  @Input() placeholder: string = '';
  @Input() optional = false;

  readonly value = signal('');
  readonly isDisabled = signal(false);

  constructor(@Optional() @Self() public ngControl: NgControl) {
    if (ngControl) ngControl.valueAccessor = this;
  }

  onChange = (_: any) => { };
  onTouched = () => { };

  writeValue(value: any): void { this.value.set(value ?? ''); }
  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled.set(isDisabled); }

  onInput(event: any): void {
    this.value.set(event.target.value);
    this.onChange(this.value());
  }

  onBlur(): void { this.onTouched(); }
}