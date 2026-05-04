import { Component, Input, Optional, Self, signal } from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { IonTextarea } from '@ionic/angular/standalone';
import { FormFieldComponent } from '@frontend/shared-ui';
@Component({
  selector: 'ui-textarea',
  templateUrl: './ui-textarea.component.html',
  standalone: true,
  imports: [IonTextarea, FormFieldComponent],
})
export class UiTextareaComponent implements ControlValueAccessor {

  @Input() label!: string;
  @Input() placeholder: string = '';
  @Input() optional = false;
  @Input() rows: number = 3;
  @Input() maxlength?: number;

  readonly value = signal('');
  readonly isDisabled = signal(false);

  constructor(@Optional() @Self() public ngControl: NgControl) {
    if (ngControl) ngControl.valueAccessor = this;
  }

  onChange = (_: any) => {};
  onTouched = () => {};

  writeValue(value: any): void { this.value.set(value ?? ''); }
  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled.set(isDisabled); }

  onInput(event: any): void {
    this.value.set(event.target.value);
    this.onChange(this.value());
  }

  onBlur(): void { this.onTouched(); }

  get charCount(): number { return this.value().length; }
}