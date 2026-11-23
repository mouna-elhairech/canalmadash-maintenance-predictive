import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from "../environments/environment";
import {TypeAction} from "../models/TypeAction";

@Injectable({
  providedIn: 'root'
})
export class TypeActionService {

  private baseUrl = `${environment.apiUrl}/typeAction`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<TypeAction[]> {
    return this.http.get<TypeAction[]>(this.baseUrl);
  }

  findActifs(): Observable<TypeAction[]> {
    return this.http.get<TypeAction[]>(`${this.baseUrl}/actifs`);
  }

  findById(id: number): Observable<TypeAction> {
    return this.http.get<TypeAction>(`${this.baseUrl}/${id}`);
  }

  create(type: TypeAction): Observable<TypeAction> {
    return this.http.post<TypeAction>(this.baseUrl, type);
  }

  update(id: number, type: TypeAction): Observable<TypeAction> {
    return this.http.put<TypeAction>(`${this.baseUrl}/${id}`, type);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  disable(id: number): Observable<TypeAction> {
    return this.http.post<TypeAction>(`${this.baseUrl}/${id}/disable`, null);
  }

  enable(id: number): Observable<TypeAction> {
    return this.http.post<TypeAction>(`${this.baseUrl}/${id}/enable`, null);
  }
}
