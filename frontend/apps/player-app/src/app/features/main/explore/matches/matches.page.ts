import { Component } from '@angular/core';
import { IonContent, IonHeader, IonToolbar, IonTitle, IonBackButton, IonButtons } from '@ionic/angular/standalone';

@Component({
  selector: 'app-matches',
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button defaultHref="/explore"></ion-back-button>
        </ion-buttons>
        <ion-title>Buscar partidos</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content></ion-content>
  `,
  standalone: true,
  imports: [IonContent, IonHeader, IonToolbar, IonTitle, IonBackButton, IonButtons],
})
export class MatchesPage {}