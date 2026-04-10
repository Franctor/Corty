import {
  Component, forwardRef, inject, input, output, signal, computed, effect,
  HostListener, ElementRef, ChangeDetectionStrategy,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export interface SelectOption<T = unknown> {
  value: T;
  label: string;
}

@Component({
  selector: 'ui-select',
  templateUrl: 'select.component.html',
  styleUrl: 'select.component.scss',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true,
    },
  ],
})
export class SelectComponent<T = unknown> implements ControlValueAccessor {
  private el = inject(ElementRef);

  readonly options = input<SelectOption<T>[]>([]);
  readonly placeholder = input('Selecciona una opción...');
  /** Para uso sin formControl: bind directo con [value] + (valueChange) */
  readonly value = input<T | null>(null);
  readonly disabled = input(false);
  readonly valueChange = output<T | null>();

  readonly isOpen = signal(false);
  readonly search = signal('');
  readonly selectedOption = signal<SelectOption<T> | null>(null);
  private readonly disabledByForm = signal(false);

  readonly isDisabled = computed(() => this.disabled() || this.disabledByForm());

  readonly filtered = computed(() => {
    const q = this.search().toLowerCase();
    return q
      ? this.options().filter(o => o.label.toLowerCase().includes(q))
      : this.options();
  });

  private onChange: (v: T | null) => void = () => {};
  private onTouched: () => void = () => {};
  /** true cuando Angular ha registrado onChange — indica modo CVA (formControlName/formControl) */
  private cvaMode = false;
  private pendingValue: T | null = null;

  constructor() {
    // Cuando cambian las opciones, resolver valor pendiente (CVA) o sincronizar [value] (standalone)
    effect(() => {
      const opts = this.options();
      if (this.cvaMode) {
        // Resolver valor pendiente de writeValue si las opciones llegaron tarde
        if (this.pendingValue != null) {
          const found = opts.find(o => o.value === this.pendingValue) ?? null;
          if (found) { this.selectedOption.set(found); this.pendingValue = null; }
        }
      } else {
        // Modo standalone: sincronizar con [value]
        const v = this.value();
        const found = v != null ? opts.find(o => o.value === v) ?? null : null;
        this.selectedOption.set(found);
      }
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(e: MouseEvent): void {
    if (!this.el.nativeElement.contains(e.target)) {
      this.isOpen.set(false);
    }
  }

  open(): void {
    if (this.isDisabled()) return;
    this.isOpen.set(true);
    this.search.set('');
  }

  select(option: SelectOption<T>): void {
    this.selectedOption.set(option);
    this.isOpen.set(false);
    this.search.set('');
    this.onChange(option.value);
    this.onTouched();
    this.valueChange.emit(option.value);
  }

  onInput(value: string): void {
    this.search.set(value);
  }

  inputValue(): string {
    return this.isOpen() ? this.search() : (this.selectedOption()?.label ?? '');
  }

  // CVA
  writeValue(value: T | null): void {
    if (value === null || value === undefined) {
      this.selectedOption.set(null);
      this.pendingValue = null;
      return;
    }
    const found = this.options().find(o => o.value === value);
    if (found) {
      this.selectedOption.set(found);
      this.pendingValue = null;
    } else {
      // Opciones aún no disponibles — guardar para resolver cuando lleguen
      this.pendingValue = value;
    }
  }

  registerOnChange(fn: (v: T | null) => void): void { this.onChange = fn; this.cvaMode = true; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.disabledByForm.set(isDisabled); }
}
