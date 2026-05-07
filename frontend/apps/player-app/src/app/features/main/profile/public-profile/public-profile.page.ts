import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';
import { ConfirmSheetComponent } from '../../../../components/confirm-sheet/confirm-sheet.component';
import { FriendResponse, MediaUrlPipe, PlayerProfileResponse, PlayerService } from '@frontend/shared-core';

@Component({
  selector: 'app-public-profile',
  templateUrl: './public-profile.page.html',
  styleUrl: './public-profile.page.scss',
  standalone: true,
  imports: [IonContent, IonSpinner, LucideAngularModule, PageHeaderComponent, MediaUrlPipe, DecimalPipe, ConfirmSheetComponent],
})
export class PublicProfilePage {
  private route         = inject(ActivatedRoute);
  private playerService = inject(PlayerService);
  private toastCtrl     = inject(ToastController);

  readonly profile     = signal<PlayerProfileResponse | null>(null);
  readonly friendship  = signal<FriendResponse | null>(null);
  readonly loading           = signal(true);
  readonly requesting        = signal(false);
  readonly isPrivate         = signal(false);
  readonly showRemoveConfirm = signal(false);

  private playerId!: number;

  readonly topSport = computed(() => {
    const p = this.profile();
    if (!p || p.sports.length === 0) return null;
    return [...p.sports].sort((a, b) => b.playedMatches - a.playedMatches)[0];
  });

  readonly initial = computed(() => {
    const p = this.profile();
    return p ? p.name[0]?.toUpperCase() ?? '?' : '?';
  });

  readonly friendStatus = computed(() => this.friendship()?.status ?? null);

  ionViewWillEnter(): void {
    this.playerId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.isPrivate.set(false);
    this.playerService.getPlayerProfile(this.playerId).subscribe({
      next: p => { this.profile.set(p); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        if (err?.status === 404) this.isPrivate.set(true);
      },
    });
    this.playerService.getFriends().subscribe({
      next: list => {
        const f = list.find(fr => fr.playerId === this.playerId) ?? null;
        this.friendship.set(f);
      },
    });
    this.playerService.getPendingRequests().subscribe({
      next: list => {
        const f = list.find(fr => fr.playerId === this.playerId) ?? null;
        if (f) this.friendship.set(f);
      },
    });
  }

  onSendRequest(): void {
    if (this.requesting()) return;
    this.requesting.set(true);
    this.playerService.sendFriendRequest(this.playerId).subscribe({
      next: f => {
        this.requesting.set(false);
        this.friendship.set(f);
      },
      error: async err => {
        this.requesting.set(false);
        const t = await this.toastCtrl.create({
          message: err?.error?.message ?? 'No se pudo enviar la solicitud.',
          duration: 3000, color: 'danger', position: 'top',
        });
        await t.present();
      },
    });
  }

  onRemove(): void {
    this.showRemoveConfirm.set(true);
  }

  onCancelRequest(): void {
    const f = this.friendship();
    if (!f || this.requesting()) return;
    this.requesting.set(true);
    this.playerService.declineOrRemoveFriend(f.friendshipId).subscribe({
      next: () => { this.requesting.set(false); this.friendship.set(null); },
      error: async () => {
        this.requesting.set(false);
        const t = await this.toastCtrl.create({ message: 'No se pudo cancelar la solicitud.', duration: 2500, color: 'danger', position: 'top' });
        await t.present();
      },
    });
  }

  confirmRemove(): void {
    this.showRemoveConfirm.set(false);
    const f = this.friendship();
    if (!f || this.requesting()) return;
    this.requesting.set(true);
    this.playerService.declineOrRemoveFriend(f.friendshipId).subscribe({
      next: () => { this.requesting.set(false); this.friendship.set(null); },
      error: async () => {
        this.requesting.set(false);
        const t = await this.toastCtrl.create({ message: 'No se pudo eliminar.', duration: 2500, color: 'danger', position: 'top' });
        await t.present();
      },
    });
  }
}
