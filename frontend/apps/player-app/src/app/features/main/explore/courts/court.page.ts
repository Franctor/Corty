import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  IonContent, IonHeader, IonToolbar,
  IonBackButton, IonButtons, IonSpinner, IonButton, IonFooter,
} from '@ionic/angular/standalone';
import { HttpClient } from '@angular/common/http';
import { CourtAdminResponse, MediaUrlPipe } from '@frontend/shared-core';
import { API_URL } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-court',
  templateUrl: './court.page.html',
  styleUrls: ['./court.page.scss'],
  standalone: true,
  imports: [
    IonContent, IonHeader, IonToolbar,
    IonBackButton, IonButtons, IonSpinner, IonButton, IonFooter,
    LucideAngularModule, MediaUrlPipe,
  ],
})
export class CourtPage {
  private route   = inject(ActivatedRoute);
  private router  = inject(Router);
  private http    = inject(HttpClient);
  private apiUrl  = inject(API_URL);

  readonly court   = signal<CourtAdminResponse | null>(null);
  readonly loading = signal(true);
  readonly error   = signal<string | null>(null);

  readonly coverLabel = computed(() =>
    this.court()?.covered ? 'Pista techada' : 'Pista exterior'
  );

  readonly features = computed(() => {
    const c = this.court();
    if (!c) return [];
    const list: { icon: string; label: string }[] = [
      { icon: c.covered ? 'warehouse' : 'sun', label: c.covered ? 'Techada' : 'Exterior' },
    ];
    if (c.lighting) list.push({ icon: 'zap', label: 'Iluminación' });
    if (c.surfaceName) list.push({ icon: 'layers', label: c.surfaceName });
    list.push({ icon: 'clock', label: `${c.slotDurationMinutes} min / sesión` });
    return list;
  });

  private courtId!: number;

  constructor() {
    this.courtId = Number(this.route.snapshot.paramMap.get('id'));
    this.http.get<CourtAdminResponse>(`${this.apiUrl}/courts/${this.courtId}`).subscribe({
      next: c  => { this.court.set(c); this.loading.set(false); },
      error: () => { this.error.set('No se pudo cargar la pista.'); this.loading.set(false); },
    });
  }

  onBook(): void {
    this.router.navigate(['/booking', this.courtId]);
  }
}
