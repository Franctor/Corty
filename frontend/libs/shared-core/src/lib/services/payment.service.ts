import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '../tokens/api.tokens';
import { Observable } from 'rxjs';

export interface PaymentIntentResponse {
  clientSecret: string;
  publishableKey: string;
  bookingId: number;
  amount: string;
  description: string;
}

export interface SetupIntentResponse {
  clientSecret: string;
  publishableKey: string;
}

export interface SavedCardResponse {
  brand: string;
  last4: string;
  expMonth: number;
  expYear: number;
  paymentMethodId: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private http   = inject(HttpClient);
  private apiUrl = inject(API_URL);

  createIntent(bookingId: number): Observable<PaymentIntentResponse> {
    return this.http.post<PaymentIntentResponse>(`${this.apiUrl}/payments/intent`, { bookingId });
  }

  createSetupIntent(): Observable<SetupIntentResponse> {
    return this.http.post<SetupIntentResponse>(`${this.apiUrl}/payments/setup-intent`, {});
  }

  getSavedCard(): Observable<SavedCardResponse | null> {
    return this.http.get<SavedCardResponse | null>(`${this.apiUrl}/payments/payment-method`);
  }

  savePaymentMethod(paymentMethodId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/payments/payment-method`, { paymentMethodId });
  }

  deletePaymentMethod(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/payments/payment-method`);
  }
}
