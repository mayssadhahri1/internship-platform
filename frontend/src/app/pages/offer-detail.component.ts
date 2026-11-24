import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService, errorMessage } from '../core/api';
import { AuthService } from '../core/auth.service';
import { Offer } from '../core/models';

@Component({
  selector: 'app-offer-detail',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe],
  template: `
    <a routerLink="/offers" class="muted">← Toutes les offres</a>
    @if (offer(); as o) {
      <article class="card">
        <h1>{{ o.title }}</h1>
        <p class="muted">{{ o.companyName }} · {{ o.location }} · {{ o.durationMonths }} mois · publiée le {{ o.createdAt | date: 'dd/MM/yyyy' }}</p>
        <p class="description">{{ o.description }}</p>
        @if (o.skills) {
          <div class="tags">@for (s of o.skills.split(','); track s) { <span class="tag">{{ s.trim() }}</span> }</div>
        }
      </article>

      <section class="card">
        @if (!auth.isLoggedIn()) {
          <p><a routerLink="/login">Connectez-vous</a> en tant qu'étudiant(e) pour postuler.</p>
        } @else if (auth.hasRole('STUDENT')) {
          @if (sent()) {
            <p class="success">Candidature envoyée ! Suivez-la dans « Mes candidatures ».</p>
          } @else if (!o.open) {
            <p class="muted">Cette offre est fermée.</p>
          } @else {
            <h2>Postuler</h2>
            <textarea [(ngModel)]="motivation" rows="5" placeholder="Pourquoi ce stage vous intéresse (20 caractères minimum)"></textarea>
            @if (error()) { <p class="error">{{ error() }}</p> }
            <button class="btn" (click)="apply(o.id)" [disabled]="motivation.trim().length < 20">Envoyer ma candidature</button>
          }
        }
      </section>
    }
  `,
  styles: `
    .description { white-space: pre-line; line-height: 1.6; }
    textarea { width: 100%; margin-bottom: 10px; }
  `,
})
export class OfferDetailComponent implements OnInit {
  id = input.required<string>();

  private api = inject(ApiService);
  auth = inject(AuthService);

  offer = signal<Offer | null>(null);
  motivation = '';
  sent = signal(false);
  error = signal('');

  ngOnInit(): void {
    this.api.getOffer(+this.id()).subscribe((o) => this.offer.set(o));
  }

  apply(offerId: number): void {
    this.api.apply(offerId, this.motivation).subscribe({
      next: () => this.sent.set(true),
      error: (err: HttpErrorResponse) => this.error.set(errorMessage(err)),
    });
  }
}
