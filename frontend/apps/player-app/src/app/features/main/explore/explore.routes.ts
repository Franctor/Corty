import { Routes } from '@angular/router';

export const exploreRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./courts/courts.page').then((m) => m.CourtsPage),
  },
  {
    path: 'court/:id',
    loadComponent: () =>
      import('./courts/court.page').then((m) => m.CourtPage),
  },
  {
    path: 'matches',
    loadComponent: () =>
      import('./matches/matches.page').then((m) => m.MatchesPage),
  },
];