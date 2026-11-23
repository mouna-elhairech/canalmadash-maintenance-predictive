import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Machine } from '../models/machine.model';
import { Action } from '../models/action.model';
import { environment } from '../environments/environment';
import { avecToken } from './url-token';

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

 

  getActionsForMachine(
    machineId: number,
    from?: string,
    to?: string
  ): Observable<Action[]> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to)   params = params.set('to', to);

    return this.http.get<Action[]>(
      `${this.baseUrl}/${machineId}/actions`,
      { params }
    );
  }



  getMachineImageUrl(machine: Machine): string {
    // L'URL garde le meme format; le backend renvoie 404 si aucune image n'existe.
    return avecToken(`${this.baseUrl}/${machine.id}/image`);
  }


  createMachine(formData: FormData): Observable<Machine> {
    return this.http.post<Machine>(`${this.baseUrl}/avec-image`, formData);
  }

  updateMachine(id: number, formData: FormData): Observable<Machine> {
    return this.http.put<Machine>(`${this.baseUrl}/${id}/avec-image`, formData);
  }


  deleteMachine(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }



  changeEtat(machineId: number, typeActionId: number): Observable<Action> {
    const params = new HttpParams().set('typeActionId', typeActionId);
    return this.http.post<Action>(`${this.baseUrl}/${machineId}/etat`, null, { params });
  }
}