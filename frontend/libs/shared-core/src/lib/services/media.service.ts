import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_URL } from '../tokens/api.tokens';

export type MediaFolder = 'avatars' | 'court-images' | 'documents' | 'general' | 'sport-icons' | 'surface-icons' | 'club-logos';

@Injectable({ providedIn: 'root' })
export class MediaService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  // Uploads a file to the specified folder and returns its public URL
  uploadFile(file: File, folder: MediaFolder = 'general', entityId?: number): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('folder', folder);
    if (entityId != null) formData.append('entityId', entityId.toString());
    return this.http
      .post<{ url: string }>(`${this.apiUrl}/media/upload`, formData)
      .pipe(map(res => res.url));
  }

  uploadAvatar(file: File, entityId?: number): Observable<string> {
    return this.uploadFile(file, 'avatars', entityId);
  }

  uploadCourtImage(file: File, entityId?: number): Observable<string> {
    return this.uploadFile(file, 'court-images', entityId);
  }

  uploadDocument(file: File): Observable<string> {
    return this.uploadFile(file, 'documents');
  }

  getFullUrl(relativePath: string): string {
    return relativePath ? `${this.apiUrl}/media${relativePath}` : '';
  }
}