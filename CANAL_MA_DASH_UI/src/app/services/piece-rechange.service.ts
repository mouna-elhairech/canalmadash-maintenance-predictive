import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import {
  DemandePieceRechange,
  DemandePieceRequest,
  EntreeStockPieceRequest,
  MouvementStockPiece,
  PieceRechange,
  StatutDemandePiece
} from '../models/piece-rechange.model';

@Injectable({ providedIn: 'root' })
export class PieceRechangeService {
  private baseUrl = `${environment.apiUrl}/pieces-rechange`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<PieceRechange[]> {
    return this.http.get<PieceRechange[]>(this.baseUrl);
  }

  findAlertes(): Observable<PieceRechange[]> {
    return this.http.get<PieceRechange[]>(`${this.baseUrl}/alertes`);
  }

  findCompatiblesForMachine(machineId: number): Observable<PieceRechange[]> {
    return this.http.get<PieceRechange[]>(`${this.baseUrl}/machine/${machineId}/compatibles`);
  }

  create(piece: PieceRechange): Observable<PieceRechange> {
    return this.http.post<PieceRechange>(this.baseUrl, piece);
  }

  update(id: number, piece: PieceRechange): Observable<PieceRechange> {
    return this.http.put<PieceRechange>(`${this.baseUrl}/${id}`, piece);
  }

  ajouterStock(id: number, request: EntreeStockPieceRequest): Observable<PieceRechange> {
    return this.http.post<PieceRechange>(`${this.baseUrl}/${id}/entrees-stock`, request);
  }

  findMouvements(id: number): Observable<MouvementStockPiece[]> {
    return this.http.get<MouvementStockPiece[]>(`${this.baseUrl}/${id}/mouvements`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  findDemandes(): Observable<DemandePieceRechange[]> {
    return this.http.get<DemandePieceRechange[]>(`${this.baseUrl}/demandes`);
  }

  createDemande(request: DemandePieceRequest): Observable<DemandePieceRechange> {
    return this.http.post<DemandePieceRechange>(`${this.baseUrl}/demandes`, request);
  }

  updateDemandeStatut(id: number, statut: StatutDemandePiece, prixTotalReception?: number): Observable<DemandePieceRechange> {
    const priceParam = prixTotalReception == null ? '' : `&prixTotalReception=${prixTotalReception}`;
    return this.http.post<DemandePieceRechange>(`${this.baseUrl}/demandes/${id}/statut?statut=${statut}${priceParam}`, null);
  }
}
