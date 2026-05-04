import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api.tokens';
import { FriendResponse, PlayerProfileResponse, PlayerProfileUpdateRequest, PlayerStatsResponse } from '../models/player.models';

@Injectable({ providedIn: 'root' })
export class PlayerService {
  private http   = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getMyProfile(): Observable<PlayerProfileResponse> {
    return this.http.get<PlayerProfileResponse>(`${this.apiUrl}/players/me`);
  }

  getMyStats(): Observable<PlayerStatsResponse> {
    return this.http.get<PlayerStatsResponse>(`${this.apiUrl}/players/me/stats`);
  }

  updateMyProfile(request: PlayerProfileUpdateRequest): Observable<PlayerProfileResponse> {
    return this.http.put<PlayerProfileResponse>(`${this.apiUrl}/players/me`, request);
  }

  getPlayerProfile(playerId: number): Observable<PlayerProfileResponse> {
    return this.http.get<PlayerProfileResponse>(`${this.apiUrl}/players/${playerId}`);
  }

  searchByUsername(username: string): Observable<PlayerProfileResponse> {
    return this.http.get<PlayerProfileResponse>(`${this.apiUrl}/players/search`, { params: { username } });
  }

  getFriends(): Observable<FriendResponse[]> {
    return this.http.get<FriendResponse[]>(`${this.apiUrl}/friends`);
  }

  getPendingRequests(): Observable<FriendResponse[]> {
    return this.http.get<FriendResponse[]>(`${this.apiUrl}/friends/pending`);
  }

  sendFriendRequest(playerId: number): Observable<FriendResponse> {
    return this.http.post<FriendResponse>(`${this.apiUrl}/friends/${playerId}/request`, {});
  }

  acceptFriendRequest(friendshipId: number): Observable<FriendResponse> {
    return this.http.post<FriendResponse>(`${this.apiUrl}/friends/${friendshipId}/accept`, {});
  }

  declineOrRemoveFriend(friendshipId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/friends/${friendshipId}`);
  }
}
