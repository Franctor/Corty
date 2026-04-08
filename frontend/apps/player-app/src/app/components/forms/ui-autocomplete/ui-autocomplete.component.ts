import { Component, Input, Optional, Self, computed, signal } from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { IonInput } from '@ionic/angular/standalone';
import { FormFieldComponent } from '@frontend/shared-ui';
export interface AutocompleteOption {
  value: any;
  label: string;
}

@Component({
  selector: 'ui-autocomplete',
  templateUrl: './ui-autocomplete.component.html',
  styleUrls: ['./ui-autocomplete.component.scss'],
  standalone: true,
  imports: [IonInput, FormFieldComponent],
})
export class UiAutocompleteComponent implements ControlValueAccessor {

  @Input() label!: string;
  @Input() placeholder: string = 'Busca una opción...';
  @Input() optional = false;

  @Input() set options(value: AutocompleteOption[]) {
    this._options.set(value);
  }

  @Input() set disabled(value: boolean) {
    this.isDisabled.set(value);
  }

  private readonly _options = signal<AutocompleteOption[]>([]);
  readonly searchTerm = signal('');
  readonly isDisabled = signal(false);
  readonly selectedLabel = signal('');

  readonly filteredOptions = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    if (!term) return [];
    return this._options().filter(o => o.label.toLowerCase().includes(term));
  });

  readonly showDropdown = computed(() =>
    this.filteredOptions().length > 0 &&
    this.searchTerm().length > 0 &&
    !this._selectedValue()
  );

  private readonly _selectedValue = signal<any>(null);

  constructor(@Optional() @Self() public ngControl: NgControl) {
    if (ngControl) ngControl.valueAccessor = this;
  }

  onChange = (_: any) => {};
  onTouched = () => {};

  writeValue(value: any): void {
    this._selectedValue.set(value ?? null);
    if (!value) {
      this.searchTerm.set('');
      this.selectedLabel.set('');
    }
  }

  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.isDisabled.set(isDisabled); }

  onSearchInput(term: string): void {
    this.searchTerm.set(term);
    if (this._selectedValue()) {
      this._selectedValue.set(null);
      this.onChange(null);
    }
  }

  selectOption(option: AutocompleteOption): void {
    this._selectedValue.set(option.value);
    this.selectedLabel.set(option.label);
    this.searchTerm.set('');
    this.onChange(option.value);
    this.onTouched();
  }

  onBlur(): void { this.onTouched(); }

  get displayValue(): string {
    return this.selectedLabel() || this.searchTerm();
  }
}