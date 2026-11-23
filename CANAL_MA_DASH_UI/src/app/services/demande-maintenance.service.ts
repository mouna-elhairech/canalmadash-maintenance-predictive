import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DemandeMaintenance } from '../models/demande-maintenance.model';
import { environment } from '../environments/environment';

export type StatutDemandeMaintenance = 'EN_ATTENTE' | 'PLANIFIEE' | 'EN_COURS' | 'EN_ATTENTE_VALIDATION' | 'TERMINEE' | 'ANNULEE';
export type PrioriteDemandeMaintenance = 'FAIBLE' | 'NORMALE' | 'URGENTE' | 'CRITIQUE';

export interface DemandeMaintenanceFilters {
  machineId?: number;
  statut?: StatutDemandeMaintenance;
  priorite?: PrioriteDemandeMaintenance;
  typeMaintenanceId?: number;
  from?: string;
  to?: string;
  retard?: boolean;
  coutMin?: number;
}

@Injectable({
  providedIn: 'root'
})
export class DemandeMaintenanceService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createDemande(formData: FormData): Observable<DemandeMaintenance> {
    return this.http.post<DemandeMaintenance>(`${this.apiUrl}/demandes-maintenance`, formData);
  }

  getByMachine(
    machineId: number,
    from?: string,
    to?: string
  ): Observable<DemandeMaintenance[]> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);

    return this.http.get<DemandeMaintenance[]>(
      `${this.apiUrl}/demandes-maintenance/machine/${machineId}`,
      { params }
    );
  }

  updateStatut(
    id: number,
    statut: StatutDemandeMaintenance,
    dateSouhaitee?: string
  ): Observable<DemandeMaintenance> {
    let params = new HttpParams().set('statut', statut);
    if (dateSouhaitee) {
      params = params.set('dateSouhaitee', dateSouhaitee);
    }
    return this.http.patch<DemandeMaintenance>(`${this.apiUrl}/demandes-maintenance/${id}/statut`, null, { params });
  }

  validerIntervention(id: number): Observable<DemandeMaintenance> {
    return this.http.post<DemandeMaintenance>(`${this.apiUrl}/demandes-maintenance/${id}/validation`, null);
  }

  refuserIntervention(id: number): Observable<DemandeMaintenance> {
    return this.http.post<DemandeMaintenance>(`${this.apiUrl}/demandes-maintenance/${id}/refus-validation`, null);
  }

  search(filters: DemandeMaintenanceFilters): Observable<DemandeMaintenance[]> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });
    return this.http.get<DemandeMaintenance[]>(`${this.apiUrl}/demandes-maintenance/search`, { params });
  }
}
