import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from './api';
import { AuthResponse, Role, User } from './models';

const STORAGE_KEY = 'internship-auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  // État de connexion partagé par toute l'application
  private session = signal<AuthResponse | null>(this.restore());
  readonly user = computed(() => this.session()?.user ?? null);
  readonly isLoggedIn = computed(() => this.session() !== null);

  get token(): string | null {
    return this.session()?.token ?? null;
  }

  hasRole(role: Role): boolean {
    return this.user()?.role === role;
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, { email, password }).pipe(tap((r) => this.save(r)));
  }

  register(data: { fullName: string; email: string; password: string; role: Role; organization: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/register`, data).pipe(tap((r) => this.save(r)));
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
    this.router.navigate(['/login']);
  }

  private save(res: AuthResponse): void {
    const expiresAt = Date.now() + res.expiresIn * 1000;
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...res, expiresAt }));
    this.session.set(res);
  }

  private restore(): AuthResponse | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const data = JSON.parse(raw);
      // On ignore une session expirée
      return data.expiresAt > Date.now() ? data : null;
    } catch {
      return null;
    }
  }
}
