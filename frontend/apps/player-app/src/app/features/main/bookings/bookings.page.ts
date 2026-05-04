import { Component, inject, signal } from '@angular/core';
import { IonContent, IonSpinner } from '@ionic/angular/standalone';
import { BookingService, BookingListItemResponse, BookingStatus } from '@frontend/shared-core';
import { BookingCardComponent } from './components/booking-card/booking-card.component';
import { LucideAngularModule } from 'lucide-angular';
import { PageHeaderComponent } from '../../../components/page-header/page-header.component';

type FilterTab = 'upcoming' | 'past' | 'cancelled';

const TAB_STATUSES: Record<FilterTab, BookingStatus[]> = {
  upcoming:  ['CONFIRMED', 'PENDING_PAYMENT'],
  past:      ['COMPLETED'],
  cancelled: ['CANCELLED'],
};

interface TabState {
  loading: boolean;
  bookings: BookingListItemResponse[];
}

@Component({
  selector: 'app-bookings',
  templateUrl: './bookings.page.html',
  styleUrls: ['./bookings.page.scss'],
  standalone: true,
  imports: [IonContent, IonSpinner, PageHeaderComponent, BookingCardComponent, LucideAngularModule],
})
export class BookingsPage {
  private bookingService = inject(BookingService);

  readonly activeTab = signal<FilterTab>('upcoming');
  readonly state = signal<TabState>({ loading: true, bookings: [] });

  readonly tabs: { key: FilterTab; label: string }[] = [
    { key: 'upcoming',  label: 'Próximas'   },
    { key: 'past',      label: 'Pasadas'    },
    { key: 'cancelled', label: 'Canceladas' },
  ];

  ionViewWillEnter(): void {
    this.loadTab(this.activeTab());
  }

  selectTab(tab: FilterTab): void {
    this.activeTab.set(tab);
    this.loadTab(tab);
  }

  private loadTab(tab: FilterTab): void {
    this.state.set({ loading: true, bookings: [] });
    this.bookingService.getMyBookings(TAB_STATUSES[tab]).subscribe({
      next: data  => this.state.set({ loading: false, bookings: data }),
      error: ()   => this.state.set({ loading: false, bookings: [] }),
    });
  }
}
