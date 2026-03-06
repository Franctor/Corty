import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { API_URL } from '@frontend/shared-core';

export function provideAuth(config: { apiUrl: string }): EnvironmentProviders {
  return makeEnvironmentProviders([
    { provide: API_URL, useValue: config.apiUrl },
  ]);
}