import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';

export interface AuthUser {
  token: string;
  nom: string;
  email: string;
  role: string;
  fonctionnalites: string[];
}

const STORAGE_KEY = 'canalmadash_auth';

// Adresse -> fonctionnalite requise (la premiere regle qui correspond s'applique).
const REGLES_ROUTES: { motif: RegExp; code: string }[] = [
  { motif: /^\/machines\/new/, code: 'MACHINE_CREER_MODIFIER_SUPPRIMER' },
  { motif: /^\/machines\/[^/]+\/edit/, code: 'MACHINE_CREER_MODIFIER_SUPPRIMER' },
  { motif: /^\/machinesD/, code: 'PAGE_DETAIL_MACHINE' },
  { motif: /^\/machines/, code: 'PAGE_MACHINES' },
  { motif: /^\/dashboard/, code: 'PAGE_DASHBOARD' },
  { motif: /^\/maintenance\/kanban/, code: 'PAGE_KANBAN' },
  { motif: /^\/maintenance\/pieces/, code: 'PAGE_PIECES' },
  { motif: /^\/parametres\/types/, code: 'PAGE_PARAM_TYPES' },
  { motif: /^\/parametres\/score/, code: 'PAGE_PARAM_SCORE' },
  { motif: /^\/parametres\/roles/, code: 'PAGE_PARAM_ROLES' },
  { motif: /^\/aide/, code: 'PAGE_AIDE' }
];

// Ordre de recherche de la page d'accueil apres le login.
const PAGES_ACCUEIL = [
  '/dashboard', '/machines', '/maintenance/kanban', '/maintenance/pieces',
  '/parametres/types', '/parametres/score', '/parametres/roles', '/aide'
];

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private http: HttpClient) {}

  login(email: string, motDePasse: string): Observable<AuthUser> {
    return this.http
      .post<AuthUser>(`${environment.apiUrl}/auth/login`, { email, motDePasse })
      .pipe(tap(user => sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user))));
  }

  logout(): void {
    const token = this.token;
    sessionStorage.removeItem(STORAGE_KEY);
    if (token) {
      this.http
        .post(`${environment.apiUrl}/auth/logout`, {}, {
          headers: new HttpHeaders({ Authorization: `Bearer ${token}` })
        })
        .subscribe({ error: () => {} });
    }
  }

  get user(): AuthUser | null {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  }

  get token(): string | null {
    return this.user?.token ?? null;
  }

  // Une session ouverte avant l'ajout des fonctionnalites est consideree comme expiree.
  get isLoggedIn(): boolean {
    return !!this.token && Array.isArray(this.user?.fonctionnalites);
  }

  get fonctionnalites(): string[] {
    return this.user?.fonctionnalites ?? [];
  }

  peutFaire(code: string): boolean {
    return this.fonctionnalites.includes(code);
  }

  peutVoirRoute(url: string): boolean {
    const chemin = url.split('?')[0];
    const regle = REGLES_ROUTES.find(r => r.motif.test(chemin));
    return regle ? this.peutFaire(regle.code) : true;
  }

  pageAccueil(): string {
    return PAGES_ACCUEIL.find(page => this.peutVoirRoute(page)) ?? '/login';
  }
}