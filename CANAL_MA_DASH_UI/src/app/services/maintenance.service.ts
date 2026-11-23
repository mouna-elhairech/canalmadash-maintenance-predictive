import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { avecToken } from './url-token';

@Injectable({ providedIn: 'root' })
export class MaintenanceService {
  private baseUrl = `${environment.apiUrl}/maintenances`;

  constructor(private http: HttpClient) {}

  createIntervention(formData: FormData): Observable<any> {
    return this.http.post<any>(this.baseUrl, formData);
  }

  getByMachine(machineId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/machine/${machineId}`);
  }

  getImageUrl(maintenanceId: number): string {
    return avecToken(`${this.baseUrl}/${maintenanceId}/image`);
  }

  getBeforeImageUrl(maintenanceId: number): string {
    return avecToken(`${this.baseUrl}/${maintenanceId}/image-avant`);
  }

  getAfterImageUrl(maintenanceId: number): string {
    return avecToken(`${this.baseUrl}/${maintenanceId}/image-apres`);
  }

  getDemandeImageUrl(demandeId: number): string {
    return avecToken(`${this.baseUrl}/demandes/${demandeId}/image`);
  }

  retournerPiece(pieceUtiliseeId: number, payload: { quantite: number; cause: string }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/pieces/${pieceUtiliseeId}/retour`, payload);
  }
}