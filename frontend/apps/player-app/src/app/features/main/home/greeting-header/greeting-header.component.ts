import { Component, computed, inject } from '@angular/core';
import { DatePipe, UpperCasePipe } from '@angular/common';
import { TokenService } from '@frontend/shared-auth';

@Component({
    selector: 'app-greeting-header',
    templateUrl: './greeting-header.component.html',
    styleUrl: './greeting-header.component.scss',
    standalone: true,
    imports: [DatePipe, UpperCasePipe],
})
export class GreetingHeaderComponent {
    private tokenService = inject(TokenService);

    readonly today = new Date();

    readonly username = computed(() => {
        const raw = this.tokenService.token();
        let resolvedName = 'Jugador';
        if (raw) {
            try {
                const payload = JSON.parse(atob(raw.split('.')[1]));
                resolvedName = (payload?.['sub'] as string | undefined) ?? 'Jugador';
            } catch {
                resolvedName = 'Jugador';
            }
        }
        return resolvedName;
    });
}