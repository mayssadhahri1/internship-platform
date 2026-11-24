import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api';
import { Offer, Page } from '../core/models';

@Component({
  selector: 'app-offers',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <h1>Offres de stage</h1>
    <form class="search card" (ngSubmit)="load(0)">
      <input [(ngModel)]="keyword" name="keyword" placeholder="Mot-clé : Spring Boot, IA, mobile…" />
      <input [(ngModel)]="location" name="location" placeholder="Ville" />
      <button class="btn">Rechercher</button>
    </form>

    @if (page(); as p) {
      <p class="muted">{{ p.totalElements }} offre(s)</p>
      <div class="grid">
        @for (o of p.content; track o.id) {
          <a class="card offer" [routerLink]="['/offers', o.id]">
            <h3>{{ o.title }}</h3>
            <p class="muted">{{ o.companyName }} · {{ o.location }} · {{ o.durationMonths }} mois</p>
            <p>{{ o.description.length > 140 ? o.description.slice(0, 140) + '…' : o.description }}</p>
            @if (o.skills) {
              <div class="tags">
                @for (s of o.skills.split(','); track s) { <span class="tag">{{ s.trim() }}</span> }
              </div>
            }
          </a>
        } @empty {
          <p>Aucune offre ne correspond à votre recherche.</p>
        }
      </div>
      @if (p.totalPages > 1) {
        <div class="pager">
          <button class="btn ghost" [disabled]="p.number === 0" (click)="load(p.number - 1)">Précédent</button>
          <span>Page {{ p.number + 1 }} / {{ p.totalPages }}</span>
          <button class="btn ghost" [disabled]="p.number + 1 >= p.totalPages" (click)="load(p.number + 1)">Suivant</button>
        </div>
      }
    }
  `,
  styles: `
    .search { display: flex; gap: 8px; margin-bottom: 16px; }
    .search input { flex: 1; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
    .offer { color: inherit; text-decoration: none; transition: transform .15s, box-shadow .15s; }
    .offer:hover { transform: translateY(-2px); box-shadow: 0 6px 18px rgba(18, 58, 107, .08); }
    .offer h3 { margin: 0 0 4px; color: var(--ink); }
    .pager { display: flex; gap: 12px; align-items: center; justify-content: center; margin-top: 20px; }
    @media (max-width: 600px) { .search { flex-direction: column; } }
  `,
})
export class OffersComponent implements OnInit {
  private api = inject(ApiService);
  keyword = '';
  location = '';
  page = signal<Page<Offer> | null>(null);

  ngOnInit(): void {
    this.load(0);
  }

  load(page: number): void {
    this.api.searchOffers(this.keyword, this.location, page).subscribe((p) => this.page.set(p));
  }
}
