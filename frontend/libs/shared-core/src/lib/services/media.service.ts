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
  uploadFile(file: File, folder: MediaFolder = 'general'): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('folder', folder);
    return this.http
      .post<{ url: string }>(`${this.apiUrl}/media/upload`, formData)
      .pipe(map(res => res.url));
  }

  // Convenience method for avatar uploads
  uploadAvatar(file: File): Observable<string> {
    return this.uploadFile(file, 'avatars');
  }

  // Convenience method for court image uploads
  uploadCourtImage(file: File): Observable<string> {
    return this.uploadFile(file, 'court-images');
  }

  // Convenience method for document uploads
  uploadDocument(file: File): Observable<string> {
    return this.uploadFile(file, 'documents');
  }

  getFullUrl(relativePath: string): string {
    return relativePath ? `${this.apiUrl}/media${relativePath}` : '';
  }
}