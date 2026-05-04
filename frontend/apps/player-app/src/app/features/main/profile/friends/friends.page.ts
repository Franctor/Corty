import { Component, computed, inject, signal } from '@angular/core';
import { IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';
import { FriendResponse, MediaUrlPipe, PlayerService } from '@frontend/shared-core';

type Tab = 'friends' | 'pending';

@Component({
  selector: 'app-friends',
  templateUrl: './friends.page.html',
  styleUrl: './friends.page.scss',
  standalone: true,
  imports: [IonContent, IonSpinner, LucideAngularModule, PageHeaderComponent, MediaUrlPipe],
})
export class FriendsPage {
  private playerService = inject(PlayerService);
  private toastCtrl     = inject(ToastController);

  readonly friends  = signal<FriendResponse[]>([]);
  readonly pending  = signal<FriendResponse[]>([]);
  readonly loading  = signal(true);
  readonly activeTab = signal<Tab>('friends');
  readonly processingId = signal<number | null>(null);

  readonly pendingReceived = computed(() =>
    this.pending().filter(f => !f.iAmRequester)
  );
  readonly pendingSent = computed(() =>
    this.pending().filter(f => f.iAmRequester)
  );
  readonly pendingCount = computed(() => this.pendingReceived().length);

  ionViewWillEnter(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.playerService.getFriends().subscribe({
      next: list => { this.friends.set(list); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.playerService.getPendingRequests().subscribe({
      next: list => this.pending.set(list),
    });
  }

  onAccept(friendshipId: number): void {
    if (this.processingId() != null) return;
    this.processingId.set(friendshipId);
    this.playerService.acceptFriendRequest(friendshipId).subscribe({
      next: accepted => {
        this.processingId.set(null);
        this.pending.update(list => list.filter(f => f.friendshipId !== friendshipId));
        this.friends.update(list => [accepted, ...list]);
      },
      error: async () => {
        this.processingId.set(null);
        const t = await this.toastCtrl.create({ message: 'No se pudo aceptar.', duration: 2500, color: 'danger', position: 'top' });
        await t.present();
      },
    });
  }

  onDecline(friendshipId: number): void {
    if (this.processingId() != null) return;
    this.processingId.set(friendshipId);
    this.playerService.declineOrRemoveFriend(friendshipId).subscribe({
      next: () => {
        this.processingId.set(null);
        this.pending.update(list => list.filter(f => f.friendshipId !== friendshipId));
      },
      error: async () => {
        this.processingId.set(null);
        const t = await this.toastCtrl.create({ message: 'No se pudo rechazar.', duration: 2500, color: 'danger', position: 'top' });
        await t.present();
      },
    });
  }

  onRemove(friendshipId: number): void {
    if (this.processingId() != null) return;
    this.processingId.set(friendshipId);
    this.playerService.declineOrRemoveFriend(friendshipId).subscribe({
      next: () => {
        this.processingId.set(null);
        this.friends.update(list => list.filter(f => f.friendshipId !== friendshipId));
      },
      error: async () => {
        this.processingId.set(null);
        const t = await this.toastCtrl.create({ message: 'No se pudo eliminar.', duration: 2500, color: 'danger', position: 'top' });
        await t.present();
      },
    });
  }

  readonly initial = (name: string) => name[0]?.toUpperCase() ?? '?';
}
