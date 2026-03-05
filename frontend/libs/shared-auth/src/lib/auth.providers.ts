import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { AUTH_API_URL } from './services/auth.service';

export function provideAuth(config: { apiUrl: string }): EnvironmentProviders {
  return makeEnvironmentProviders([
    { provide: AUTH_API_URL, useValue: config.apiUrl },
  ]);
}