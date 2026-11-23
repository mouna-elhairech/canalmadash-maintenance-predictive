import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DashboardMaintenance, MachineMaintenanceIndicators } from '../models/DashboardMaintenance';

@Injectable({ providedIn: 'root' })
export class DashboardMaintenanceService {
  private baseUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getMaintenanceDashboard(): Observable<DashboardMaintenance> {
    return this.http.get<DashboardMaintenance>(`${this.baseUrl}/maintenance`);
  }

  getMachineIndicators(machineId: number): Observable<MachineMaintenanceIndicators> {
    return this.http.get<MachineMaintenanceIndicators>(`${this.baseUrl}/maintenance/machines/${machineId}`);
  }
}
