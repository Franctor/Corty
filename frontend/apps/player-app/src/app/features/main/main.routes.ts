import { Routes } from '@angular/router';
import { MainPage } from './main.page';

export const mainRoutes: Routes = [
  {
    path: '',
    component: MainPage,
    children: [
      {
        path: 'home',
        loadComponent: () =>
          import('./home/home.page').then((m) => m.HomePage),
      },
      {
        path: 'explore',
        loadChildren: () =>
          import('./explore/explore.routes').then((m) => m.exploreRoutes),
      },
      {
        path: 'bookings',
        loadChildren: () =>
          import('./bookings/bookings.routes').then((m) => m.bookingsRoutes),
      },
      {
        path: 'social',
        loadChildren: () =>
          import('./social/social.routes').then((m) => m.socialRoutes),
      },
      {
        path: 'profile',
        loadChildren: () =>
          import('./profile/profile.routes').then((m) => m.profileRoutes),
      },
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full',
      },
      {
        path: '**',
        redirectTo: 'home',
        pathMatch: 'full'
      }
    ],
  },
];