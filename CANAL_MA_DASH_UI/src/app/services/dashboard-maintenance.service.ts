import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardMaintenance, MachineMaintenanceIndicators } from '../models/dashboard-maintenance.model';
import { environment } from '../environments/environment';
import { avecToken } from './url-token';

@Injectable({
  providedIn: 'root'
})
export class DashboardMaintenanceService {

  private baseUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getMaintenanceDashboard(): Observable<DashboardMaintenance> {
    return this.http.get<DashboardMaintenance>(`${this.baseUrl}/maintenance`);
  }

  getMachineMaintenanceIndicators(machineId: number): Observable<MachineMaintenanceIndicators> {
    return this.http.get<MachineMaintenanceIndicators>(`${this.baseUrl}/maintenance/machines/${machineId}`);
  }

  getDashboardExportUrl(): string {
    return avecToken(`${this.baseUrl}/maintenance/export`);
  }

  getOverdueExportUrl(): string {
    return avecToken(`${this.baseUrl}/maintenance/retards/export`);
  }

  getMachineHistoryExportUrl(machineId: number): string {
    return avecToken(`${this.baseUrl}/maintenance/machines/${machineId}/historique/export`);
  }

  getDashboardReportUrl(): string {
    return avecToken(`${this.baseUrl}/maintenance/report`);
  }

  getOverdueReportUrl(): string {
    return avecToken(`${this.baseUrl}/maintenance/retards/report`);
  }

  getMachineHistoryReportUrl(machineId: number): string {
    return avecToken(`${this.baseUrl}/maintenance/machines/${machineId}/historique/report`);
  }

  getMonthlyReportUrl(year: number, month: number): string {
    return avecToken(`${this.baseUrl}/maintenance/monthly-report?year=${year}&month=${month}`);
  }
}