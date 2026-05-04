import { Component, effect, ElementRef, inject, signal, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { IonSpinner } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { SlicePipe } from '@angular/common';
import { ChatService, MessageResponse, NotificationService } from '@frontend/shared-core';
import { AuthService } from '@frontend/shared-auth';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.page.html',
  styleUrls: ['./chat.page.scss'],
  standalone: true,
  imports: [IonSpinner, LucideAngularModule, PageHeaderComponent, SlicePipe],
})
export class ChatPage {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef<HTMLDivElement>;
  @ViewChild('messagesEnd')    private messagesEnd!: ElementRef<HTMLDivElement>;

  private chatService         = inject(ChatService);
  private notificationService = inject(NotificationService);
  private authService         = inject(AuthService);
  private route               = inject(ActivatedRoute);
  private router              = inject(Router);

  private conversationId = 0;
  private recipientId    = 0;
  readonly myUserId  = signal<number | null>(null);

  readonly loading   = signal(false);
  readonly sending   = signal(false);
  readonly messages  = signal<MessageResponse[]>([]);
  readonly draft     = signal('');
  readonly otherName = signal('Chat');

  constructor() {
    const routeId = this.route.snapshot.paramMap.get('id');
    this.conversationId = routeId && routeId !== 'new' ? Number(routeId) : 0;
    this.recipientId = Number(this.route.snapshot.queryParamMap.get('recipientId') ?? '0');
    const name = this.route.snapshot.queryParamMap.get('name');
    if (name) this.otherName.set(name);
    this.myUserId.set(this.authService.getUserId());

    effect(() => {
      const ev = this.notificationService.lastEvent();
      if (ev?.type === 'NEW_MESSAGE' && this.conversationId) this.loadHistory();
    });
  }

  ionViewWillEnter(): void {
    if (this.conversationId) {
      this.loadHistory();
    } else if (this.recipientId) {
      this.loading.set(true);
      this.chatService.getConversationWith(this.recipientId).subscribe({
        next: res => {
          if (res.status === 200 && res.body) {
            this.conversationId = res.body.idConversation;
            this.loadHistory();
          } else {
            this.loading.set(false);
          }
        },
        error: () => this.loading.set(false),
      });
    }
  }

  private loadHistory(): void {
    if (!this.conversationId) return;
    this.loading.set(true);
    this.chatService.getHistory(this.conversationId).subscribe({
      next: msgs => {
        this.messages.set(msgs);
        this.loading.set(false);
        this.scrollToBottom();
      },
      error: () => this.loading.set(false),
    });
  }

  onInput(event: Event): void {
    this.draft.set((event.target as HTMLTextAreaElement).value);
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  sendMessage(): void {
    const content = this.draft().trim();
    if (!content || this.sending()) return;
    const myId = this.myUserId();
    if (!myId || !this.recipientId) return;

    this.sending.set(true);
    this.chatService.sendMessage({ senderId: myId, recipientId: this.recipientId, content }).subscribe({
      next: msg => {
        if (!this.conversationId) {
          this.conversationId = msg.idConversation;
          this.router.navigate(['/social/chat', this.conversationId], {
            replaceUrl: true,
            queryParams: { recipientId: this.recipientId, name: this.otherName() },
          });
        }
        this.messages.update(list => [...list, msg]);
        this.draft.set('');
        this.sending.set(false);
        this.scrollToBottom();
      },
      error: () => this.sending.set(false),
    });
  }

  isMine(msg: MessageResponse): boolean {
    const myId = this.myUserId();
    return msg.isMine || (myId !== null && msg.idSender === myId);
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      this.messagesEnd?.nativeElement?.scrollIntoView({ behavior: 'instant' });
    }, 30);
  }
}
