import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.corty.app',
  appName: 'Corty',
  webDir: '../../dist/apps/player-app/browser',
  server: {
    androidScheme: 'https',
  },
};

export default config;
