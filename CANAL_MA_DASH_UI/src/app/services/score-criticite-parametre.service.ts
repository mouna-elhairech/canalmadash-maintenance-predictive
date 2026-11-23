import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface ScoreCriticiteParametre {
  id?: number;
  poidsCoutMaintenance: number;
  poidsNombrePannes: number;
  poidsTempsArret: number;
  poidsDisponibilite: number;
  poidsTempsReparation: number;
  facteurSurveillance: number;
  facteurAttention: number;
  facteurCritique: number;
  seuilNormalMax: number;
  seuilSurveillanceMax: number;
}

@Injectable({ providedIn: 'root' })
export class ScoreCriticiteParametreService {
  private baseUrl = `${environment.apiUrl}/score-criticite-parametres`;

  constructor(private http: HttpClient) {}

  get(): Observable<ScoreCriticiteParametre> {
    return this.http.get<ScoreCriticiteParametre>(this.baseUrl);
  }

  update(payload: ScoreCriticiteParametre): Observable<ScoreCriticiteParametre> {
    return this.http.put<ScoreCriticiteParametre>(this.baseUrl, payload);
  }

  reset(): Observable<ScoreCriticiteParametre> {
    return this.http.post<ScoreCriticiteParametre>(`${this.baseUrl}/reset`, {});
  }
}
