import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  LucideAngularModule,
  LayoutDashboard,
  Building2,
  MapPin,
  CalendarDays,
  Users,
  Briefcase,
  Dumbbell,
  LogOut,
  Menu,
  Plus,
  Pencil,
  Trash2,
  X,
  Layers,
  Image,
  CheckCircle,
  XCircle,
  Info,
  AlertTriangle,
  PersonStanding,
} from 'lucide-angular';

import { appRoutes } from './app.routes';
import { environment } from '../environments/environment';
import { provideAuth, authInterceptor, errorInterceptor, GUEST_REDIRECT } from '@frontend/shared-auth';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(appRoutes),
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor])
    ),
    provideAuth({ apiUrl: environment.apiUrl }),
    { provide: GUEST_REDIRECT, useValue: '/dashboard' },
    ...LucideAngularModule.pick({
      LayoutDashboard,
      Building2,
      MapPin,
      CalendarDays,
      Users,
      Briefcase,
      Dumbbell,
      LogOut,
      Menu,
      Plus,
      Pencil,
      Trash2,
      X,
      Layers,
      Image,
      CheckCircle,
      XCircle,
      Info,
      AlertTriangle,
      PersonStanding,
    }).providers!,
  ],
};