import { Component, input } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { chevronForwardOutline, footballOutline, tennisballOutline, basketballOutline } from 'ionicons/icons';
import { RouterLink } from '@angular/router';

export interface RecentActivityItem {
  id: number;
  sport: string;
  sportIcon: string;
  description: string;      
  result?: string;          
  timeAgo: string;          
  iconColor: string;       
}

@Component({
  selector: 'app-recent-activity-item',
  templateUrl: './recent-activity.component.html',
  styleUrl: './recent-activity.component.scss',
  standalone: true,
  imports: [IonIcon, RouterLink],
})
export class RecentActivityItemComponent {
  readonly item = input.required<RecentActivityItem>();

  constructor() {
    addIcons({ chevronForwardOutline, footballOutline, tennisballOutline, basketballOutline });
  }
}