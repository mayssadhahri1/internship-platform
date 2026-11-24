import { HttpClient, HttpErrorResponse, HttpInterceptorFn, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { Application, ApplicationStatus, Offer, Page, Role, Stats } from './models';

export const API_URL = '/api';

/** Ajoute le jeton JWT et déconnecte l'utilisateur si le serveur répond 401. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;
  const request = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(request).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && token) auth.logout();
      return throwError(() => err);
    }),
  );
};

/** Garde de route : connexion obligatoire, et rôle précis si indiqué dans data.role. */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) return router.createUrlTree(['/login']);
  const role = route.data['role'] as Role | undefined;
  return !role || auth.hasRole(role) ? true : router.createUrlTree(['/offers']);
};

/** Message d'erreur lisible à partir d'une réponse ProblemDetail du backend. */
export function errorMessage(err: HttpErrorResponse): string {
  const body = err.error;
  if (body?.errors) return Object.values(body.errors).join(' · ');
  return body?.detail ?? 'Une erreur est survenue, réessayez.';
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

  searchOffers(keyword: string, location: string, page = 0) {
    const params = new HttpParams().set('keyword', keyword).set('location', location).set('page', page).set('size', 9);
    return this.http.get<Page<Offer>>(`${API_URL}/offers`, { params });
  }

  getOffer(id: number) {
    return this.http.get<Offer>(`${API_URL}/offers/${id}`);
  }

  myOffers() {
    return this.http.get<Offer[]>(`${API_URL}/offers/mine`);
  }

  createOffer(offer: Partial<Offer>) {
    return this.http.post<Offer>(`${API_URL}/offers`, offer);
  }

  updateOffer(id: number, offer: Partial<Offer>) {
    return this.http.put<Offer>(`${API_URL}/offers/${id}`, offer);
  }

  apply(offerId: number, motivation: string) {
    return this.http.post<Application>(`${API_URL}/offers/${offerId}/applications`, { motivation });
  }

  myApplications() {
    return this.http.get<Application[]>(`${API_URL}/applications/mine`);
  }

  applicationsForOffer(offerId: number) {
    return this.http.get<Application[]>(`${API_URL}/offers/${offerId}/applications`);
  }

  changeStatus(id: number, status: ApplicationStatus) {
    return this.http.patch<Application>(`${API_URL}/applications/${id}/status`, { status });
  }

  stats() {
    return this.http.get<Stats>(`${API_URL}/stats`);
  }
}
