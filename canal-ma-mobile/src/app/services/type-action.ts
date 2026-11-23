import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {TypeAction} from "../models/TypeAction";
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TypeActionService {
  private baseUrl = `${environment.apiUrl}/typeAction`;

  constructor(private http: HttpClient) {}

  getActifs(): Observable<TypeAction[]> {
    return this.http.get<TypeAction[]>(`${this.baseUrl}/actifs`);
  }
}
