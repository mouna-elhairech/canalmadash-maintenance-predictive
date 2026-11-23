import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {TypeMaintenance} from "../models/TypeMaintenance";
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TypeMaintenanceService {
  private baseUrl = `${environment.apiUrl}/typeMaintenance`;

  constructor(private http: HttpClient) {}

  getActifs(): Observable<TypeMaintenance[]> {
    return this.http.get<TypeMaintenance[]>(`${this.baseUrl}/actifs`);
  }

  create(payload: { code: string; libelle: string; actif: boolean }): Observable<TypeMaintenance> {
    return this.http.post<TypeMaintenance>(this.baseUrl, payload);
  }
}
