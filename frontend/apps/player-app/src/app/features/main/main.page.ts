import { Component, computed, effect, inject } from '@angular/core';
import { IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel } from '@ionic/angular/standalone';
import { RouterModule } from '@angular/router';
import { addIcons } from 'ionicons';
import { home, search, calendar, chatbubbles, person } from 'ionicons/icons';
import { BreakpointService } from '@frontend/shared-ui';
import { TopnavComponent } from '../../components/top-nav/topnav.component';
import { ChatService, NotificationService } from '@frontend/shared-core';
import { TokenService } from '@frontend/shared-auth';

@Component({
  selector: 'app-main',
  templateUrl: 'main.page.html',
  styleUrl: 'main.page.scss',
  standalone: true,
  imports: [IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel, RouterModule, TopnavComponent],
})
export class MainPage {
  private bp                  = inject(BreakpointService);
  private chatService         = inject(ChatService);
  private notificationService = inject(NotificationService);
  private tokenService        = inject(TokenService);

  readonly isDesktop = computed(() => this.bp.isTablet());

  readonly unreadMessages  = computed(() => this.chatService.unreadCount());
  readonly pendingRequests = computed(() =>
    this.notificationService.latest().filter(n => n.type === 'FRIEND_REQUEST' && !n.read).length
  );
  readonly socialBadge = computed(() => this.unreadMessages() + this.pendingRequests());

  constructor() {
    addIcons({ home, search, calendar, chatbubbles, person });

    // En móvil el TopNav no existe, así que conectamos SSE/notificaciones aquí
    if (!this.bp.isTablet()) {
      this.notificationService.loadUnreadCount();
      this.notificationService.loadLatest();
      const token = this.tokenService.get();
      if (token) this.notificationService.connectSse(token);
    }

    // Cargar conversaciones para badge inicial
    this.chatService.getConversations().subscribe();

    // Recargar unread chat cuando llega mensaje nuevo
    effect(() => {
      const ev = this.notificationService.lastEvent();
      if (ev?.type === 'NEW_MESSAGE') {
        this.chatService.getConversations().subscribe();
      }
    });
  }
}
