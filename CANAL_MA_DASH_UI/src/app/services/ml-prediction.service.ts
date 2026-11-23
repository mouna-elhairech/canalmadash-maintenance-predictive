import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface MlMetrics {
  f1: number;
  pr_auc: number;
  precision: number;
  recall: number;
  seuil: number;
}

export interface MlPrediction {
  machineId: number;
  nom?: string;
  probability: number;
  riskLevel: string;
  riskColor: string;
  recommendation: string;
  modelAccuracy: number;
  modelMetrics: MlMetrics;
  asOf: string;
  economics?: {
    cout_panne_moyen: number | null;
    nb_pannes: number;
    cout_visite_moyen: number | null;
    nb_visites: number;
    seuil_rentabilite_pct: number | null;
  };
  features: Record<string, number>;
  topFactors: Array<{ name: string; importance: number; value: number; contribution: number; effect: string }>;
  error?: string;
}

export interface MlAllPrediction {
  machine_id: number;
  nom: string;
  probability: number;
  risk_level: string;
  risk_color?: string;
  recommendation?: string;
  as_of?: string;
  model_metrics?: { seuil: number; pr_auc: number; precision: number; recall: number; f1: number };
  top_factors?: Array<{ name: string; importance: number; value: number; contribution: number; effect: string }>;
}

export interface MlHistory {
  machine_id: number;
  seuil: number;
  test_depuis: string;
  points: Array<{ d: string; s: number }>;
  pannes: string[];
}

export interface MlReport {
  machineId?: number;
  model?: string;
  score?: number;
  riskLevel?: string;
  report?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MlPredictionService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Prediction pour un equipement
  predictMachine(machineId: number): Observable<MlPrediction> {
    return this.http.get<MlPrediction>(`${this.apiUrl}/ml/predict/${machineId}`);
  }

  // Prediction pour tous les equipements
  predictAll(): Observable<MlAllPrediction[]> {
    return this.http.get<MlAllPrediction[]>(`${this.apiUrl}/ml/predict/all`);
  }

  // Score jour par jour sur la periode de test + dates des vraies pannes
  history(machineId: number): Observable<MlHistory> {
    return this.http.get<MlHistory>(`${this.apiUrl}/ml/history/${machineId}`);
  }

  // Rapport redige par Ollama a partir du score et des contributions
  report(machineId: number): Observable<MlReport> {
    return this.http.get<MlReport>(`${this.apiUrl}/ml/report/${machineId}`);
  }

  // Verifie que l'API Flask repond
  checkHealth(): Observable<any> {
    return this.http.get(`${this.apiUrl}/ml/health`);
  }
}