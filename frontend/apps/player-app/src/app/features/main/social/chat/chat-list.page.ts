import { Component, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { IonContent, IonSpinner } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { DatePipe } from '@angular/common';
import { ChatService, ConversationResponse, MediaUrlPipe, NotificationService } from '@frontend/shared-core';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';

@Component({
  selector: 'app-chat-list',
  templateUrl: './chat-list.page.html',
  styleUrls: ['./chat-list.page.scss'],
  standalone: true,
  imports: [IonContent, IonSpinner, LucideAngularModule, PageHeaderComponent, DatePipe, MediaUrlPipe],
})
export class ChatListPage {
  private chatService         = inject(ChatService);
  private notificationService = inject(NotificationService);
  private router              = inject(Router);

  readonly loading       = signal(true);
  readonly conversations = signal<ConversationResponse[]>([]);

  constructor() {
    effect(() => {
      const ev = this.notificationService.lastEvent();
      if (ev?.type === 'NEW_MESSAGE') this.load();
    });
  }

  ionViewWillEnter(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.chatService.getConversations().subscribe({
      next: data => { this.conversations.set(data); this.loading.set(false); },
      error: ()  => this.loading.set(false),
    });
  }

  openChat(conv: ConversationResponse): void {
    this.router.navigate(['/social/chat', conv.idConversation], {
      queryParams: { recipientId: conv.otherParticipantId, name: conv.otherParticipantName },
    });
  }
}
