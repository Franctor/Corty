import { Component, inject, signal } from '@angular/core';
import { IonContent, IonToggle } from '@ionic/angular/standalone';
import { LucideAngularModule } from 'lucide-angular';
import { PageHeaderComponent } from '../../../../../components/page-header/page-header.component';
import { NotificationService, NotificationType } from '@frontend/shared-core';

interface NotifGroup {
  label: string;
  items: { type: NotificationType; label: string; sub: string }[];
}

const STORAGE_KEY = 'notif_prefs';

const GROUPS: NotifGroup[] = [
  {
    label: 'Reservas',
    items: [
      { type: 'BOOKING_CONFIRMED',  label: 'Reserva confirmada',     sub: 'Cuando una reserva es aceptada' },
      { type: 'BOOKING_CANCELLED',  label: 'Reserva cancelada',      sub: 'Cuando una reserva es cancelada' },
      { type: 'JOIN_REQUEST',       label: 'Solicitudes de unión',   sub: 'Cuando alguien pide unirse a tu reserva' },
      { type: 'JOIN_ACCEPTED',      label: 'Solicitud aceptada',     sub: 'Cuando aceptan tu solicitud de unión' },
      { type: 'JOIN_REJECTED',      label: 'Solicitud rechazada',    sub: 'Cuando rechazan tu solicitud de unión' },
      { type: 'PARTICIPANT_JOINED', label: 'Nuevo participante',     sub: 'Cuando alguien se une a tu reserva' },
      { type: 'PARTICIPANT_LEFT',   label: 'Participante se fue',    sub: 'Cuando alguien abandona tu reserva' },
      { type: 'RESULT_PENDING',     label: 'Resultado pendiente',    sub: 'Cuando toca registrar el resultado' },
      { type: 'MATCH_READY',        label: 'Partido listo',          sub: 'Cuando el partido está completo' },
    ],
  },
  {
    label: 'Social',
    items: [
      { type: 'FRIEND_REQUEST',  label: 'Solicitud de amistad', sub: 'Cuando alguien te envía una solicitud' },
      { type: 'FRIEND_ACCEPTED', label: 'Amistad aceptada',     sub: 'Cuando aceptan tu solicitud de amistad' },
      { type: 'NEW_MESSAGE',     label: 'Mensajes',             sub: 'Cuando recibes un mensaje nuevo' },
    ],
  },
  {
    label: 'Logros',
    items: [
      { type: 'LEVEL_UP', label: 'Subida de nivel', sub: 'Cuando subes de nivel en un deporte' },
    ],
  },
];

@Component({
  selector: 'app-notifications-config',
  templateUrl: './notifications-config.page.html',
  styleUrl: './notifications-config.page.scss',
  standalone: true,
  imports: [IonContent, IonToggle, LucideAngularModule, PageHeaderComponent],
})
export class NotificationsConfigPage {
  private notificationService = inject(NotificationService);

  readonly groups = GROUPS;
  readonly prefs  = signal<Record<string, boolean>>({});

  ionViewWillEnter(): void {
    // Cargar desde backend; fallback a localStorage si falla
    this.notificationService.getNotifPrefs().subscribe({
      next: prefs => {
        this.prefs.set(prefs);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs));
      },
      error: () => {
        try {
          const raw = localStorage.getItem(STORAGE_KEY);
          if (raw) this.prefs.set(JSON.parse(raw));
        } catch { /* ignore */ }
      },
    });
  }

  isEnabled(type: NotificationType): boolean {
    return this.prefs()[type] !== false;
  }

  toggle(type: NotificationType): void {
    const next = { ...this.prefs(), [type]: !this.isEnabled(type) };
    this.prefs.set(next);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    this.notificationService.saveNotifPrefs(next).subscribe();
  }
}
