import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export type CategorieFonctionnalite = 'PAGE' | 'ACTION';

export interface FonctionnaliteApplication {
  id: number;
  code: string;
  libelle: string;
  description?: string;
  route?: string;
  categorie: CategorieFonctionnalite;
  actif: boolean;
}

export interface RoleApplication {
  id: number;
  code: string;
  libelle: string;
  objectif?: string;
  actif: boolean;
  fonctionnalites: FonctionnaliteApplication[];
}

export interface RoleApplicationRequest {
  code: string;
  libelle: string;
  objectif?: string;
  actif: boolean;
  fonctionnaliteIds: number[];
}

export interface FonctionnaliteApplicationRequest {
  code: string;
  libelle: string;
  description?: string;
  route?: string;
  categorie: CategorieFonctionnalite;
  actif: boolean;
}

@Injectable({ providedIn: 'root' })
export class RoleApplicationService {
  private baseUrl = `${environment.apiUrl}/roles-application`;

  constructor(private http: HttpClient) {}

  getRoles(): Observable<RoleApplication[]> {
    return this.http.get<RoleApplication[]>(this.baseUrl);
  }

  getFonctionnalites(): Observable<FonctionnaliteApplication[]> {
    return this.http.get<FonctionnaliteApplication[]>(`${this.baseUrl}/fonctionnalites`);
  }

  createRole(payload: RoleApplicationRequest): Observable<RoleApplication> {
    return this.http.post<RoleApplication>(this.baseUrl, payload);
  }

  updateRole(id: number, payload: RoleApplicationRequest): Observable<RoleApplication> {
    return this.http.put<RoleApplication>(`${this.baseUrl}/${id}`, payload);
  }

  createFonctionnalite(payload: FonctionnaliteApplicationRequest): Observable<FonctionnaliteApplication> {
    return this.http.post<FonctionnaliteApplication>(`${this.baseUrl}/fonctionnalites`, payload);
  }

  updateFonctionnalite(id: number, payload: FonctionnaliteApplicationRequest): Observable<FonctionnaliteApplication> {
    return this.http.put<FonctionnaliteApplication>(`${this.baseUrl}/fonctionnalites/${id}`, payload);
  }

  initialiser(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/initialiser`, {});
  }
}
