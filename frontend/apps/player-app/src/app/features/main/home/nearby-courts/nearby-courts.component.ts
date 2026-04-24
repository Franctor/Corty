import { Component, inject, input } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { sunnyOutline, businessOutline } from 'ionicons/icons';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { GeoService, MediaUrlPipe } from '@frontend/shared-core';

export interface NearbyCourtItem {
    id: number;
    name: string;
    clubName: string;
    clubCity?: string;
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
    imports: [IonIcon, RouterLink, MediaUrlPipe, DecimalPipe],
})
export class NearbyCourtCardComponent {
    readonly court = input.required<NearbyCourtItem>();
    readonly geoService = inject(GeoService);

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