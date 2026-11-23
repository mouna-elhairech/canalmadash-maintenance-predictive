import {TypeMaintenance} from "./TypeMaintenance";

export type StatutDemandeMaintenance =
  | 'EN_ATTENTE'
  | 'PLANIFIEE'
  | 'EN_COURS'
  | 'EN_ATTENTE_VALIDATION'
  | 'TERMINEE'
  | 'ANNULEE';

export type PrioriteDemandeMaintenance =
  | 'FAIBLE'
  | 'NORMALE'
  | 'URGENTE'
  | 'CRITIQUE';


export interface DemandeMaintenance {
  id: number;
  machine?: {
    id: number;
    nom?: string;
    ref?: string;
  } | null;
  typeMaintenance?: TypeMaintenance | null;
  description: string;
  dateDemande: string;
  dateSouhaitee?: string | null;
  dateDebutReelle?: string | null;
  dateFinReelle?: string | null;
  statut: StatutDemandeMaintenance;
  priorite?: PrioriteDemandeMaintenance;
  demandeur: string;
  imagePath?: string | null;
}
