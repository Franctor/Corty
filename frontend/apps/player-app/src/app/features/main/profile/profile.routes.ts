import { Routes } from '@angular/router';

export const profileRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./profile.page').then((m) => m.ProfilePage),
  },
  {
    path: 'stats',
    loadComponent: () =>
      import('./stats/stats.page').then((m) => m.StatsPage),
  },
  {
    path: 'settings',
    loadComponent: () =>
      import('./settings/settings.page').then((m) => m.SettingsPage),
  },
];