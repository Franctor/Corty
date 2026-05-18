# Corty

Plataforma digital de gestión de clubs deportivos. Permite a los jugadores buscar y reservar pistas, unirse a partidos abiertos y gestionar su perfil, y a los clubs administrar instalaciones, horarios, reservas y balance financiero.

Proyecto de Fin de Ciclo · CFGS Desarrollo de Aplicaciones Web · 2025–2026.

## Estructura del repositorio

```
Corty/
├── backend/    Java 21 · Spring Boot 4 · MySQL · Stripe · Firebase · Brevo
└── frontend/   Monorepo NX con Angular 21 + Ionic 8 + Astro
    ├── apps/
    │   ├── player-app/    App móvil (Android APK) y web del jugador
    │   ├── admin-web/     Panel de administración para gestores de clubs
    │   └── landing/       Sitio de marketing
    └── libs/
        ├── shared-ui/     Tokens de diseño y componentes
        ├── shared-core/   Modelos, servicios y utilidades
        └── shared-auth/   Autenticación JWT, guards e interceptores
```

## Despliegue

- **Backend + MySQL**: Railway
- **admin-web + landing**: Vercel
- **player-app**: APK firmado para Android


## Autor

Francisco Castillo Torres

## Licencia

Corty © 2026 by Francisco Castillo Torres está licenciado bajo
[CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/).

El texto completo de la licencia está disponible en el archivo [`LICENSE`](LICENSE).
