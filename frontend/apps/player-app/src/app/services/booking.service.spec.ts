import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { BookingService, API_URL } from '@frontend/shared-core';

const API = 'http://localhost:8080/api';

describe('BookingService', () => {
  let service: BookingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_URL, useValue: API },
      ],
    });
    service = TestBed.inject(BookingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ── TC-BS01 ────────────────────────────────────────────────────────────────

  it('TC-BS01: getAvailableSlots() GET con courtId y date como params', () => {
    const mockSlots = [{ startTime: '10:00', endTime: '11:00', available: true }];

    service.getAvailableSlots(5, '2030-06-15').subscribe((slots) => {
      expect(slots).toEqual(mockSlots);
    });

    const req = httpMock.expectOne(
      (r) => r.url === `${API}/bookings/availability` &&
        r.params.get('courtId') === '5' &&
        r.params.get('date') === '2030-06-15'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockSlots);
  });

  // ── TC-BS02 ────────────────────────────────────────────────────────────────

  it('TC-BS02: createBooking() POST a /bookings y devuelve bookingId', () => {
    const request = {
      courtId: 1,
      date: '2030-06-15',
      startTime: '10:00',
      endTime: '11:00',
      bookingType: 'PRIVATE',
      paymentMethod: 'CASH',
    };

    service.createBooking(request as any).subscribe((res) => {
      expect(res.bookingId).toBe(99);
    });

    const req = httpMock.expectOne(`${API}/bookings`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ bookingId: 99 });
  });

  // ── TC-BS03 ────────────────────────────────────────────────────────────────

  it('TC-BS03: cancelBooking() DELETE a /bookings/{id}', () => {
    const mockResponse = {
      message: 'Cancelada',
      karmaDeducted: 0,
      karmaRemaining: 80,
      refundInfo: 'Reembolso completo',
    };

    service.cancelBooking(10).subscribe((res) => {
      expect(res.karmaDeducted).toBe(0);
    });

    const req = httpMock.expectOne(`${API}/bookings/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });

  // ── TC-BS04 ────────────────────────────────────────────────────────────────

  it('TC-BS04: getMyBookings() sin filtro → GET sin params de status', () => {
    service.getMyBookings().subscribe();

    const req = httpMock.expectOne(
      (r) => r.url === `${API}/bookings/mine` && !r.params.has('status')
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  // ── TC-BS05 ────────────────────────────────────────────────────────────────

  it('TC-BS05: getMyBookings() con statuses → GET con params status', () => {
    service.getMyBookings(['CONFIRMED', 'PENDING_PAYMENT']).subscribe();

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${API}/bookings/mine` &&
        (r.params.getAll('status') ?? []).includes('CONFIRMED') &&
        (r.params.getAll('status') ?? []).includes('PENDING_PAYMENT')
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  // ── TC-BS06 ────────────────────────────────────────────────────────────────

  it('TC-BS06: getPublicBookings() construye solo los params presentes', () => {
    service.getPublicBookings({ sport: 'Pádel', limit: 10 }).subscribe();

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${API}/bookings/public` &&
        r.params.get('sport') === 'Pádel' &&
        r.params.get('limit') === '10' &&
        !r.params.has('lat')
    );
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  // ── TC-BS07 ────────────────────────────────────────────────────────────────

  it('TC-BS07: leaveBooking() DELETE a /bookings/{id}/leave', () => {
    service.leaveBooking(7).subscribe();

    const req = httpMock.expectOne(`${API}/bookings/7/leave`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ message: 'Abandonada', karmaDeducted: 15, karmaRemaining: 65, refundInfo: '' });
  });
});
