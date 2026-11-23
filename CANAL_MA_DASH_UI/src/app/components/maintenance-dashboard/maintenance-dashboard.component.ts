import { Component, OnInit } from '@angular/core';
import {
  CalendarItem,
  DashboardMaintenance,
  MachineMaintenanceIndicators
} from '../../models/dashboard-maintenance.model';
import { DashboardMaintenanceService } from '../../services/dashboard-maintenance.service';
import { RegleMaintenancePreventive, ReglePreventiveService } from '../../services/regle-preventive.service';
import { MlPredictionService, MlAllPrediction } from '../../services/ml-prediction.service';
import { factorLabel } from '../../services/ml-labels';
@Component({
  selector: 'app-maintenance-dashboard',
  templateUrl: './maintenance-dashboard.component.html',
  styleUrls: ['./maintenance-dashboard.component.css']
})
export class MaintenanceDashboardComponent implements OnInit {

  dashboard?: DashboardMaintenance;
  reglesPeriodiques: RegleMaintenancePreventive[] = [];
  loading = false;
  errorMessage = '';
  monthlyReportMonth = new Date().toISOString().substring(0, 7);
  calendarPopupOpen = false;
  calendarMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
  showPlannedDemandes = true;
  showPeriodicControls = true;
    mlRanking: MlAllPrediction[] = [];
  mlRankingLoading = false;
  mlRankingError = '';

  readonly statusLabels: Record<string, string> = {
    EN_ATTENTE: 'En attente',
    PLANIFIEE: 'Planifiee',
    EN_COURS: 'En cours',
    EN_ATTENTE_VALIDATION: 'En attente validation',
    TERMINEE: 'Terminee',
    ANNULEE: 'Annulee'
  };

  constructor(
    private dashboardService: DashboardMaintenanceService,
    private regleService: ReglePreventiveService,
    private mlService: MlPredictionService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
    this.loadMlRanking();
  }
    loadMlRanking(): void {
    this.mlRankingLoading = true;
    this.mlRankingError = '';
    this.mlService.predictAll().subscribe({
      next: r => {
        const data: any = r;
        this.mlRankingLoading = false;
        if (Array.isArray(data)) {
          this.mlRanking = data;
        } else {
          this.mlRankingError = 'Prédictions ML indisponibles. Vérifie que Flask est démarré (python3 flask_api.py).';
        }
      },
      error: err => {
        console.error(err);
        this.mlRankingLoading = false;
        this.mlRankingError = 'Prédictions ML indisponibles. Vérifie que Flask est démarré (python3 flask_api.py).';
      }
    });
  }

  mlSeuilPct(): number {
    const m = this.mlRanking.length ? this.mlRanking[0].model_metrics : undefined;
    return m ? m.seuil * 100 : 38;
  }

  mainRiskFactor(item: MlAllPrediction): string {
    const f = (item.top_factors || []).find(x => x.effect === 'hausse');
    return f ? factorLabel(f.name) : 'Aucun facteur en hausse';
  }

  mlLevelClass(level: string): string {
    switch (level) {
      case 'CRITIQUE': return 'bg-red-50 text-red-700 border border-red-200';
      case 'ATTENTION': return 'bg-amber-50 text-amber-700 border border-amber-200';
      default: return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
    }
  }

  mlBarColor(level: string): string {
    switch (level) {
      case 'CRITIQUE': return '#dc2626';
      case 'ATTENTION': return '#d97706';
      default: return '#16a34a';
    }
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    this.dashboardService.getMaintenanceDashboard().subscribe({
      next: dashboard => {
        this.dashboard = dashboard;
        this.loading = false;
        this.loadReglesPeriodiques();
      },
      error: err => {
        console.error(err);
        this.errorMessage = 'Impossible de charger les indicateurs maintenance.';
        this.loading = false;
      }
    });
  }

  loadReglesPeriodiques(): void {
    this.regleService.getAll().subscribe({
      next: regles => this.reglesPeriodiques = regles.filter(regle => !!this.getRuleCalendarDate(regle)),
      error: err => console.error(err)
    });
  }

  getStatusEntries(): { label: string; count: number }[] {
    if (!this.dashboard) {
      return [];
    }

    return Object.entries(this.dashboard.demandesByStatus)
      .map(([key, count]) => ({
        label: this.statusLabels[key] ?? key,
        count
      }));
  }

  formatDuration(minutes: number): string {
    if (!minutes || minutes <= 0) {
      return '0 min';
    }

    const days = Math.floor(minutes / 1440);
    const hours = Math.floor((minutes % 1440) / 60);
    const mins = Math.floor(minutes % 60);

    if (days > 0) {
      return `${days} j ${hours} h`;
    }
    if (hours > 0) {
      return `${hours} h ${mins} min`;
    }
    return `${mins} min`;
  }

  formatMoney(value?: number): string {
    return `${(value || 0).toFixed(2)} DH`;
  }

  getPriorityLabel(priority?: string): string {
    switch (priority) {
      case 'FAIBLE': return 'Faible';
      case 'NORMALE': return 'Normale';
      case 'URGENTE': return 'Urgente';
      case 'CRITIQUE': return 'Critique';
      default: return priority || 'Normale';
    }
  }

  openDashboardExport(): void {
    window.open(this.dashboardService.getDashboardExportUrl(), '_blank');
  }

  openOverdueExport(): void {
    window.open(this.dashboardService.getOverdueExportUrl(), '_blank');
  }

  openDashboardReport(): void {
    window.open(this.dashboardService.getDashboardReportUrl(), '_blank');
  }

  openOverdueReport(): void {
    window.open(this.dashboardService.getOverdueReportUrl(), '_blank');
  }

  openMonthlyReport(): void {
    if (!this.monthlyReportMonth) return;
    const [year, month] = this.monthlyReportMonth.split('-').map(Number);
    window.open(this.dashboardService.getMonthlyReportUrl(year, month), '_blank');
  }

  openCalendarPopup(): void {
    this.calendarPopupOpen = true;
  }

  closeCalendarPopup(): void {
    this.calendarPopupOpen = false;
  }

  previousCalendarMonth(): void {
    this.calendarMonth = new Date(this.calendarMonth.getFullYear(), this.calendarMonth.getMonth() - 1, 1);
  }

  nextCalendarMonth(): void {
    this.calendarMonth = new Date(this.calendarMonth.getFullYear(), this.calendarMonth.getMonth() + 1, 1);
  }

  goToCurrentCalendarMonth(): void {
    const now = new Date();
    this.calendarMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  }

  getCalendarMonthLabel(): string {
    return this.calendarMonth.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  }

  getAllCalendarItems(): Array<CalendarItem & { source: 'DEMANDE' | 'CONTROLE'; title: string; date: string; description?: string }> {
    const demandes = this.showPlannedDemandes ? (this.dashboard?.calendarItems || []).map(item => ({
      ...item,
      source: 'DEMANDE' as const,
      title: item.typeMaintenance || 'Demande maintenance',
      date: item.dateSouhaitee,
      description: item.statut
    })) : [];

    const controles = this.showPeriodicControls ? this.reglesPeriodiques
      .filter(regle => !!this.getRuleCalendarDate(regle))
      .map(regle => ({
        demandeId: 0,
        machineNom: regle.machine?.nom || 'Machine',
        typeMaintenance: regle.typeMaintenance?.libelle || regle.typeMaintenance?.code || 'Controle periodique',
        statut: 'CONTROLE_PERIODIQUE',
        priorite: 'NORMALE',
        dateSouhaitee: this.getRuleCalendarDate(regle)!,
        retard: new Date(this.getRuleCalendarDate(regle)!).getTime() < Date.now(),
        source: 'CONTROLE' as const,
        title: regle.typeMaintenance?.libelle || 'Controle periodique',
        date: this.getRuleCalendarDate(regle)!,
        description: this.ruleValueLabel(regle)
      })) : [];

    return [...demandes, ...controles]
      .filter(item => !!item.date)
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  }

  getRuleCalendarDate(rule: RegleMaintenancePreventive): string | null {
    return rule.prochaineDate || rule.dateDebut || null;
  }

  getPreviewCalendarItems(): Array<CalendarItem & { source: 'DEMANDE' | 'CONTROLE'; title: string; date: string; description?: string }> {
    return this.getAllCalendarItems().slice(0, 7);
  }

  getCalendarDays(): Array<{ date: Date; inMonth: boolean; today: boolean; items: Array<any> }> {
    const year = this.calendarMonth.getFullYear();
    const month = this.calendarMonth.getMonth();
    const first = new Date(year, month, 1);
    const start = new Date(first);
    const mondayBasedDay = (first.getDay() + 6) % 7;
    start.setDate(first.getDate() - mondayBasedDay);

    const today = new Date();
    const days = [];
    for (let i = 0; i < 42; i++) {
      const date = new Date(start);
      date.setDate(start.getDate() + i);
      days.push({
        date,
        inMonth: date.getMonth() === month,
        today: this.isSameDay(date, today),
        items: this.getItemsForDay(date)
      });
    }
    return days;
  }

  getItemsForDay(date: Date): Array<any> {
    return this.getAllCalendarItems().filter(item => this.isSameDay(new Date(item.date), date));
  }

  isSameDay(a: Date, b: Date): boolean {
    return a.getFullYear() === b.getFullYear()
      && a.getMonth() === b.getMonth()
      && a.getDate() === b.getDate();
  }

  ruleValueLabel(rule: RegleMaintenancePreventive): string {
    return `Chaque ${rule.frequence || 1} ${this.unitLabel(rule.unite || 'JOUR')}`;
  }

  unitLabel(unit: string): string {
    switch (unit) {
      case 'JOUR': return 'jour(s)';
      case 'SEMAINE': return 'semaine(s)';
      case 'MOIS': return 'mois';
      case 'ANNEE': return 'annee(s)';
      default: return unit.toLowerCase();
    }
  }

  getGlobalAverageBetweenFailuresLabel(): string {
    if (!this.dashboard || this.dashboard.mtbfHours <= 0) {
      return 'Non calculable';
    }
    return `${this.dashboard.mtbfHours.toFixed(1)} h`;
  }

  getGlobalAverageRepairTimeLabel(): string {
    if (!this.dashboard || this.dashboard.mttrHours <= 0) {
      return 'Non calculable';
    }
    return `${this.dashboard.mttrHours.toFixed(1)} h`;
  }

  getAverageBetweenFailuresLabel(machine: MachineMaintenanceIndicators): string {
    if (machine.failureCount <= 0 || machine.mtbfHours <= 0) {
      return 'Non calculable';
    }
    return `${machine.mtbfHours.toFixed(1)} h`;
  }

  getAverageRepairTimeLabel(machine: MachineMaintenanceIndicators): string {
    if (machine.failureCount <= 0 || machine.mttrHours <= 0) {
      return 'Non calculable';
    }
    return `${machine.mttrHours.toFixed(1)} h`;
  }
}
