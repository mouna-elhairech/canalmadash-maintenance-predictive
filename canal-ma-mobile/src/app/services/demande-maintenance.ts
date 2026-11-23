import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type StatutDemandeMaintenance = 'EN_ATTENTE' | 'PLANIFIEE' | 'EN_COURS' | 'EN_ATTENTE_VALIDATION' | 'TERMINEE' | 'ANNULEE';
export type PrioriteDemandeMaintenance = 'FAIBLE' | 'NORMALE' | 'URGENTE' | 'CRITIQUE';

export interface DemandeMaintenance {
  id: number;
  typeMaintenance?: {
    id: number;
    code?: string;
    libelle?: string;
    color?: string;
  } | null;
  description: string;
  dateDemande: string;
  dateSouhaitee?: string | null;
  dateDebutReelle?: string | null;
  dateFinReelle?: string | null;
  statut: StatutDemandeMaintenance;
  priorite?: PrioriteDemandeMaintenance;
  demandeur?: string;
}

export interface DemandeMaintenanceCreate {
  machineId: number;
  typeMaintenanceId: number;
  description: string;
  demandeur: string;
}

@Injectable({ providedIn: 'root' })
export class DemandeMaintenanceService {
  private baseUrl = `${environment.apiUrl}/demandes-maintenance`;

  constructor(private http: HttpClient) {}

  createDemande(formData: FormData): Observable<any> {
    return this.http.post<any>(this.baseUrl, formData);
  }

  getByMachine(machineId: number): Observable<DemandeMaintenance[]> {
    return this.http.get<DemandeMaintenance[]>(`${this.baseUrl}/machine/${machineId}`);
  }

  updateStatut(id: number, statut: StatutDemandeMaintenance, dateSouhaitee?: string): Observable<DemandeMaintenance> {
    let params = new HttpParams().set('statut', statut);
    if (dateSouhaitee) {
      params = params.set('dateSouhaitee', dateSouhaitee);
    }
    return this.http.patch<any>(`${this.baseUrl}/${id}/statut`, null, { params });
  }

  validerIntervention(id: number): Observable<DemandeMaintenance> {
    return this.http.post<DemandeMaintenance>(`${this.baseUrl}/${id}/validation`, null);
  }

  refuserIntervention(id: number): Observable<DemandeMaintenance> {
    return this.http.post<DemandeMaintenance>(`${this.baseUrl}/${id}/refus-validation`, null);
  }
}
