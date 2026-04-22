import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL, CourtScheduleEntry, CourtBlock } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class CourtScheduleService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getSchedules(courtId: number): Observable<CourtScheduleEntry[]> {
    return this.http.get<CourtScheduleEntry[]>(`${this.apiUrl}/courts/${courtId}/schedule`);
  }

  replaceSchedules(courtId: number, schedules: CourtScheduleEntry[]): Observable<CourtScheduleEntry[]> {
    return this.http.put<CourtScheduleEntry[]>(`${this.apiUrl}/courts/${courtId}/schedule`, schedules);
  }

  updateUseClubSchedule(courtId: number, useClubSchedule: boolean): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/courts/${courtId}/schedule/use-club-schedule`, { useClubSchedule });
  }

  updateSlotDuration(courtId: number, slotDurationMinutes: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/courts/${courtId}/schedule/slot-duration`, { slotDurationMinutes });
  }

  getBlocks(courtId: number): Observable<CourtBlock[]> {
    return this.http.get<CourtBlock[]>(`${this.apiUrl}/courts/${courtId}/schedule/blocks`);
  }

  addBlock(courtId: number, block: Omit<CourtBlock, 'id'>): Observable<CourtBlock> {
    return this.http.post<CourtBlock>(`${this.apiUrl}/courts/${courtId}/schedule/blocks`, block);
  }

  deleteBlock(courtId: number, blockId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/courts/${courtId}/schedule/blocks/${blockId}`);
  }
}
