import { Component, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { IonContent, IonSpinner, NavController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { PageHeaderComponent } from '../../../components/page-header/page-header.component';
import { MediaUrlPipe, PlayerProfileResponse, PlayerService } from '@frontend/shared-core';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.page.html',
  styleUrl: './profile.page.scss',
  standalone: true,
  imports: [IonContent, IonSpinner, LucideAngularModule, PageHeaderComponent, MediaUrlPipe, DecimalPipe],
})
export class ProfilePage {
  private playerService = inject(PlayerService);
  private navCtrl       = inject(NavController);

  readonly profile    = signal<PlayerProfileResponse | null>(null);
  readonly loading    = signal(true);
  readonly cacheBust  = signal(Date.now());

  readonly topSport = computed(() => {
    const p = this.profile();
    if (!p || p.sports.length === 0) return null;
    return [...p.sports].sort((a, b) => b.playedMatches - a.playedMatches)[0];
  });

  readonly initial = computed(() => {
    const p = this.profile();
    return p ? p.name[0]?.toUpperCase() ?? '?' : '?';
  });

  ionViewWillEnter(): void {
    this.cacheBust.set(Date.now());
    this.playerService.getMyProfile().subscribe({
      next: p => { this.profile.set(p); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  navigate(path: string): void {
    this.navCtrl.navigateForward(['/profile', path]);
  }
}
