import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { errorMessage } from '../core/api';
import { AuthService } from '../core/auth.service';

/** Page de connexion et d'inscription (le mode vient de la route). */
@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth card">
      <h1>{{ mode() === 'login' ? 'Connexion' : 'Créer un compte' }}</h1>

      <form [formGroup]="form" (ngSubmit)="submit()">
        @if (mode() === 'register') {
          <div class="roles">
            <label [class.active]="form.value.role === 'STUDENT'">
              <input type="radio" formControlName="role" value="STUDENT" /> Étudiant(e)
            </label>
            <label [class.active]="form.value.role === 'COMPANY'">
              <input type="radio" formControlName="role" value="COMPANY" /> Entreprise
            </label>
          </div>
          <label>Nom complet <input formControlName="fullName" /></label>
          <label>{{ form.value.role === 'COMPANY' ? 'Société' : 'École / université' }}
            <input formControlName="organization" />
          </label>
        }
        <label>Email <input type="email" formControlName="email" autocomplete="email" /></label>
        <label>Mot de passe <input type="password" formControlName="password" autocomplete="current-password" /></label>

        @if (error()) { <p class="error">{{ error() }}</p> }
        <button class="btn" [disabled]="loading()">{{ mode() === 'login' ? 'Se connecter' : "S'inscrire" }}</button>
      </form>

      @if (mode() === 'login') {
        <p class="muted">Pas de compte ? <a routerLink="/register">Inscrivez-vous</a></p>
        <p class="muted small">Démo : etudiant&#64;demo.tn ou rh&#64;techsousse.tn / Demo1234!</p>
      } @else {
        <p class="muted">Déjà inscrit(e) ? <a routerLink="/login">Connectez-vous</a></p>
      }
    </section>
  `,
  styles: `
    .auth { max-width: 420px; margin: 40px auto; }
    .roles { display: flex; gap: 8px; margin-bottom: 12px; }
    .roles label { flex: 1; border: 1px solid var(--line); border-radius: 8px; padding: 10px; cursor: pointer; text-align: center; }
    .roles label.active { border-color: var(--accent); background: var(--accent-soft); }
    .roles input { display: none; }
    .small { font-size: 12px; }
  `,
})
export class AuthComponent {
  mode = input<'login' | 'register'>('login');

  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  loading = signal(false);
  error = signal('');

  form = this.fb.nonNullable.group({
    role: 'STUDENT' as 'STUDENT' | 'COMPANY',
    fullName: [''],
    organization: [''],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.error.set('Email valide et mot de passe de 8 caractères minimum requis.');
      return;
    }
    const v = this.form.getRawValue();
    const request = this.mode() === 'login' ? this.auth.login(v.email, v.password) : this.auth.register(v);

    this.loading.set(true);
    this.error.set('');
    request.subscribe({
      next: (res) => this.router.navigate([res.user.role === 'COMPANY' ? '/dashboard' : '/offers']),
      error: (err: HttpErrorResponse) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
