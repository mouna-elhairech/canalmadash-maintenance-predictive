import {TypeAction} from "./TypeAction";

export interface Machine {
  id: number;
  nom: string;
  ref: string;
  typeAction: TypeAction | null;
  dateDebutAction: string | null;
  imagePath?: string | null;
  emplacement?: string | null;
  fabricant?: string | null;
  modele?: string | null;
  numeroSerie?: string | null;
  dateInstallation?: string | null;
  criticite?: string | null;
  heuresMarche?: number | null;
  coutAchat?: number | null;
  coutInstallation?: number | null;
  coutTransport?: number | null;
  coutFormation?: number | null;
  coutMiseEnService?: number | null;
  coutTotalInvestissement?: number | null;
  seuilDisponibiliteAttentionPercent?: number | null;
  seuilDisponibiliteCritiquePercent?: number | null;
  seuilTempsArretAttentionMinutes?: number | null;
  seuilTempsArretCritiqueMinutes?: number | null;
  seuilPannesAttention?: number | null;
  seuilPannesCritique?: number | null;
  seuilTempsReparationAttentionHours?: number | null;
  seuilTempsReparationCritiqueHours?: number | null;
  seuilRemplacementSurveillancePercent?: number | null;
  seuilRemplacementAttentionPercent?: number | null;
  seuilRemplacementCritiquePercent?: number | null;
  commentaireTechnique?: string | null;
}
