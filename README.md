# StageHub : plateforme de gestion des stages

Application full-stack qui relie les étudiants et les entreprises : les entreprises publient des offres de stage, les étudiants postulent, et chaque candidature est suivie jusqu'à la décision.

![CI](https://github.com/mayssadhahri1/internship-platform/actions/workflows/ci.yml/badge.svg)

## Fonctionnalités

**Étudiant(e)**
- Recherche d'offres par mot-clé (titre, description, compétences) et par ville, avec pagination.
- Candidature avec lettre de motivation (une seule candidature par offre).
- Suivi du statut : en attente, acceptée ou refusée.

**Entreprise**
- Publication, modification, fermeture et suppression de ses offres.
- Liste des candidats par offre, puis acceptation ou refus.
- Tableau de bord : offres ouvertes, candidatures reçues, en attente, acceptées.

**Sécurité**
- Authentification par JWT (sans session), mots de passe hachés avec BCrypt.
- Rôles `STUDENT`, `COMPANY` et `ADMIN` contrôlés avec `@PreAuthorize`.
- Vérification de propriété : une entreprise ne voit et ne modifie que ses propres offres et candidatures.
- Erreurs au format standard ProblemDetail (RFC 7807), avec le détail des champs invalides.

## Stack technique

| Couche | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA, Bean Validation, JJWT |
| Base de données | PostgreSQL (Docker), H2 en développement et pour les tests |
| Frontend | Angular 18 (composants standalone, signals, nouveau control flow), formulaires réactifs |
| Qualité | Tests d'intégration MockMvc, GitHub Actions |
| Déploiement | Docker multi-étapes, Nginx, Docker Compose |

## Architecture

```
frontend (Angular) ──/api──► Nginx ──► backend (Spring Boot) ──► PostgreSQL
                                          │
                                          ├── user/          inscription, connexion, JWT
                                          ├── offer/         offres + recherche paginée
                                          ├── application/   candidatures + statuts
                                          ├── stats/         indicateurs du tableau de bord
                                          ├── security/      filtre JWT, utilisateur courant
                                          └── common/        gestion centralisée des erreurs
```

## API REST

| Méthode | Route | Accès |
|---|---|---|
| `POST` | `/api/auth/register` · `/api/auth/login` | Public |
| `GET` | `/api/auth/me` | Connecté |
| `GET` | `/api/offers?keyword=&location=&page=` | Public |
| `GET` | `/api/offers/{id}` | Public |
| `GET` | `/api/offers/mine` | Entreprise |
| `POST` · `PUT` · `DELETE` | `/api/offers` · `/api/offers/{id}` | Entreprise propriétaire |
| `POST` | `/api/offers/{id}/applications` | Étudiant |
| `GET` | `/api/applications/mine` | Étudiant |
| `GET` | `/api/offers/{id}/applications` | Entreprise propriétaire |
| `PATCH` | `/api/applications/{id}/status` | Entreprise propriétaire |
| `GET` | `/api/stats` | Connecté |

## Lancer le projet

### Avec Docker

```bash
docker compose up --build
```

- Application : http://localhost:4200
- API : http://localhost:8080

### En développement

```bash
# Backend (H2 en mémoire, données de démo)
cd backend && mvn spring-boot:run

# Frontend (proxy /api → localhost:8080)
cd frontend && npm install && npx ng serve
```

**Comptes de démonstration** (mot de passe `Demo1234!`) :
- Étudiant : `etudiant@demo.tn`
- Entreprise : `rh@techsousse.tn`

### Tests

```bash
cd backend && mvn verify
```

Les tests d'intégration couvrent le parcours complet (inscription → offre → candidature → décision), les doublons, les droits par rôle, l'accès aux données d'une autre entreprise, l'authentification et la validation.

## Configuration

| Variable | Rôle |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Connexion à la base (H2 par défaut) |
| `JWT_SECRET` | Clé de signature en Base64 (à changer en production) |
| `JWT_EXPIRATION_MINUTES` | Durée de validité du jeton (120 par défaut) |
| `CORS_ORIGINS` | Origines autorisées pour le frontend |
| `SEED_DEMO_DATA` | Charge les données de démonstration (`true` par défaut) |

## Pistes d'amélioration

- Dépôt du CV en PDF avec chaque candidature.
- Notifications par email lors d'un changement de statut.
- Migrations de base avec Flyway.
- Recommandation d'offres selon les compétences de l'étudiant.

## Auteure

Mayssa Dhahri, élève-ingénieure en génie logiciel à l'École Polytechnique de Sousse.
[LinkedIn](https://www.linkedin.com/in/mayssa-dhahri-38b1373bb/)
