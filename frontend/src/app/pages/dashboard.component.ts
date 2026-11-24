import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService, errorMessage } from '../core/api';
import { Application, ApplicationStatus, Offer, Stats } from '../core/models';
import { StatusLabelPipe } from './status-label.pipe';

/** Espace entreprise : statistiques, publication d'offres et traitement des candidatures. */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, StatusLabelPipe],
  template: `
    <h1>Tableau de bord</h1>

    @if (stats(); as s) {
      <div class="kpis">
        <div class="card kpi"><span>{{ s.openOffers }}</span>Offres ouvertes</div>
        <div class="card kpi"><span>{{ s.applications }}</span>Candidatures</div>
        <div class="card kpi"><span>{{ s.pending }}</span>En attente</div>
        <div class="card kpi"><span>{{ s.accepted }}</span>Acceptées</div>
      </div>
    }

    <div class="layout">
      <section class="card">
        <h2>Publier une offre</h2>
        <form [formGroup]="form" (ngSubmit)="create()">
          <label>Titre <input formControlName="title" /></label>
          <label>Description <textarea formControlName="description" rows="4"></textarea></label>
          <div class="row">
            <label>Ville <input formControlName="location" /></label>
            <label>Durée (mois) <input type="number" formControlName="durationMonths" min="1" max="12" /></label>
          </div>
          <label>Compétences (séparées par des virgules) <input formControlName="skills" /></label>
          @if (error()) { <p class="error">{{ error() }}</p> }
          <button class="btn" [disabled]="form.invalid">Publier</button>
        </form>
      </section>

      <section class="card">
        <h2>Mes offres</h2>
        @for (o of offers(); track o.id) {
          <div class="offer" [class.selected]="selected()?.id === o.id">
            <div>
              <strong>{{ o.title }}</strong>
              <p class="muted">{{ o.location }} · {{ o.createdAt | date: 'dd/MM/yyyy' }} · {{ o.open ? 'Ouverte' : 'Fermée' }}</p>
            </div>
            <div class="actions">
              <button class="btn ghost" (click)="select(o)">Candidats</button>
              <button class="btn ghost" (click)="toggle(o)">{{ o.open ? 'Fermer' : 'Rouvrir' }}</button>
            </div>
          </div>
        } @empty {
          <p class="muted">Aucune offre publiée pour l'instant.</p>
        }
      </section>
    </div>

    @if (selected(); as o) {
      <section class="card">
        <h2>Candidats : {{ o.title }}</h2>
        <table>
          <thead><tr><th>Candidat</th><th>École</th><th>Motivation</th><th>Statut</th><th></th></tr></thead>
          <tbody>
            @for (a of applicants(); track a.id) {
              <tr>
                <td>{{ a.studentName }}<br /><small class="muted">{{ a.studentEmail }}</small></td>
                <td>{{ a.studentSchool }}</td>
                <td class="motivation">{{ a.motivation }}</td>
                <td><span class="badge" [class]="a.status">{{ a.status | statusLabel }}</span></td>
                <td class="actions">
                  <button class="btn ghost" (click)="decide(a, 'ACCEPTED')" [disabled]="a.status === 'ACCEPTED'">Accepter</button>
                  <button class="btn ghost danger" (click)="decide(a, 'REJECTED')" [disabled]="a.status === 'REJECTED'">Refuser</button>
                </td>
              </tr>
            } @empty {
              <tr><td colspan="5" class="muted">Aucune candidature pour cette offre.</td></tr>
            }
          </tbody>
        </table>
      </section>
    }
  `,
  styles: `
    .kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
    .kpi { color: var(--muted); }
    .kpi span { display: block; font-size: 28px; font-weight: 700; color: var(--accent); }
    .layout { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
    .row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
    .offer { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--line); }
    .offer.selected { background: var(--accent-soft); }
    .offer p { margin: 2px 0 0; }
    .actions { display: flex; gap: 6px; }
    .motivation { max-width: 320px; font-size: 13px; }
    @media (max-width: 800px) { .kpis { grid-template-columns: repeat(2, 1fr); } .layout { grid-template-columns: 1fr; } }
  `,
})
export class DashboardComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);

  stats = signal<Stats | null>(null);
  offers = signal<Offer[]>([]);
  selected = signal<Offer | null>(null);
  applicants = signal<Application[]>([]);
  error = signal('');

  form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    description: ['', Validators.required],
    location: ['', Validators.required],
    durationMonths: [6, [Validators.required, Validators.min(1), Validators.max(12)]],
    skills: [''],
  });

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.api.stats().subscribe((s) => this.stats.set(s));
    this.api.myOffers().subscribe((o) => this.offers.set(o));
  }

  create(): void {
    this.api.createOffer(this.form.getRawValue()).subscribe({
      next: () => {
        this.form.reset({ durationMonths: 6 });
        this.error.set('');
        this.refresh();
      },
      error: (err: HttpErrorResponse) => this.error.set(errorMessage(err)),
    });
  }

  toggle(o: Offer): void {
    this.api.updateOffer(o.id, { ...o, open: !o.open }).subscribe(() => this.refresh());
  }

  select(o: Offer): void {
    this.selected.set(o);
    this.api.applicationsForOffer(o.id).subscribe((list) => this.applicants.set(list));
  }

  decide(a: Application, status: ApplicationStatus): void {
    this.api.changeStatus(a.id, status).subscribe((updated) => {
      this.applicants.update((list) => list.map((x) => (x.id === updated.id ? updated : x)));
      this.api.stats().subscribe((s) => this.stats.set(s));
    });
  }
}
