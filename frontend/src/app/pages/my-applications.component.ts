import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api';
import { Application } from '../core/models';
import { StatusLabelPipe } from './status-label.pipe';

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [DatePipe, RouterLink, StatusLabelPipe],
  template: `
    <h1>Mes candidatures</h1>
    <div class="card">
      <table>
        <thead><tr><th>Offre</th><th>Entreprise</th><th>Date</th><th>Statut</th></tr></thead>
        <tbody>
          @for (a of applications(); track a.id) {
            <tr>
              <td><a [routerLink]="['/offers', a.offerId]">{{ a.offerTitle }}</a></td>
              <td>{{ a.companyName }}</td>
              <td>{{ a.createdAt | date: 'dd/MM/yyyy' }}</td>
              <td><span class="badge" [class]="a.status">{{ a.status | statusLabel }}</span></td>
            </tr>
          } @empty {
            <tr><td colspan="4">Vous n'avez pas encore postulé. <a routerLink="/offers">Voir les offres</a></td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class MyApplicationsComponent implements OnInit {
  private api = inject(ApiService);
  applications = signal<Application[]>([]);

  ngOnInit(): void {
    this.api.myApplications().subscribe((list) => this.applications.set(list));
  }
}
