import { Routes } from '@angular/router';

export const profileRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./profile.page').then(m => m.ProfilePage),
  },
  {
    path: 'stats',
    loadComponent: () => import('./stats/stats.page').then(m => m.StatsPage),
  },
  {
    path: 'friends',
    loadComponent: () => import('./friends/friends.page').then(m => m.FriendsPage),
  },
  {
    path: 'settings',
    children: [
      {
        path: '',
        loadComponent: () => import('./settings/settings.page').then(m => m.SettingsPage),
      },
      {
        path: 'edit',
        loadComponent: () => import('./settings/edit/edit-profile.page').then(m => m.EditProfilePage),
      },
      {
        path: 'privacy',
        loadComponent: () => import('./settings/privacy/privacy.page').then(m => m.PrivacyPage),
      },
      {
        path: 'notifications-config',
        loadComponent: () => import('./settings/notifications-config/notifications-config.page').then(m => m.NotificationsConfigPage),
      },
      {
        path: 'payment-method',
        loadComponent: () => import('./settings/payment-method/payment-method.page').then(m => m.PaymentMethodPage),
      },
    ],
  },
  {
    path: ':id',
    loadComponent: () => import('./public-profile/public-profile.page').then(m => m.PublicProfilePage),
  },
];
