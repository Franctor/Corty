import { Component } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { personAddOutline, searchOutline } from 'ionicons/icons';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-find-player',
  templateUrl: './find-player.component.html',
  styleUrl: './find-player.component.scss',
  standalone: true,
  imports: [IonIcon, RouterLink],
})
export class FindPlayerComponent {
  constructor() {
    addIcons({ personAddOutline, searchOutline });
  }
}