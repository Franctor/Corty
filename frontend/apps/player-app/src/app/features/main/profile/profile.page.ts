import { Component } from '@angular/core';
import { IonContent, IonHeader, IonToolbar, IonTitle } from '@ionic/angular/standalone';

@Component({
  selector: 'app-profile',
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Perfil</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content></ion-content>
  `,
  standalone: true,
  imports: [IonContent, IonHeader, IonToolbar, IonTitle],
})
export class ProfilePage {}