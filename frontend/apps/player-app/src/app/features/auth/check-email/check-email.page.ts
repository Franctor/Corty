import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IonContent, IonButton } from '@ionic/angular/standalone';
import { CortyLogoComponent } from '@frontend/shared-ui';
import { LucideAngularModule, Mail } from 'lucide-angular';

@Component({
  selector: 'app-check-email',
  templateUrl: 'check-email.page.html',
  styleUrls: ['check-email.page.scss'],
  standalone: true,
  imports: [IonContent, IonButton, RouterLink, CortyLogoComponent, LucideAngularModule],
})
export class CheckEmailPage {
  readonly mailIcon = Mail;
}
