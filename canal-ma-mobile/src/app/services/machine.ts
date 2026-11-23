import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {Machine} from "../models/Machine";
import {Action} from "../models/Action";

@Injectable({
  providedIn: 'root'
})
export class MachineService {

  private baseUrl = `${environment.apiUrl}/machines`;

  constructor(private http: HttpClient) {}

  getMachines(): Observable<Machine[]> {
    return this.http.get<Machine[]>(this.baseUrl);
  }

  getMachine(id: number): Observable<Machine> {
    return this.http.get<Machine>(`${this.baseUrl}/${id}`);
  }

  getMachineImageUrl(machine: Machine): string {
    return `${this.baseUrl}/${machine.id}/image`;
  }
  changeEtat(machineId: number, typeActionId: number) {
    const params = new HttpParams().set('typeActionId', typeActionId);
    return this.http.post<Action>(
      `${this.baseUrl}/${machineId}/etat`,
      null,
      { params }
    );
  }

}
