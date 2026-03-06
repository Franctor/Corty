import { Component } from '@angular/core';
import { IonContent, IonHeader, IonToolbar, IonTitle, IonBackButton, IonButtons } from '@ionic/angular/standalone';

@Component({
  selector: 'app-history',
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button defaultHref="/bookings"></ion-back-button>
        </ion-buttons>
        <ion-title>Historial</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content></ion-content>
  `,
  standalone: true,
  imports: [IonContent, IonHeader, IonToolbar, IonTitle, IonBackButton, IonButtons],
})
export class HistoryPage {}