import { Routes } from '@angular/router';
import { roleGuard } from './core/api';
import { AuthComponent } from './pages/auth.component';
import { DashboardComponent } from './pages/dashboard.component';
import { MyApplicationsComponent } from './pages/my-applications.component';
import { OfferDetailComponent } from './pages/offer-detail.component';
import { OffersComponent } from './pages/offers.component';

export const routes: Routes = [
  { path: '', redirectTo: 'offers', pathMatch: 'full' },
  { path: 'login', component: AuthComponent, data: { mode: 'login' } },
  { path: 'register', component: AuthComponent, data: { mode: 'register' } },
  { path: 'offers', component: OffersComponent },
  { path: 'offers/:id', component: OfferDetailComponent },
  { path: 'applications', component: MyApplicationsComponent, canActivate: [roleGuard], data: { role: 'STUDENT' } },
  { path: 'dashboard', component: DashboardComponent, canActivate: [roleGuard], data: { role: 'COMPANY' } },
  { path: '**', redirectTo: 'offers' },
];
