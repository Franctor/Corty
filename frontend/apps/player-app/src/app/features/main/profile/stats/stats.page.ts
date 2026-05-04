import { Component, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { IonContent, IonSpinner } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';
import { MediaUrlPipe, PlayerStatsResponse, PlayerService } from '@frontend/shared-core';

@Component({
  selector: 'app-stats',
  templateUrl: './stats.page.html',
  styleUrl: './stats.page.scss',
  standalone: true,
  imports: [IonContent, IonSpinner, LucideAngularModule, PageHeaderComponent, MediaUrlPipe, DecimalPipe],
})
export class StatsPage {
  private playerService = inject(PlayerService);

  readonly stats   = signal<PlayerStatsResponse | null>(null);
  readonly loading = signal(true);

  readonly winRate = computed(() => {
    const s = this.stats();
    return s && s.totalMatches > 0 ? Math.round((s.totalWins / s.totalMatches) * 100) : 0;
  });

  ionViewWillEnter(): void {
    this.loading.set(true);
    this.playerService.getMyStats().subscribe({
      next: s => { this.stats.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
