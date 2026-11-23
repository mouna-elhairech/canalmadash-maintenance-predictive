import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from "../environments/environment";
import {TypeMaintenance} from "../models/TypeMaintenance";

@Injectable({
  providedIn: 'root'
})
export class TypeMaintenanceService {

  private baseUrl = `${environment.apiUrl}/typeMaintenance`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<TypeMaintenance[]> {
    return this.http.get<TypeMaintenance[]>(this.baseUrl);
  }

  findActifs(): Observable<TypeMaintenance[]> {
    return this.http.get<TypeMaintenance[]>(`${this.baseUrl}/actifs`);
  }

  findById(id: number): Observable<TypeMaintenance> {
    return this.http.get<TypeMaintenance>(`${this.baseUrl}/${id}`);
  }

 create(type: TypeMaintenance): Observable<TypeMaintenance> {
    return this.http.post<TypeMaintenance>(this.baseUrl, type);
  }

  update(id: number, type: TypeMaintenance): Observable<TypeMaintenance> {
    return this.http.put<TypeMaintenance>(`${this.baseUrl}/${id}`, type);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  disable(id: number): Observable<TypeMaintenance> {
    return this.http.post<TypeMaintenance>(`${this.baseUrl}/${id}/disable`, null);
  }

  enable(id: number): Observable<TypeMaintenance> {
    return this.http.post<TypeMaintenance>(`${this.baseUrl}/${id}/enable`, null);
  }
}
