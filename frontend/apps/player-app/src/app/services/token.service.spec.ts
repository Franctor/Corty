import { TestBed } from '@angular/core/testing';
import { TokenService } from '@frontend/shared-auth';

const VALID_JWT = (() => {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(
    JSON.stringify({
      sub: 'testuser',
      id: 42,
      authorities: ['ROLE_PLAYER', 'BOOKING_CREATE'],
      exp: Math.floor(Date.now() / 1000) + 3600,
    })
  );
  return `${header}.${payload}.signature`;
})();

const EXPIRED_JWT = (() => {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(
    JSON.stringify({
      sub: 'testuser',
      id: 42,
      authorities: ['ROLE_PLAYER'],
      exp: Math.floor(Date.now() / 1000) - 10,
    })
  );
  return `${header}.${payload}.signature`;
})();

describe('TokenService', () => {
  let service: TokenService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenService);
  });

  afterEach(() => localStorage.clear());

  // ── TC-TS01 ────────────────────────────────────────────────────────────────

  it('TC-TS01: save() almacena el token y lo expone en la señal', () => {
    service.save(VALID_JWT);

    expect(localStorage.getItem('corty_token')).toBe(VALID_JWT);
    expect(service.token()).toBe(VALID_JWT);
  });

  // ── TC-TS02 ────────────────────────────────────────────────────────────────

  it('TC-TS02: remove() borra el token y la señal queda null', () => {
    service.save(VALID_JWT);
    service.remove();

    expect(localStorage.getItem('corty_token')).toBeNull();
    expect(service.token()).toBeNull();
  });

  // ── TC-TS03 ────────────────────────────────────────────────────────────────

  it('TC-TS03: isExpired() devuelve false con token válido', () => {
    service.save(VALID_JWT);

    expect(service.isExpired()).toBe(false);
  });

  // ── TC-TS04 ────────────────────────────────────────────────────────────────

  it('TC-TS04: isExpired() devuelve true con token caducado', () => {
    service.save(EXPIRED_JWT);

    expect(service.isExpired()).toBe(true);
  });

  // ── TC-TS05 ────────────────────────────────────────────────────────────────

  it('TC-TS05: getRole() extrae el rol sin prefijo ROLE_', () => {
    service.save(VALID_JWT);

    expect(service.getRole()).toBe('PLAYER');
  });

  // ── TC-TS06 ────────────────────────────────────────────────────────────────

  it('TC-TS06: getUserId() extrae el id del payload', () => {
    service.save(VALID_JWT);

    expect(service.getUserId()).toBe(42);
  });

  // ── TC-TS07 ────────────────────────────────────────────────────────────────

  it('TC-TS07: hasAuthority() devuelve true para authority existente', () => {
    service.save(VALID_JWT);

    expect(service.hasAuthority('BOOKING_CREATE')).toBe(true);
  });

  // ── TC-TS08 ────────────────────────────────────────────────────────────────

  it('TC-TS08: hasAuthority() devuelve false para authority inexistente', () => {
    service.save(VALID_JWT);

    expect(service.hasAuthority('ADMIN_ACCESS')).toBe(false);
  });
});
