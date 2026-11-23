export interface DashboardMaintenance {
  totalMachines: number;
  availabilityPercent: number;
  totalDowntimeMinutes: number;
  mtbfHours: number;
  mttrHours: number;
  overdueDemandesCount: number;
  upcomingDemandesCount: number;
  highPriorityDemandesCount: number;
  totalMaintenanceCost: number;
  longDowntimeMachinesCount: number;
  machinesByState: StateStat[];
  demandesByStatus: Record<string, number>;
  topProblemMachines: ProblemMachine[];
  overdueDemandes: DemandeSummary[];
  upcomingDemandes: DemandeSummary[];
  costByMachine: CostStat[];
  costByType: CostStat[];
  costByMonth: CostStat[];
  longDowntimeMachines: LongDowntimeMachine[];
  preventiveRecommendations: PreventiveRecommendation[];
  machineIndicators: MachineMaintenanceIndicators[];
  notifications: MaintenanceNotification[];
  calendarItems: CalendarItem[];
  criticalMachines: CriticalMachine[];
  causeStats: CostStat[];
  objectives: MaintenanceObjective[];
}

export interface StateStat {
  code: string;
  label: string;
  color: string;
  count: number;
}

export interface ProblemMachine {
  id: number;
  nom: string;
  ref: string;
  failureCount: number;
  downtimeMinutes: number;
}

export interface DemandeSummary {
  id: number;
  machineNom: string;
  machineRef: string;
  typeMaintenance: string;
  statut: string;
  priorite: string;
  description: string;
  dateDemande: string;
  dateSouhaitee: string;
}

export interface CostStat {
  label: string;
  cost: number;
}

export interface LongDowntimeMachine {
  machineId: number;
  nom: string;
  ref: string;
  etat: string;
  minutes: number;
}

export interface PreventiveRecommendation {
  machineId: number;
  nom: string;
  ref: string;
  reason: string;
  recommendation: string;
}

export interface MachineMaintenanceIndicators {
  machineId: number;
  nom: string;
  ref: string;
  actionCount: number;
  failureCount: number;
  uptimeMinutes: number;
  downtimeMinutes: number;
  availabilityPercent: number;
  mtbfHours: number;
  mttrHours: number;
  investmentCost: number;
  maintenanceCost: number;
  replacementRatioPercent: number;
  replacementRecommendation: string;
  replacementRecommendationLevel: string;
  availabilityRecommendation: string;
  availabilityRecommendationLevel: string;
  downtimeRecommendation: string;
  downtimeRecommendationLevel: string;
  failureRecommendation: string;
  failureRecommendationLevel: string;
  repairTimeRecommendation: string;
  repairTimeRecommendationLevel: string;
  seuilDisponibiliteAttentionPercent: number;
  seuilDisponibiliteCritiquePercent: number;
  seuilTempsArretAttentionMinutes: number;
  seuilTempsArretCritiqueMinutes: number;
  seuilPannesAttention: number;
  seuilPannesCritique: number;
  seuilTempsReparationAttentionHours: number;
  seuilTempsReparationCritiqueHours: number;
  seuilRemplacementSurveillancePercent: number;
  seuilRemplacementAttentionPercent: number;
  seuilRemplacementCritiquePercent: number;
}

export interface MaintenanceNotification {
  level: string;
  title: string;
  message: string;
  date: string;
}

export interface CalendarItem {
  demandeId: number;
  machineNom: string;
  typeMaintenance: string;
  statut: string;
  priorite: string;
  dateSouhaitee: string;
  retard: boolean;
}

export interface CriticalMachine {
  machineId: number;
  nom: string;
  ref: string;
  score: number;
  niveau: string;
  raison: string;
}

export interface MaintenanceObjective {
  label: string;
  target: number;
  value: number;
  unit: string;
  achieved: boolean;
}
