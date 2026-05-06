import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService, TokenService } from '@frontend/shared-auth';
import { API_URL } from '@frontend/shared-core';

const API = 'http://localhost:8080/api';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let tokenService: TokenService;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: API_URL, useValue: API },
      ],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    tokenService = TestBed.inject(TokenService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  // ── TC-AS01 ────────────────────────────────────────────────────────────────

  it('TC-AS01: login() POST a /auth/login y guarda el token', () => {
    const fakeToken = 'header.eyJpZCI6MX0.sig';
    let received: string | undefined;

    service.login({ username: 'user', password: 'pass' }).subscribe((res) => {
      received = res.token;
    });

    const req = httpMock.expectOne(`${API}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: fakeToken });

    expect(received).toBe(fakeToken);
    expect(tokenService.get()).toBe(fakeToken);
    expect(service.isLoggedIn()).toBe(true);
  });

  // ── TC-AS02 ────────────────────────────────────────────────────────────────

  it('TC-AS02: register() POST a /auth/register y guarda el token', () => {
    const fakeToken = 'header.eyJpZCI6Mn0.sig';

    service
      .register({
        username: 'nuevo',
        email: 'nuevo@test.com',
        password: 'pass',
        name: 'Test',
        surname: 'User',
        phone: '600000000',
        gender: 'MALE',
        birthDate: '1990-01-01',
        cityId: 1,
      })
      .subscribe();

    const req = httpMock.expectOne(`${API}/auth/register`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: fakeToken });

    expect(tokenService.get()).toBe(fakeToken);
    expect(service.isLoggedIn()).toBe(true);
  });

  // ── TC-AS03 ────────────────────────────────────────────────────────────────

  it('TC-AS03: logout() elimina el token, isLoggedIn false y redirige', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    tokenService.save('some.token.here');
    service.isLoggedIn.set(true);

    service.logout();

    expect(tokenService.get()).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/auth/login']);
  });

  // ── TC-AS04 ────────────────────────────────────────────────────────────────

  it('TC-AS04: isLoggedIn arranca false cuando no hay token', () => {
    expect(service.isLoggedIn()).toBe(false);
  });

  // ── TC-AS05 ────────────────────────────────────────────────────────────────

  it('TC-AS05: login() error HTTP → token NO se guarda', () => {
    let errorReceived = false;

    service.login({ username: 'bad', password: 'bad' }).subscribe({
      error: () => { errorReceived = true; },
    });

    const req = httpMock.expectOne(`${API}/auth/login`);
    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(errorReceived).toBe(true);
    expect(tokenService.get()).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
  });
});
