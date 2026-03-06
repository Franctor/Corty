import { Component } from '@angular/core';
import { IonContent, IonHeader, IonToolbar, IonTitle } from '@ionic/angular/standalone';

@Component({
  selector: 'app-bookings',
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Reservas</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content></ion-content>
  `,
  standalone: true,
  imports: [IonContent, IonHeader, IonToolbar, IonTitle],
})
export class BookingsPage {}