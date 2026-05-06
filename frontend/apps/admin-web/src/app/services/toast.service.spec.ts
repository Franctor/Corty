import { TestBed } from '@angular/core/testing';
import { ToastService } from '../shared/services/toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  // ── TC-ADM01 ───────────────────────────────────────────────────────────────

  it('TC-ADM01: show() añade un toast a la señal con el tipo correcto', () => {
    service.show('Operación completada', 'success');

    const toasts = service.toasts();
    expect(toasts.length).toBe(1);
    expect(toasts[0].message).toBe('Operación completada');
    expect(toasts[0].type).toBe('success');
  });

  // ── TC-ADM02 ───────────────────────────────────────────────────────────────

  it('TC-ADM02: success() crea toast de tipo success', () => {
    service.success('Guardado correctamente');

    expect(service.toasts()[0].type).toBe('success');
    expect(service.toasts()[0].message).toBe('Guardado correctamente');
  });

  // ── TC-ADM03 ───────────────────────────────────────────────────────────────

  it('TC-ADM03: error() crea toast de tipo error', () => {
    service.error('Ha ocurrido un error');

    expect(service.toasts()[0].type).toBe('error');
  });

  // ── TC-ADM04 ───────────────────────────────────────────────────────────────

  it('TC-ADM04: info() crea toast de tipo info', () => {
    service.info('Información disponible');

    expect(service.toasts()[0].type).toBe('info');
  });

  // ── TC-ADM05 ───────────────────────────────────────────────────────────────

  it('TC-ADM05: dismiss() elimina el toast con el id indicado', () => {
    service.show('Toast A', 'info');
    service.show('Toast B', 'success');
    const idA = service.toasts()[0].id;

    service.dismiss(idA);

    const remaining = service.toasts();
    expect(remaining.length).toBe(1);
    expect(remaining[0].message).toBe('Toast B');
  });

  // ── TC-ADM06 ───────────────────────────────────────────────────────────────

  it('TC-ADM06: múltiples toasts tienen IDs únicos y crecientes', () => {
    service.show('Primero', 'info');
    service.show('Segundo', 'success');
    service.show('Tercero', 'error');

    const ids = service.toasts().map(t => t.id);
    const unique = new Set(ids);
    expect(unique.size).toBe(3);
    expect(ids[0]).toBeLessThan(ids[1]);
    expect(ids[1]).toBeLessThan(ids[2]);
  });
});
