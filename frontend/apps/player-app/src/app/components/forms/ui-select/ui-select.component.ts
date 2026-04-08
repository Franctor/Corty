import { Component, ElementRef, Input, Optional, Self, signal } from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { IonSelect, IonSelectOption, Platform } from '@ionic/angular/standalone';
import { FormFieldComponent } from '@frontend/shared-ui';

@Component({
  selector: 'ui-select',
  templateUrl: './ui-select.component.html',
  standalone: true,
  imports: [IonSelect, IonSelectOption, FormFieldComponent],
})
export class UiSelectComponent implements ControlValueAccessor {

  @Input() label!: string;
  @Input() placeholder: string = 'Selecciona una opción';
  @Input() optional = false;
  @Input() options: { value: any; label: string }[] = [];

  readonly value = signal<any>(null);
  readonly isDisabled = signal(false);

  constructor(
    @Optional() @Self() public ngControl: NgControl,
    private platform: Platform,
    private el: ElementRef<HTMLElement>,
  ) {
    if (ngControl) ngControl.valueAccessor = this;
  }

  get selectInterface(): 'popover' | 'action-sheet' {
    return this.platform.is('desktop') ? 'popover' : 'action-sheet';
  }

  get selectOptions() {
    return { cssClass: 'corty-select', alignment: 'start' };
  }

  onSelectClick(): void {
    if (!this.platform.is('desktop')) return;

    const hostRect = this.el.nativeElement.getBoundingClientRect();

    document.documentElement.style.setProperty(
      '--corty-select-popover-width', `${hostRect.width}px`
    );
    document.documentElement.style.setProperty(
      '--corty-select-popover-left', `${hostRect.left}px`
    );
  }

  onChange = (_: any) => { };
  onTouched = () => { };

  writeValue(value: any): void { this.value.set(value ?? null); }
  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled.set(isDisabled); }

  onIonChange(event: any): void {
    this.value.set(event.detail.value);
    this.onChange(this.value());
  }

  onBlur(): void { this.onTouched(); }
}