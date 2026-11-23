import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface AiDiagnosticResponse {
  provider: string;
  model?: string;
  machineId: number;
  machineNom?: string;
  status: string;
  diagnostic: string;
  sections: AiResponseSection[];
  generatedAt: string;
}

export interface AiResponseSection {
  title: string;
  content: string;
}

export interface AiChatRequest {
  machineId?: number;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class AiMaintenanceService {
  private baseUrl = `${environment.apiUrl}/ai`;

  constructor(private http: HttpClient) {}

  testModel(): Observable<AiDiagnosticResponse> {
    return this.http.get<AiDiagnosticResponse>(`${this.baseUrl}/test`);
  }

  diagnoseMachine(machineId: number): Observable<AiDiagnosticResponse> {
    return this.http.get<AiDiagnosticResponse>(`${this.baseUrl}/machines/${machineId}/diagnostic`);
  }

  chat(request: AiChatRequest): Observable<AiDiagnosticResponse> {
    return this.http.post<AiDiagnosticResponse>(`${this.baseUrl}/chat`, request);
  }
}
