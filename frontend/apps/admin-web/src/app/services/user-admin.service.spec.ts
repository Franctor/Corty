import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserAdminService } from '../core/services/user-admin.service';
import { API_URL } from '@frontend/shared-core';

const API = 'http://localhost:8080/api';

describe('UserAdminService', () => {
  let service: UserAdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_URL, useValue: API },
      ],
    });
    service = TestBed.inject(UserAdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ── TC-ADM07 ───────────────────────────────────────────────────────────────

  it('TC-ADM07: getAll() GET con page, size y search como params', () => {
    service.getAll(0, 10, 'franco').subscribe();

    const req = httpMock.expectOne(
      r => r.url === `${API}/users` &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '10' &&
        r.params.get('search') === 'franco'
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 });
  });

  // ── TC-ADM08 ───────────────────────────────────────────────────────────────

  it('TC-ADM08: updateStatus() PATCH a /users/{id}/status con body correcto', () => {
    service.updateStatus(5, { enabled: false }).subscribe();

    const req = httpMock.expectOne(`${API}/users/5/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ enabled: false });
    req.flush({});
  });

  // ── TC-ADM09 ───────────────────────────────────────────────────────────────

  it('TC-ADM09: updateRoleAndAuthorities() PATCH a /users/{id}/role', () => {
    const body = { role: 'ADMIN', authorities: ['BOOKING_CREATE'] };
    service.updateRoleAndAuthorities(3, body).subscribe();

    const req = httpMock.expectOne(`${API}/users/3/role`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  // ── TC-ADM10 ───────────────────────────────────────────────────────────────

  it('TC-ADM10: getRoles() GET a /users/roles', () => {
    service.getRoles().subscribe(roles => {
      expect(roles).toEqual(['ADMIN', 'PLAYER']);
    });

    const req = httpMock.expectOne(`${API}/users/roles`);
    expect(req.request.method).toBe('GET');
    req.flush(['ADMIN', 'PLAYER']);
  });

  // ── TC-ADM11 ───────────────────────────────────────────────────────────────

  it('TC-ADM11: delete() DELETE a /users/{id}', () => {
    service.delete(7).subscribe();

    const req = httpMock.expectOne(`${API}/users/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
