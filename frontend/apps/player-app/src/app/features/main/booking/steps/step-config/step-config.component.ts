import { Component, input, output, signal, OnInit } from '@angular/core';
import { IonToggle } from '@ionic/angular/standalone';
import { BookingType } from '@frontend/shared-core';
import { BookingState } from '../../booking.page';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-booking-step-config',
  templateUrl: './step-config.component.html',
  styleUrls: ['./step-config.component.scss'],
  standalone: true,
  imports: [IonToggle, LucideAngularModule],
})
export class BookingStepConfigComponent implements OnInit {
  readonly state = input.required<BookingState>();
  readonly done  = output<{ bookingType: BookingType; splitPayment: boolean; notes: string }>();

  readonly bookingType  = signal<BookingType>('PRIVATE');
  readonly splitPayment = signal(true);
  readonly notes        = signal('');

  ngOnInit(): void {
    this.bookingType.set(this.state().bookingType);
    this.splitPayment.set(this.state().splitPayment);
    this.notes.set(this.state().notes);
  }

  setType(type: BookingType): void {
    this.bookingType.set(type);
  }

  onNotesInput(event: Event): void {
    this.notes.set((event.target as HTMLTextAreaElement).value);
  }

  onNext(): void {
    this.done.emit({
      bookingType:  this.bookingType(),
      splitPayment: this.splitPayment(),
      notes:        this.notes(),
    });
  }
}
