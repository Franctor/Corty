import { Component} from '@angular/core';
import { IonTabs, IonRouterLink } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { search, calendar, chatbubbles, person,  home } from 'ionicons/icons';
import { IonRouterOutlet, IonTabBar,  IonIcon, IonTabButton, IonLabel } from "@ionic/angular/standalone";
import { RouterModule } from '@angular/router';

interface NavItem {
    tab: string;
    href: string;
    icon: string;
    label: string;
}

@Component({
    selector: 'app-bottomnav',
    templateUrl: './bottomnav.component.html',
    styleUrls: ['./bottomnav.component.scss'],
    standalone: true,
    imports: [
    IonTabBar,
    IonIcon,
    IonTabButton,
    IonLabel,
    RouterModule
],
})
export class BottomnavComponent {
    readonly navItems: NavItem[] = [
        { tab: 'home', href: '/home', icon: 'home', label: 'Inicio' },
        { tab: 'explore', href: '/explore', icon: 'search', label: 'Explorar' },
        { tab: 'bookings', href: '/bookings', icon: 'calendar', label: 'Reservas' },
        { tab: 'social', href: '/social', icon: 'chatbubbles', label: 'Social' },
        { tab: 'profile', href: '/profile', icon: 'person', label: 'Perfil' },
    ];

    constructor() {
        addIcons({ home, search, calendar, chatbubbles, person });
    }
}