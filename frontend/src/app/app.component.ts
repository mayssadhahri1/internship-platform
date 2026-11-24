import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header>
      <a routerLink="/offers" class="brand">Stage<span>Hub</span></a>
      <nav>
        <a routerLink="/offers" routerLinkActive="active">Offres</a>
        @if (auth.hasRole('STUDENT')) { <a routerLink="/applications" routerLinkActive="active">Mes candidatures</a> }
        @if (auth.hasRole('COMPANY')) { <a routerLink="/dashboard" routerLinkActive="active">Tableau de bord</a> }
      </nav>
      <div class="user">
        @if (auth.user(); as u) {
          <span class="muted">{{ u.fullName }}</span>
          <button class="btn ghost" (click)="auth.logout()">Déconnexion</button>
        } @else {
          <a class="btn" routerLink="/login">Connexion</a>
        }
      </div>
    </header>
    <main><router-outlet /></main>
  `,
  styles: `
    header { display: flex; align-items: center; gap: 24px; padding: 14px 28px; background: #fff; border-bottom: 1px solid var(--line); position: sticky; top: 0; z-index: 10; }
    .brand { font-weight: 800; font-size: 20px; color: var(--ink); text-decoration: none; }
    .brand span { color: var(--accent); }
    nav { display: flex; gap: 16px; flex: 1; }
    nav a { color: var(--muted); text-decoration: none; font-weight: 500; }
    nav a.active { color: var(--accent); }
    .user { display: flex; gap: 10px; align-items: center; }
    main { max-width: 1100px; margin: 0 auto; padding: 24px 20px 60px; }
    @media (max-width: 700px) { header { flex-wrap: wrap; gap: 10px; } .user .muted { display: none; } }
  `,
})
export class AppComponent {
  auth = inject(AuthService);
}
