import { defineConfig } from 'vitest/config';
import { resolve } from 'path';

export default defineConfig({
  test: {
    globals: true,
    environment: 'jsdom',
    include: [
      'apps/player-app/src/app/services/*.spec.ts',
      'apps/admin-web/src/app/services/*.spec.ts',
    ],
    setupFiles: ['./vitest-setup.ts'],
  },
  resolve: {
    alias: {
      '@frontend/shared-auth': resolve(__dirname, 'libs/shared-auth/src/index.ts'),
      '@frontend/shared-core': resolve(__dirname, 'libs/shared-core/src/index.ts'),
    },
  },
});
