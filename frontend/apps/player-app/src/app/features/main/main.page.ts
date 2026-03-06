import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import {
  IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel, IonRouterOutlet,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { home, search, calendar, chatbubbles, person } from 'ionicons/icons';

@Component({
  selector: 'app-main',
  templateUrl: 'main.page.html',
  standalone: true,
  imports: [
    IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel, IonRouterOutlet,
    RouterLink, RouterLinkActive,
  ],
})
export class MainPage {
  constructor() {
    addIcons({ home, search, calendar, chatbubbles, person });
  }
}