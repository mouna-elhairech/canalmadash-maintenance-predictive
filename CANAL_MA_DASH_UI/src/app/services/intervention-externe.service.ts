import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface DemandeInterventionExterne {
  id: number;
  fournisseurNom: string;
  fournisseurContact: string;
  fournisseurTelephone: string;
  fournisseurEmail: string;
  fournisseurAdresse: string;
  objet: string;
  description: string;
  statut: string;
  dateCreation: string;
  dateSouhaitee?: string;
}

export interface DemandeInterventionExterneRequest {
  machineId: number;
  demandeMaintenanceId?: number;
  fournisseurNom: string;
  fournisseurContact: string;
  fournisseurTelephone: string;
  fournisseurEmail: string;
  fournisseurAdresse: string;
  objet: string;
  description: string;
  dateSouhaitee?: string;
}

@Injectable({ providedIn: 'root' })
export class InterventionExterneService {
  private baseUrl = `${environment.apiUrl}/interventions-externes`;

  constructor(private http: HttpClient) {}

  findByMachine(machineId: number): Observable<DemandeInterventionExterne[]> {
    return this.http.get<DemandeInterventionExterne[]>(`${this.baseUrl}/machine/${machineId}`);
  }

  create(request: DemandeInterventionExterneRequest): Observable<DemandeInterventionExterne> {
    return this.http.post<DemandeInterventionExterne>(this.baseUrl, request);
  }
}
