export interface PieceRechange {
  id?: number;
  reference: string;
  libelle: string;
  quantite: number;
  seuilMinimum: number;
  coutUnitaire?: number;
  valeurStock?: number;
  alerteStock?: boolean;
  standard?: boolean;
  machineIds?: number[];
}

export type TypeMouvementStockPiece = 'ENTREE' | 'SORTIE_INTERVENTION' | 'RETOUR_INTERVENTION' | 'AJUSTEMENT';

export interface MouvementStockPiece {
  id: number;
  piece: PieceRechange;
  type: TypeMouvementStockPiece;
  quantite: number;
  coutUnitaire: number;
  coutTotal: number;
  dateMouvement: string;
  commentaire?: string;
}

export type StatutDemandePiece = 'EN_ATTENTE' | 'COMMANDEE' | 'RECUE' | 'ANNULEE';

export interface DemandePieceRechange {
  id: number;
  piece: PieceRechange;
  quantiteDemandee: number;
  prixTotalReception?: number;
  motif: string;
  dateDemande: string;
  dateReception?: string;
  statut: StatutDemandePiece;
}

export interface EntreeStockPieceRequest {
  quantite: number;
  prixTotal: number;
  commentaire?: string;
}

export interface DemandePieceRequest {
  pieceId: number;
  quantiteDemandee: number;
  motif: string;
}
