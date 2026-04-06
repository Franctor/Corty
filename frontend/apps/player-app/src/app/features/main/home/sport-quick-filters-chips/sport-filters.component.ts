import { Component, output, signal } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { footballOutline, tennisballOutline } from 'ionicons/icons';

export interface SportFilter {
    id: string;
    label: string;
    icon?: string;
}

const SPORT_FILTERS: SportFilter[] = [
    { id: 'all', label: 'Todos' },
    { id: 'futbol', label: 'Fútbol', icon: 'football-outline' },
    { id: 'padel', label: 'Pádel', icon: 'tennisball-outline' },
    { id: 'tenis', label: 'Tenis', icon: 'tennisball-outline' },
    { id: 'baloncesto', label: 'Basket', icon: 'tennisball-outline' },
];

@Component({
    selector: 'app-sport-filters',
    templateUrl: './sport-filters.component.html',
    styleUrl: './sport-filters.component.scss',
    standalone: true,
    imports: [IonIcon],
})
export class SportFiltersComponent {
    readonly filterChange = output<string>();

    readonly filters = SPORT_FILTERS;
    readonly activeFilter = signal('all');

    constructor() {
        addIcons({ footballOutline, tennisballOutline });
    }

    selectFilter(id: string): void {
        this.activeFilter.set(id);
        this.filterChange.emit(id);
    }
}