import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Machine } from '../models/machine.model';
import { TypeMaintenance } from '../models/TypeMaintenance';

export type TypeReglePreventive = 'CONTROLE_PERIODIQUE' | 'CHAQUE_X_JOURS' | 'CHAQUE_X_HEURES_MARCHE' | 'APRES_X_PANNES';
export type UniteControlePeriodique = 'JOUR' | 'SEMAINE' | 'MOIS' | 'ANNEE';

export interface RegleMaintenancePreventive {
  id: number;
  machine: Machine;
  typeMaintenance: TypeMaintenance;
  typeRegle: TypeReglePreventive;
  valeur: number;
  frequence?: number | null;
  unite?: UniteControlePeriodique | null;
  dateDebut?: string | null;
  prochaineDate?: string | null;
  actif: boolean;
  derniereGeneration?: string | null;
  description?: string | null;
}

export interface RegleMaintenancePreventiveCreate {
  machineId: number;
  typeMaintenanceId: number;
  typeRegle: TypeReglePreventive;
  valeur?: number;
  frequence?: number;
  unite?: UniteControlePeriodique;
  dateDebut?: string;
  description?: string;
}

@Injectable({ providedIn: 'root' })
export class ReglePreventiveService {
  private baseUrl = `${environment.apiUrl}/regles-preventives`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<RegleMaintenancePreventive[]> {
    return this.http.get<RegleMaintenancePreventive[]>(this.baseUrl);
  }

  create(payload: RegleMaintenancePreventiveCreate): Observable<RegleMaintenancePreventive> {
    return this.http.post<RegleMaintenancePreventive>(this.baseUrl, payload);
  }

  disable(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  generate(): Observable<{ generated: number }> {
    return this.http.post<{ generated: number }>(`${this.baseUrl}/generate`, {});
  }
}
