import { Routes } from '@angular/router';

export const bookingsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./bookings.page').then((m) => m.BookingsPage),
  },
  {
    path: 'active',
    loadComponent: () =>
      import('./active/active.page').then((m) => m.ActivePage),
  },
  {
    path: 'history',
    loadComponent: () =>
      import('./history/history.page').then((m) => m.HistoryPage),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./detail/detail.page').then((m) => m.DetailPage),
  },
];