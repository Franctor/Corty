import { Component, input } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { checkmarkCircleOutline } from 'ionicons/icons';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-booking-price-breakdown',
  templateUrl: './booking-price-breakdown.component.html',
  styleUrl: './booking-price-breakdown.component.scss',
  standalone: true,
  imports: [IonIcon, DecimalPipe],
})
export class BookingPriceBreakdownComponent {
  readonly courtPrice = input.required<number>();
  readonly totalPrice = input.required<number>();
  readonly durationMin = input.required<number>();
  readonly isPaid = input<boolean>(false);

  constructor() {
    addIcons({ checkmarkCircleOutline });
  }
}
