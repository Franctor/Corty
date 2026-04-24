import { Component, computed, input, output, signal } from '@angular/core';
import { SportFilterResponse, MediaUrlPipe } from '@frontend/shared-core';
export interface SportFilter {
    id: string;
    label: string;
    iconUrl?: string;
}

@Component({
    selector: 'app-sport-filters',
    templateUrl: './sport-filters.component.html',
    styleUrl: './sport-filters.component.scss',
    standalone: true,
    imports: [MediaUrlPipe],
})
export class SportFiltersComponent {
    readonly sports = input<SportFilterResponse[]>([]);
    readonly filterChange = output<string>();

    readonly activeFilter = signal('all');

    // "Todos" fijo al inicio + deportes dinámicos de la API
    readonly filters = computed<SportFilter[]>(() => [
        { id: 'all', label: 'Todos' },
        ...this.sports().map(s => ({
            id: String(s.id),
            label: s.name,
            iconUrl: s.iconUrl,
        })),
    ]);

    selectFilter(id: string): void {
        this.activeFilter.set(id);
        this.filterChange.emit(id);
    }
}