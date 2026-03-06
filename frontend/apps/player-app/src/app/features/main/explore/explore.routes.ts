import { Routes } from '@angular/router';

export const exploreRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./explore.page').then((m) => m.ExplorePage),
  },
  {
    path: 'courts',
    loadComponent: () =>
      import('./courts/courts.page').then((m) => m.CourtsPage),
  },
  {
    path: 'matches',
    loadComponent: () =>
      import('./matches/matches.page').then((m) => m.MatchesPage),
  },
];