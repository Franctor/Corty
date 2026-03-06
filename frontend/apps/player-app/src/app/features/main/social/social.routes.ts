import { Routes } from '@angular/router';

export const socialRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./social.page').then((m) => m.SocialPage),
  },
  {
    path: 'chat',
    loadComponent: () =>
      import('./chat/chat-list.page').then((m) => m.ChatListPage),
  },
  {
    path: 'chat/:id',
    loadComponent: () =>
      import('./chat/chat.page').then((m) => m.ChatPage),
  },
  {
    path: 'notifications',
    loadComponent: () =>
      import('./notifications/notifications.page').then((m) => m.NotificationsPage),
  },
];