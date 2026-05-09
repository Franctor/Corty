import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { ChatService, FriendResponse, MediaUrlPipe, NotificationService, PlayerProfileResponse, PlayerService } from '@frontend/shared-core';
import { PageHeaderComponent } from '../../../components/page-header/page-header.component';
import { ConfirmSheetComponent } from '../../../components/confirm-sheet/confirm-sheet.component';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-social',
  templateUrl: './social.page.html',
  styleUrls: ['./social.page.scss'],
  standalone: true,
  imports: [IonContent, IonSpinner, LucideAngularModule, PageHeaderComponent, ConfirmSheetComponent, MediaUrlPipe],
})
export class SocialPage {
  private playerService       = inject(PlayerService);
  readonly notificationService = inject(NotificationService);
  readonly chatService         = inject(ChatService);
  private router              = inject(Router);
  private toastCtrl           = inject(ToastController);

  readonly unreadMessages = computed(() => this.chatService.unreadCount());

  readonly loading         = signal(false);
  readonly friends         = signal<FriendResponse[]>([]);
  readonly pending         = signal<FriendResponse[]>([]);
  readonly addUsername     = signal('');
  readonly searching       = signal(false);
  readonly suggestions     = signal<PlayerProfileResponse[]>([]);
  readonly showSuggestions = signal(false);

  private readonly search$ = new Subject<string>();

  readonly receivedPending    = computed(() => this.pending().filter(f => !f.iAmRequester));
  readonly sentPending        = computed(() => this.pending().filter(f => f.iAmRequester));
  readonly friendToRemove     = signal<FriendResponse | null>(null);

  constructor() {
    effect(() => {
      const ev = this.notificationService.lastEvent();
      if (ev?.type === 'FRIEND_REQUEST' || ev?.type === 'FRIEND_ACCEPTED') {
        this.loadFriends();
      }
    });

    this.search$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => {
        if (q.trim().length < 2) {
          this.suggestions.set([]);
          this.showSuggestions.set(false);
          return [];
        }
        return this.playerService.suggestPlayers(q.trim());
      }),
      takeUntilDestroyed(),
    ).subscribe(results => {
      this.suggestions.set(results);
      this.showSuggestions.set(results.length > 0);
    });
  }

  ionViewWillEnter(): void {
    this.loadFriends();
  }

  loadFriends(): void {
    this.loading.set(true);
    this.playerService.getFriends().subscribe({
      next: data => this.friends.set(data),
      error: ()  => {},
    });
    this.playerService.getPendingRequests().subscribe({
      next: data => { this.pending.set(data); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }

  goToMessages(): void {
    this.router.navigate(['/social/chat']);
  }

  viewProfile(f: FriendResponse): void {
    if (f.playerId) this.router.navigate(['/profile', f.playerId]);
  }

  openChat(f: FriendResponse): void {
    this.router.navigate(['/social/chat/new'], {
      queryParams: { recipientId: f.userId, name: `${f.name} ${f.surname}`.trim() },
    });
  }

  onAddUsernameInput(event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    this.addUsername.set(val);
    this.search$.next(val);
  }

  closeSuggestions(): void {
    this.showSuggestions.set(false);
  }

  goToPlayerProfile(player: PlayerProfileResponse): void {
    this.showSuggestions.set(false);
    this.addUsername.set('');
    this.router.navigate(['/profile', player.id]);
  }

  sendFriendRequestToPlayer(player: PlayerProfileResponse): void {
    this.showSuggestions.set(false);
    this.addUsername.set('');
    this.searching.set(true);
    this.playerService.sendFriendRequest(player.id).subscribe({
      next: () => {
        this.searching.set(false);
        this.loadFriends();
        this.showToast(`Solicitud enviada a ${player.username}`, 'success');
      },
      error: (err) => {
        this.searching.set(false);
        this.showToast(err?.error?.message ?? 'Error al enviar solicitud', 'danger');
      },
    });
  }

  sendFriendRequest(): void {
    const username = this.addUsername().trim();
    if (!username) return;
    this.searching.set(true);
    this.showSuggestions.set(false);
    this.playerService.searchByUsername(username).subscribe({
      next: player => {
        this.playerService.sendFriendRequest(player.id).subscribe({
          next: () => {
            this.searching.set(false);
            this.addUsername.set('');
            this.loadFriends();
            this.showToast('Solicitud enviada', 'success');
          },
          error: (err) => {
            this.searching.set(false);
            this.showToast(err?.error?.message ?? 'Error al enviar solicitud', 'danger');
          },
        });
      },
      error: () => {
        this.searching.set(false);
        this.showToast('Usuario no encontrado', 'danger');
      },
    });
  }

  acceptRequest(f: FriendResponse): void {
    this.playerService.acceptFriendRequest(f.friendshipId).subscribe({
      next: () => {
        this.pending.update(list => list.filter(p => p.friendshipId !== f.friendshipId));
        this.loadFriends();
      },
      error: () => this.showToast('Error al aceptar solicitud', 'danger'),
    });
  }

  declineRequest(f: FriendResponse): void {
    this.playerService.declineOrRemoveFriend(f.friendshipId).subscribe({
      next: () => this.pending.update(list => list.filter(p => p.friendshipId !== f.friendshipId)),
      error: () => this.showToast('Error al rechazar solicitud', 'danger'),
    });
  }

  removeFriend(f: FriendResponse): void {
    this.friendToRemove.set(f);
  }

  confirmRemoveFriend(): void {
    const f = this.friendToRemove();
    this.friendToRemove.set(null);
    if (!f) return;
    this.playerService.declineOrRemoveFriend(f.friendshipId).subscribe({
      next: () => this.friends.update(list => list.filter(fr => fr.friendshipId !== f.friendshipId)),
      error: () => this.showToast('Error al eliminar amigo', 'danger'),
    });
  }

  private async showToast(message: string, color: 'success' | 'danger'): Promise<void> {
    const toast = await this.toastCtrl.create({ message, duration: 2500, color, position: 'top' });
    await toast.present();
  }
}
