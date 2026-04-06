import { Component, input } from '@angular/core';
import { DatePipe, UpperCasePipe } from '@angular/common';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { peopleOutline, chevronForwardOutline } from 'ionicons/icons';
import { RouterLink } from '@angular/router';

export interface NextBooking {
    id: number;
    courtLabel: string;
    clubName: string;
    date: string;
    time: string;
    sport: string;
    pendingPlayers: number;
    totalPlayers: number;
}

@Component({
    selector: 'app-next-booking-card',
    templateUrl: './next-booking-card.component.html',
    styleUrl: './next-booking-card.component.scss',
    standalone: true,
    imports: [DatePipe, IonIcon, RouterLink, UpperCasePipe],
})
export class NextBookingCardComponent {
    readonly booking = input<NextBooking | null>(null);

    constructor() {
        addIcons({ peopleOutline, chevronForwardOutline });
    }
}