import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MaintenanceService {
  private baseUrl = `${environment.apiUrl}/maintenances`;
  private externalUrl = `${environment.apiUrl}/interventions-externes`;
  private piecesUrl = `${environment.apiUrl}/pieces-rechange`;

  constructor(private http: HttpClient) {}

  createIntervention(formData: FormData): Observable<any> {
    return this.http.post<any>(this.baseUrl, formData);
  }

  getByMachine(machineId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/machine/${machineId}`);
  }

  retournerPiece(pieceUtiliseeId: number, payload: { quantite: number; cause: string }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/pieces/${pieceUtiliseeId}/retour`, payload);
  }

  createExternalDraft(payload: any): Observable<any> {
    return this.http.post<any>(this.externalUrl, payload);
  }

  getPiecesCompatibles(machineId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.piecesUrl}/machine/${machineId}/compatibles`);
  }
}
