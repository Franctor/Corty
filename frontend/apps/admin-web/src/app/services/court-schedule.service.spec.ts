import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CourtScheduleService } from '../core/services/court-schedule.service';
import { API_URL } from '@frontend/shared-core';

const API = 'http://localhost:8080/api';

describe('CourtScheduleService', () => {
  let service: CourtScheduleService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_URL, useValue: API },
      ],
    });
    service = TestBed.inject(CourtScheduleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ── TC-ADM12 ───────────────────────────────────────────────────────────────

  it('TC-ADM12: getSchedules() GET a /courts/{id}/schedule', () => {
    service.getSchedules(1).subscribe();

    const req = httpMock.expectOne(`${API}/courts/1/schedule`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  // ── TC-ADM13 ───────────────────────────────────────────────────────────────

  it('TC-ADM13: replaceSchedules() PUT con los horarios al completo', () => {
    const schedules = [{ dayOfWeek: 'MONDAY', openTime: '09:00', closeTime: '21:00' }];
    service.replaceSchedules(1, schedules as any).subscribe();

    const req = httpMock.expectOne(`${API}/courts/1/schedule`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(schedules);
    req.flush(schedules);
  });

  // ── TC-ADM14 ───────────────────────────────────────────────────────────────

  it('TC-ADM14: updateUseClubSchedule() PATCH con flag correcto', () => {
    service.updateUseClubSchedule(2, true).subscribe();

    const req = httpMock.expectOne(`${API}/courts/2/schedule/use-club-schedule`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ useClubSchedule: true });
    req.flush(null);
  });

  // ── TC-ADM15 ───────────────────────────────────────────────────────────────

  it('TC-ADM15: updateSlotDuration() PATCH con slotDurationMinutes', () => {
    service.updateSlotDuration(3, 60).subscribe();

    const req = httpMock.expectOne(`${API}/courts/3/schedule/slot-duration`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ slotDurationMinutes: 60 });
    req.flush(null);
  });

  // ── TC-ADM16 ───────────────────────────────────────────────────────────────

  it('TC-ADM16: addBlock() POST el bloque a /courts/{id}/schedule/blocks', () => {
    const block = { startDate: '2030-07-01', endDate: '2030-07-07', reason: 'Vacaciones' };
    service.addBlock(1, block as any).subscribe();

    const req = httpMock.expectOne(`${API}/courts/1/schedule/blocks`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(block);
    req.flush({ id: 10, ...block });
  });

  // ── TC-ADM17 ───────────────────────────────────────────────────────────────

  it('TC-ADM17: deleteBlock() DELETE a /courts/{id}/schedule/blocks/{blockId}', () => {
    service.deleteBlock(1, 10).subscribe();

    const req = httpMock.expectOne(`${API}/courts/1/schedule/blocks/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
