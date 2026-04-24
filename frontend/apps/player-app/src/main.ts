import { bootstrapApplication } from '@angular/platform-browser';
import {
  RouteReuseStrategy,
  provideRouter,
  withPreloading,
  PreloadAllModules,
} from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  IonicRouteStrategy,
  provideIonicAngular,
} from '@ionic/angular/standalone';

import { routes } from './app/app.routes';
import { AppComponent } from './app/app.component';
import { environment } from './environments/environment';
import { provideAuth, authInterceptor, errorInterceptor, GUEST_REDIRECT } from '@frontend/shared-auth';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { LOCALE_ID } from '@angular/core';
import {
  LucideAngularModule,
  Sun, Moon,
  ChevronUp, ChevronDown, ChevronLeft, ChevronRight, ArrowLeft,
  Clock, Calendar, CalendarX, MapPin,
  SlidersHorizontal, Search, SearchX, Warehouse, Zap, X,
  Star, Phone, Globe, Navigation, Mail,
  AlertCircle, ImageOff, Layers, Building2,
  Check, Users, Split, MessageSquare, Lock,
} from 'lucide-angular';

registerLocaleData(localeEs)

bootstrapApplication(AppComponent, {
  providers: [
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    provideIonicAngular({animated:false}),
    provideRouter(routes, withPreloading(PreloadAllModules)),
    provideHttpClient(
      withInterceptors([errorInterceptor, authInterceptor])
    ),
    provideAuth({ apiUrl: environment.apiUrl }),
    { provide: GUEST_REDIRECT, useValue: '/tabs/tab1' },
    { provide: LOCALE_ID, useValue: 'es-ES' },
    ...LucideAngularModule.pick({
      Sun, Moon,
      ChevronUp, ChevronDown, ChevronLeft, ChevronRight, ArrowLeft,
      Clock, Calendar, CalendarX, MapPin,
      SlidersHorizontal, Search, SearchX, Warehouse, Zap, X,
      Star, Phone, Globe, Navigation, Mail,
      AlertCircle, ImageOff, Layers, Building2,
      Check, Users, Split, MessageSquare, Lock,
    }).providers!,
  ],
});