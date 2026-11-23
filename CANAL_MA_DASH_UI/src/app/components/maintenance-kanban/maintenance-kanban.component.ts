import { Component, OnInit } from '@angular/core';
import { DemandeMaintenance } from '../../models/demande-maintenance.model';
import {
  DemandeMaintenanceFilters,
  DemandeMaintenanceService,
  PrioriteDemandeMaintenance,
  StatutDemandeMaintenance
} from '../../services/demande-maintenance.service';
import { MachineService } from '../../services/machine.service';
import { TypeMaintenanceService } from '../../services/type-maintenance.service';
import { Machine } from '../../models/machine.model';
import { TypeMaintenance } from '../../models/TypeMaintenance';
import {
  RegleMaintenancePreventive,
  ReglePreventiveService,
  UniteControlePeriodique
} from '../../services/regle-preventive.service';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-maintenance-kanban',
  templateUrl: './maintenance-kanban.component.html'
})
export class MaintenanceKanbanComponent implements OnInit {
  demandes: DemandeMaintenance[] = [];
  machines: Machine[] = [];
  typeMaintenances: TypeMaintenance[] = [];
  regles: RegleMaintenancePreventive[] = [];
  loading = false;
  generatedMessage = '';
  draggingDemande?: DemandeMaintenance;
  dropTargetStatus?: StatutDemandeMaintenance;
  updatingDemandeId?: number;

  statuses: StatutDemandeMaintenance[] = ['EN_ATTENTE', 'PLANIFIEE', 'EN_COURS', 'EN_ATTENTE_VALIDATION', 'TERMINEE', 'ANNULEE'];
  priorities: PrioriteDemandeMaintenance[] = ['FAIBLE', 'NORMALE', 'URGENTE', 'CRITIQUE'];
  periodicUnits: UniteControlePeriodique[] = ['JOUR', 'SEMAINE', 'MOIS', 'ANNEE'];

  filters: DemandeMaintenanceFilters = {};
  newRule = {
    machineId: undefined as number | undefined,
    typeMaintenanceId: undefined as number | undefined,
    frequence: 1,
    unite: 'MOIS' as UniteControlePeriodique,
    dateDebut: new Date().toISOString().substring(0, 10),
    description: ''
  };

  constructor(
    public auth: AuthService,
    private demandeService: DemandeMaintenanceService,
    private machineService: MachineService,
    private typeMaintenanceService: TypeMaintenanceService,
    private regleService: ReglePreventiveService
  ) {}

  ngOnInit(): void {
    this.loadOptions();
    this.search();
    this.loadRegles();
  }

  loadOptions(): void {
    this.machineService.getMachines().subscribe(machines => this.machines = machines);
    this.typeMaintenanceService.findActifs().subscribe(types => this.typeMaintenances = types);
  }

  search(): void {
    this.loading = true;
    const filters = { ...this.filters };
    if (filters.from) filters.from = `${filters.from}T00:00:00`;
    if (filters.to) filters.to = `${filters.to}T23:59:59`;
    this.demandeService.search(filters).subscribe({
      next: demandes => {
        this.demandes = demandes;
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  resetFilters(): void {
    this.filters = {};
    this.search();
  }

  byStatus(status: StatutDemandeMaintenance): DemandeMaintenance[] {
    return this.demandes.filter(d => d.statut === status);
  }

  updateStatus(demande: DemandeMaintenance, statut: StatutDemandeMaintenance): void {
    if (demande.statut === statut || this.updatingDemandeId === demande.id) {
      return;
    }
    this.updatingDemandeId = demande.id;
    this.demandeService.updateStatut(demande.id, statut).subscribe(updated => {
      this.demandes = this.demandes.map(item => item.id === updated.id ? updated : item);
      this.updatingDemandeId = undefined;
    }, err => {
      console.error(err);
      this.updatingDemandeId = undefined;
    });
  }

  onDragStart(event: DragEvent, demande: DemandeMaintenance): void {
    this.draggingDemande = demande;
    event.dataTransfer?.setData('text/plain', String(demande.id));
    event.dataTransfer?.setDragImage((event.currentTarget as HTMLElement), 20, 20);
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
    }
  }

  onDragEnd(): void {
    this.draggingDemande = undefined;
    this.dropTargetStatus = undefined;
  }

  onDragOver(event: DragEvent, status: StatutDemandeMaintenance): void {
    if (!this.draggingDemande || this.draggingDemande.statut === status) {
      return;
    }
    event.preventDefault();
    this.dropTargetStatus = status;
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
  }

  onDragLeave(status: StatutDemandeMaintenance): void {
    if (this.dropTargetStatus === status) {
      this.dropTargetStatus = undefined;
    }
  }

  onDrop(event: DragEvent, status: StatutDemandeMaintenance): void {
    event.preventDefault();
    const id = Number(event.dataTransfer?.getData('text/plain'));
    const demande = this.draggingDemande || this.demandes.find(item => item.id === id);
    this.dropTargetStatus = undefined;
    this.draggingDemande = undefined;
    if (!demande) {
      return;
    }
    this.updateStatus(demande, status);
  }

  isDropTarget(status: StatutDemandeMaintenance): boolean {
    return this.dropTargetStatus === status;
  }

  isDragging(demande: DemandeMaintenance): boolean {
    return this.draggingDemande?.id === demande.id;
  }

  isLate(demande: DemandeMaintenance): boolean {
    return !!demande.dateSouhaitee
      && new Date(demande.dateSouhaitee).getTime() < Date.now()
      && demande.statut !== 'TERMINEE'
      && demande.statut !== 'ANNULEE';
  }

  loadRegles(): void {
    this.regleService.getAll().subscribe(regles => this.regles = regles);
  }

  createRule(): void {
    if (!this.newRule.machineId || !this.newRule.typeMaintenanceId) return;
    if (!this.newRule.frequence || !this.newRule.unite || !this.newRule.dateDebut) return;
    this.regleService.create({
      machineId: this.newRule.machineId,
      typeMaintenanceId: this.newRule.typeMaintenanceId,
      typeRegle: 'CONTROLE_PERIODIQUE',
      frequence: this.newRule.frequence,
      unite: this.newRule.unite,
      dateDebut: `${this.newRule.dateDebut}T00:00:00`,
      description: this.newRule.description
    }).subscribe(() => {
      this.newRule.description = '';
      this.loadRegles();
    });
  }

  disableRule(id: number): void {
    this.regleService.disable(id).subscribe(() => this.loadRegles());
  }

  generatePreventive(): void {
    this.regleService.generate().subscribe(result => {
      this.generatedMessage = `${result.generated} demande(s) preventive(s) generee(s).`;
      this.search();
      this.loadRegles();
    });
  }

  label(value: string): string {
    if (value === 'CONTROLE_PERIODIQUE') return 'controle periodique';
    if (value === 'JOUR') return 'jour(s)';
    if (value === 'SEMAINE') return 'semaine(s)';
    if (value === 'MOIS') return 'mois';
    if (value === 'ANNEE') return 'annee(s)';
    if (value === 'EN_ATTENTE_VALIDATION') return 'en attente validation';
    return value.replaceAll('_', ' ').toLowerCase();
  }

  ruleValueLabel(rule: RegleMaintenancePreventive): string {
    return `Chaque ${rule.frequence || 1} ${this.label(rule.unite || 'JOUR')}`;
  }
}
