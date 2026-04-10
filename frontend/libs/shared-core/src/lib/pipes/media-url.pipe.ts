import { Pipe, PipeTransform, inject } from '@angular/core';
import { MediaService } from '../services/media.service';

@Pipe({ name: 'mediaUrl', standalone: true })
export class MediaUrlPipe implements PipeTransform {
  private mediaService = inject(MediaService);

  transform(value: string | null | undefined): string {
    if (!value) return '';
    return this.mediaService.getFullUrl(value);
  }
}
