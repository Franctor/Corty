import { Component, input } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { sunnyOutline, businessOutline } from 'ionicons/icons';
import { RouterLink } from '@angular/router';

export interface NearbyCourtItem {
    id: number;
    name: string;
    clubName: string;
    sport: string;
    surface?: string;
    pricePerHour: number;
    distance: number;
    coverType: 'indoor' | 'outdoor';
    imageUrl?: string;
}

@Component({
    selector: 'app-nearby-court-card',
    templateUrl: './nearby-courts.component.html',
    styleUrl: './nearby-courts.component.scss',
    standalone: true,
    imports: [IonIcon, RouterLink],
})
export class NearbyCourtCardComponent {
    readonly court = input.required<NearbyCourtItem>();

    constructor() {
        addIcons({ sunnyOutline, businessOutline });
    }

    get coverIcon(): string {
        return this.court().coverType === 'indoor' ? 'business-outline' : 'sunny-outline';
    }

    get coverLabel(): string {
        return this.court().coverType === 'indoor' ? 'Techada' : 'Exterior';
    }
}