import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { IonicModule, ToastController } from '@ionic/angular';
import { interval, Subscription } from 'rxjs';

import { Machine } from '../../models/Machine';
import { TypeAction } from '../../models/TypeAction';
import { TypeMaintenance } from '../../models/TypeMaintenance';
import { MachineMaintenanceIndicators } from '../../models/DashboardMaintenance';

import { MachineService } from '../../services/machine';
import { TypeActionService } from '../../services/type-action';
import { TypeMaintenanceService } from '../../services/type-maintenance';
import {
  DemandeMaintenance,
  DemandeMaintenanceService,
  PrioriteDemandeMaintenance,
  StatutDemandeMaintenance
} from '../../services/demande-maintenance';
import { DashboardMaintenanceService } from '../../services/dashboard-maintenance';
import { MaintenanceService } from '../../services/maintenance';

@Component({
  standalone: true,
  selector: 'app-machines-operateur-detail',
  templateUrl: './machines-operateur-detail.page.html',
  styleUrls: ['./machines-operateur-detail.page.scss'],
  imports: [IonicModule, CommonModule, FormsModule]
})
export class MachinesOperateurDetailPage implements OnInit, OnDestroy {

  machine?: Machine;
  indicators?: MachineMaintenanceIndicators;
  typeActions: TypeAction[] = [];
  typeMaintenances: TypeMaintenance[] = [];
  demandes: DemandeMaintenance[] = [];
  maintenances: any[] = [];
  piecesCompatibles: any[] = [];
  isMaintenanceProfile = false;
  activeTab: 'etat' | 'demandes' | 'intervention' = 'etat';

  loading = true;
  changingState = false;
  sendingDemande = false;
  sendingIntervention = false;
  updatingDemandeId?: number;
  returningPieceId?: number;
  planificationDates: Record<number, string> = {};
  retourPieceForms: Record<number, { quantite: number; cause: string }> = {};

  now: Date = new Date();
  private timerSub?: Subscription;
  pendingOfflineCount = 0;

  showMaintenancePanel = false;
  selectedMaintenanceId?: number;
  showNewTypeInput = false;
  newMaintenanceLabel = '';
  maintenanceDescription = '';
  maintenancePriority: PrioriteDemandeMaintenance = 'NORMALE';

  selectedImageFile?: File;
  selectedImageName = '';

  showInterventionPanel = false;
  selectedInterventionTypeId?: number;
  showNewInterventionTypeInput = false;
  newInterventionTypeLabel = '';
  interventionDescription = '';
  interventionNote = '';
  interventionPieces = '';
  selectedPieceId?: number;
  selectedPieceQuantity = 1;
  piecesIntervention: Array<{ pieceId: number; quantite: number; label: string }> = [];
  interventionCost?: number;
  interventionMode: 'INTERNE' | 'EXTERNE' = 'INTERNE';
  fournisseurNom = '';
  fournisseurContact = '';
  fournisseurTelephone = '';
  fournisseurEmail = '';
  fournisseurAdresse = '';
  referenceDevis = '';
  messageDemandeExterne = '';
  interventionCause = '';
  interventionChecklist = '';
  controleEffectue = false;
  nettoyageEffectue = false;
  testFinalEffectue = false;
  validationSignature = '';
  interventionImageFile?: File;
  interventionImageName = '';
  interventionBeforeImageFile?: File;
  interventionBeforeImageName = '';
  interventionAfterImageFile?: File;
  interventionAfterImageName = '';
  selectedDemandeForIntervention?: DemandeMaintenance;

  constructor(
    private route: ActivatedRoute,
    private machineService: MachineService,
    private typeActionService: TypeActionService,
    private typeMaintenanceService: TypeMaintenanceService,
    private demandeMaintenanceService: DemandeMaintenanceService,
    private dashboardMaintenanceService: DashboardMaintenanceService,
    private maintenanceService: MaintenanceService,
    private toastCtrl: ToastController
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) return;

    this.isMaintenanceProfile = this.route.snapshot.routeConfig?.path?.startsWith('machines-maintenance') ?? false;

    this.loadMachine(id);
    this.loadIndicators(id);
    this.loadTypeActions();
    this.loadTypeMaintenances();
    if (this.isMaintenanceProfile) {
      this.loadDemandes(id);
      this.loadMaintenances(id);
    }

    this.timerSub = interval(1000).subscribe(() => {
      this.now = new Date();
    });
    this.refreshPendingOfflineCount();
  }

  ngOnDestroy(): void {
    this.timerSub?.unsubscribe();
  }

  loadMachine(id: number): void {
    this.loading = true;
    this.machineService.getMachine(id).subscribe({
      next: machine => {
        this.machine = machine;
        this.loadPiecesCompatibles(machine.id);
        this.loading = false;
      },
      error: async err => {
        console.error(err);
        this.loading = false;
        await this.showToast('Erreur de chargement de la machine', 'danger');
      }
    });
  }

  loadIndicators(id: number): void {
    this.dashboardMaintenanceService.getMachineIndicators(id).subscribe({
      next: indicators => this.indicators = indicators,
      error: err => console.error(err)
    });
  }

  loadTypeActions(): void {
    this.typeActionService.getActifs().subscribe({
      next: list => this.typeActions = list,
      error: err => console.error(err)
    });
  }

  loadTypeMaintenances(): void {
    this.typeMaintenanceService.getActifs().subscribe({
      next: list => this.typeMaintenances = list,
      error: err => console.error(err)
    });
  }

  loadDemandes(machineId: number): void {
    this.demandeMaintenanceService.getByMachine(machineId).subscribe({
      next: demandes => {
        this.demandes = demandes;
        for (const demande of demandes) {
          if (demande.dateSouhaitee && !this.planificationDates[demande.id]) {
            this.planificationDates[demande.id] = this.toDateTimeLocalValue(demande.dateSouhaitee);
          }
        }
      },
      error: err => console.error(err)
    });
  }

  loadPiecesCompatibles(machineId: number): void {
    this.maintenanceService.getPiecesCompatibles(machineId).subscribe({
      next: pieces => this.piecesCompatibles = pieces,
      error: err => console.error(err)
    });
  }

  loadMaintenances(machineId: number): void {
    this.maintenanceService.getByMachine(machineId).subscribe({
      next: maintenances => {
        this.maintenances = maintenances;
        for (const maintenance of maintenances) {
          for (const piece of maintenance.pieces || []) {
            if (!this.retourPieceForms[piece.id]) {
              this.retourPieceForms[piece.id] = {
                quantite: Math.max(piece.quantiteDisponibleRetour || 0, 1),
                cause: ''
              };
            }
          }
        }
      },
      error: err => console.error(err)
    });
  }

  getEtatLabel(): string {
    if (!this.machine?.typeAction) return 'Etat inconnu';
    return this.machine.typeAction.libelle || this.machine.typeAction.code;
  }

  getEtatStyle() {
    const color = this.machine?.typeAction?.color || '#64748b';
    return {
      'background-color': color.length === 7 ? color + '22' : color,
      'border-color': color,
      'color': color
    };
  }

  getElapsedCurrent(): string {
    if (!this.machine?.dateDebutAction) return '--:--:--';
    const start = new Date(this.machine.dateDebutAction);
    const diffMs = this.now.getTime() - start.getTime();
    if (diffMs < 0) return '00:00:00';

    const totalSeconds = Math.floor(diffMs / 1000);
    const h = Math.floor(totalSeconds / 3600);
    const m = Math.floor((totalSeconds % 3600) / 60);
    const s = totalSeconds % 60;
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${pad(h)}:${pad(m)}:${pad(s)}`;
  }

  formatDuration(minutes?: number): string {
    if (!minutes || minutes <= 0) return '0 min';
    const days = Math.floor(minutes / 1440);
    const hours = Math.floor((minutes % 1440) / 60);
    const mins = Math.floor(minutes % 60);
    if (days > 0) return `${days} j ${hours} h`;
    if (hours > 0) return `${hours} h ${mins} min`;
    return `${mins} min`;
  }

  getAverageBetweenFailuresLabel(indicators?: MachineMaintenanceIndicators): string {
    if (!indicators || indicators.failureCount <= 0 || indicators.mtbfHours <= 0) {
      return 'Non calculable';
    }
    return `${indicators.mtbfHours.toFixed(1)} h`;
  }

  getAverageRepairTimeLabel(indicators?: MachineMaintenanceIndicators): string {
    if (!indicators || indicators.failureCount <= 0 || indicators.mttrHours <= 0) {
      return 'Non calculable';
    }
    return `${indicators.mttrHours.toFixed(1)} h`;
  }

  getAverageBetweenFailuresHelp(indicators?: MachineMaintenanceIndicators): string {
    if (!indicators || indicators.failureCount <= 0) {
      return 'Aucune panne detectee.';
    }
    return `${indicators.failureCount} panne(s) dans le calcul.`;
  }

  getAverageRepairTimeHelp(indicators?: MachineMaintenanceIndicators): string {
    if (!indicators || indicators.failureCount <= 0) {
      return 'Besoin d une panne terminee.';
    }
    return `${this.formatDuration(indicators.downtimeMinutes)} d arret.`;
  }

  async onChangeEtat(typeAction: TypeAction): Promise<void> {
    if (!this.machine) return;
    if (this.machine.typeAction?.id === typeAction.id) return;

    this.changingState = true;
    this.machineService.changeEtat(this.machine.id, typeAction.id).subscribe({
      next: async action => {
        this.machine!.typeAction = typeAction;
        this.machine!.dateDebutAction = action.dateDebut;
        this.loadIndicators(this.machine!.id);
        this.changingState = false;
        await this.showToast(`Etat change en "${typeAction.libelle || typeAction.code}"`, 'success');
      },
      error: async err => {
        console.error(err);
        this.changingState = false;
        await this.showToast('Erreur lors du changement etat', 'danger');
      }
    });
  }

  toggleMaintenancePanel(): void {
    this.showMaintenancePanel = !this.showMaintenancePanel;
  }

  selectMaintenanceType(type: TypeMaintenance): void {
    this.selectedMaintenanceId = type.id;
    this.showNewTypeInput = false;
  }

  onSelectAutre(): void {
    this.selectedMaintenanceId = undefined;
    this.showNewTypeInput = true;
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      this.selectedImageFile = undefined;
      this.selectedImageName = '';
      return;
    }
    this.selectedImageFile = input.files[0];
    this.selectedImageName = this.selectedImageFile.name;
  }

  toggleInterventionPanel(): void {
    this.showInterventionPanel = !this.showInterventionPanel;
  }

  openInterventionForDemande(demande: DemandeMaintenance): void {
    this.selectedDemandeForIntervention = demande;
    this.showInterventionPanel = true;
    this.interventionDescription = demande.description || '';
    this.interventionNote = `Demande #${demande.id}`;

    if (demande.typeMaintenance?.id) {
      this.selectedInterventionTypeId = demande.typeMaintenance.id;
      this.showNewInterventionTypeInput = false;
    }
  }

  selectInterventionType(type: TypeMaintenance): void {
    this.selectedInterventionTypeId = type.id;
    this.showNewInterventionTypeInput = false;
  }

  onSelectAutreIntervention(): void {
    this.selectedInterventionTypeId = undefined;
    this.showNewInterventionTypeInput = true;
  }

  addPieceIntervention(): void {
    if (!this.selectedPieceId || this.selectedPieceQuantity <= 0) {
      return;
    }
    const piece = this.piecesCompatibles.find(item => item.id === this.selectedPieceId);
    if (!piece) {
      return;
    }
    const existing = this.piecesIntervention.find(item => item.pieceId === this.selectedPieceId);
    if (existing) {
      existing.quantite += this.selectedPieceQuantity;
    } else {
      this.piecesIntervention.push({
        pieceId: this.selectedPieceId,
        quantite: this.selectedPieceQuantity,
        label: `${piece.reference || ''} ${piece.libelle || ''}`.trim()
      });
    }
    this.selectedPieceId = undefined;
    this.selectedPieceQuantity = 1;
  }

  removePieceIntervention(pieceId: number): void {
    this.piecesIntervention = this.piecesIntervention.filter(item => item.pieceId !== pieceId);
  }

  onInterventionImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      this.interventionImageFile = undefined;
      this.interventionImageName = '';
      return;
    }
    this.interventionImageFile = input.files[0];
    this.interventionImageName = this.interventionImageFile.name;
  }

  async onCreateIntervention(): Promise<void> {
    if (!this.machine) return;

    const hasSelected = !!this.selectedInterventionTypeId;
    const hasNewLabel = this.newInterventionTypeLabel.trim().length > 0;

    if (!hasSelected && !hasNewLabel) {
      await this.showToast('Selectionnez un type de maintenance.', 'warning');
      return;
    }

    if (!this.interventionDescription.trim()) {
      await this.showToast('La description est obligatoire.', 'warning');
      return;
    }
    if (this.selectedPieceId) {
      this.addPieceIntervention();
    }

    this.sendingIntervention = true;

    const dto = {
      machineId: this.machine.id,
      demandeMaintenanceId: this.selectedDemandeForIntervention?.id ?? null,
      typeMaintenanceId: hasSelected ? this.selectedInterventionTypeId! : null,
      nouveauTypeLibelle: !hasSelected ? this.newInterventionTypeLabel.trim() : null,
      description: this.interventionDescription.trim(),
      note: this.interventionNote.trim(),
      piecesUtilisees: this.interventionPieces.trim(),
      pieces: this.piecesIntervention.map(piece => ({
        pieceId: piece.pieceId,
        quantite: piece.quantite
      })),
      cout: this.interventionCost ?? null,
      typeIntervention: this.interventionMode,
      fournisseurNom: this.fournisseurNom.trim(),
      fournisseurContact: this.fournisseurContact.trim(),
      fournisseurTelephone: this.fournisseurTelephone.trim(),
      fournisseurEmail: this.fournisseurEmail.trim(),
      fournisseurAdresse: this.fournisseurAdresse.trim(),
      referenceDevis: this.referenceDevis.trim(),
      messageDemandeExterne: this.messageDemandeExterne.trim(),
      causePanne: this.interventionCause.trim(),
      checklist: this.interventionChecklist.trim(),
      controleEffectue: this.controleEffectue,
      nettoyageEffectue: this.nettoyageEffectue,
      testFinalEffectue: this.testFinalEffectue,
      validationSignature: this.validationSignature.trim(),
      user: 'MAINTENANCE'
    };

    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(dto)], { type: 'application/json' }));

    if (this.interventionImageFile) {
      formData.append('image', this.interventionImageFile);
    }
    if (this.interventionBeforeImageFile) {
      formData.append('imageAvant', this.interventionBeforeImageFile);
    }
    if (this.interventionAfterImageFile) {
      formData.append('imageApres', this.interventionAfterImageFile);
    }

    this.maintenanceService.createIntervention(formData).subscribe({
      next: async () => {
        this.sendingIntervention = false;
        this.interventionDescription = '';
        this.interventionNote = '';
        this.interventionPieces = '';
        this.piecesIntervention = [];
        this.selectedPieceId = undefined;
        this.selectedPieceQuantity = 1;
        this.interventionCost = undefined;
        this.interventionMode = 'INTERNE';
        this.fournisseurNom = '';
        this.fournisseurContact = '';
        this.fournisseurTelephone = '';
        this.fournisseurEmail = '';
        this.fournisseurAdresse = '';
        this.referenceDevis = '';
        this.messageDemandeExterne = '';
        this.interventionCause = '';
        this.interventionChecklist = '';
        this.controleEffectue = false;
        this.nettoyageEffectue = false;
        this.testFinalEffectue = false;
        this.validationSignature = '';
        this.newInterventionTypeLabel = '';
        this.selectedInterventionTypeId = undefined;
        this.showNewInterventionTypeInput = false;
        this.interventionImageFile = undefined;
        this.interventionImageName = '';
        this.interventionBeforeImageFile = undefined;
        this.interventionBeforeImageName = '';
        this.interventionAfterImageFile = undefined;
        this.interventionAfterImageName = '';
        this.selectedDemandeForIntervention = undefined;
        this.showInterventionPanel = false;
        this.loadIndicators(this.machine!.id);
        this.loadDemandes(this.machine!.id);
        this.loadMaintenances(this.machine!.id);
        this.loadPiecesCompatibles(this.machine!.id);
        await this.showToast('Intervention enregistree.', 'success');
      },
      error: async err => {
        console.error(err);
        this.sendingIntervention = false;
        this.saveOffline('intervention', dto);
        await this.showToast("Connexion indisponible: intervention gardee hors ligne.", 'warning');
      }
    });
  }

  async prepareExternalDraft(): Promise<void> {
    if (!this.machine) {
      return;
    }
    if (!this.fournisseurNom.trim() || !this.messageDemandeExterne.trim()) {
      await this.showToast('Saisissez le fournisseur et le message externe.', 'warning');
      return;
    }

    this.maintenanceService.createExternalDraft({
      machineId: this.machine.id,
      demandeMaintenanceId: this.selectedDemandeForIntervention?.id ?? null,
      fournisseurNom: this.fournisseurNom.trim(),
      fournisseurContact: this.fournisseurContact.trim(),
      fournisseurTelephone: this.fournisseurTelephone.trim(),
      fournisseurEmail: this.fournisseurEmail.trim(),
      fournisseurAdresse: this.fournisseurAdresse.trim(),
      objet: this.referenceDevis.trim() || `Intervention externe - ${this.machine.nom}`,
      description: this.messageDemandeExterne.trim()
    }).subscribe({
      next: async () => {
        await this.showToast('Brouillon externe prepare.', 'success');
      },
      error: async err => {
        console.error(err);
        await this.showToast('Erreur preparation brouillon externe.', 'danger');
      }
    });
  }

  async onDemandeMaintenance(): Promise<void> {
    if (!this.machine) return;

    const hasSelected = !!this.selectedMaintenanceId;
    const hasNewLabel = this.newMaintenanceLabel.trim().length > 0;

    if (!hasSelected && !hasNewLabel) {
      await this.showToast('Selectionnez un type de maintenance.', 'warning');
      return;
    }

    if (!this.maintenanceDescription.trim()) {
      await this.showToast('La description est obligatoire.', 'warning');
      return;
    }

    this.sendingDemande = true;

    const dto = {
      machineId: this.machine.id,
      typeMaintenanceId: hasSelected ? this.selectedMaintenanceId! : null,
      nouveauTypeLibelle: !hasSelected ? this.newMaintenanceLabel.trim() : null,
      description: this.maintenanceDescription.trim(),
      priorite: this.maintenancePriority,
      demandeur: 'OPERATEUR'
    };

    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(dto)], { type: 'application/json' }));

    if (this.selectedImageFile) {
      formData.append('image', this.selectedImageFile);
    }

    this.demandeMaintenanceService.createDemande(formData).subscribe({
      next: async () => {
        this.sendingDemande = false;
        this.maintenanceDescription = '';
        this.maintenancePriority = 'NORMALE';
        this.newMaintenanceLabel = '';
        this.selectedMaintenanceId = undefined;
        this.showNewTypeInput = false;
        this.selectedImageFile = undefined;
        this.selectedImageName = '';
        this.loadDemandes(this.machine!.id);
        await this.showToast('Demande envoyee.', 'success');
      },
      error: async err => {
        console.error(err);
        this.sendingDemande = false;
        this.saveOffline('demande', dto);
        await this.showToast('Connexion indisponible: demande gardee hors ligne.', 'warning');
      }
    });
  }

  syncOffline(): void {
    const items = this.getOfflineItems();
    if (items.length === 0) {
      return;
    }

    let completed = 0;
    const remaining = [...items];

    for (const item of items) {
      const formData = new FormData();
      formData.append('data', new Blob([JSON.stringify(item.payload)], { type: 'application/json' }));

      const request$ = item.type === 'demande'
        ? this.demandeMaintenanceService.createDemande(formData)
        : this.maintenanceService.createIntervention(formData);

      request$.subscribe({
        next: () => {
          completed++;
          const index = remaining.indexOf(item);
          if (index >= 0) {
            remaining.splice(index, 1);
          }
          this.setOfflineItems(remaining);
          if (this.machine) {
            this.loadDemandes(this.machine.id);
            this.loadIndicators(this.machine.id);
          }
          this.showToast(`${completed} element(s) synchronise(s).`, 'success');
        },
        error: err => console.error(err)
      });
    }
  }

  updateDemandeStatut(
    demande: DemandeMaintenance,
    statut: StatutDemandeMaintenance,
    dateSouhaitee?: string
  ): void {
    if (demande.statut === statut) {
      return;
    }

    this.updatingDemandeId = demande.id;
    this.demandeMaintenanceService.updateStatut(demande.id, statut, dateSouhaitee).subscribe({
      next: updated => {
        this.demandes = this.demandes.map(item => item.id === updated.id ? updated : item);
        if (updated.dateSouhaitee) {
          this.planificationDates[updated.id] = this.toDateTimeLocalValue(updated.dateSouhaitee);
        }
        this.updatingDemandeId = undefined;
        if (this.machine) {
          this.loadIndicators(this.machine.id);
        }
      },
      error: async err => {
        console.error(err);
        this.updatingDemandeId = undefined;
        await this.showToast('Erreur changement statut.', 'danger');
      }
    });
  }

  planifierDemande(demande: DemandeMaintenance): void {
    const value = this.planificationDates[demande.id];
    if (!value) {
      return;
    }
    this.updateDemandeStatut(demande, 'PLANIFIEE', value);
  }

  validerIntervention(demande: DemandeMaintenance): void {
    this.updatingDemandeId = demande.id;
    this.demandeMaintenanceService.validerIntervention(demande.id).subscribe({
      next: updated => {
        this.demandes = this.demandes.map(item => item.id === updated.id ? updated : item);
        this.updatingDemandeId = undefined;
        if (this.machine) {
          this.loadIndicators(this.machine.id);
        }
      },
      error: async err => {
        console.error(err);
        this.updatingDemandeId = undefined;
        await this.showToast('Erreur validation intervention.', 'danger');
      }
    });
  }

  refuserIntervention(demande: DemandeMaintenance): void {
    this.updatingDemandeId = demande.id;
    this.demandeMaintenanceService.refuserIntervention(demande.id).subscribe({
      next: updated => {
        this.demandes = this.demandes.map(item => item.id === updated.id ? updated : item);
        this.updatingDemandeId = undefined;
      },
      error: async err => {
        console.error(err);
        this.updatingDemandeId = undefined;
        await this.showToast('Erreur refus intervention.', 'danger');
      }
    });
  }

  async retournerPiece(pieceUtilisee: any): Promise<void> {
    if (!this.machine || !pieceUtilisee?.id) {
      return;
    }
    const form = this.retourPieceForms[pieceUtilisee.id];
    if (!form || form.quantite <= 0 || !form.cause.trim()) {
      await this.showToast('Saisissez la quantite et la cause du retour.', 'warning');
      return;
    }
    this.returningPieceId = pieceUtilisee.id;
    this.maintenanceService.retournerPiece(pieceUtilisee.id, {
      quantite: form.quantite,
      cause: form.cause.trim()
    }).subscribe({
      next: async () => {
        form.cause = '';
        this.returningPieceId = undefined;
        this.loadMaintenances(this.machine!.id);
        this.loadPiecesCompatibles(this.machine!.id);
        await this.showToast('Piece retournee au stock.', 'success');
      },
      error: async err => {
        console.error(err);
        this.returningPieceId = undefined;
        await this.showToast('Erreur retour stock.', 'danger');
      }
    });
  }

  getStatutLabel(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'En attente';
      case 'PLANIFIEE': return 'Planifiee';
      case 'EN_COURS': return 'En cours';
      case 'EN_ATTENTE_VALIDATION': return 'En attente validation';
      case 'TERMINEE': return 'Terminee';
      case 'ANNULEE': return 'Annulee';
      default: return statut;
    }
  }

  getPrioriteLabel(priorite?: string): string {
    switch (priorite) {
      case 'FAIBLE': return 'Faible';
      case 'NORMALE': return 'Normale';
      case 'URGENTE': return 'Urgente';
      case 'CRITIQUE': return 'Critique';
      default: return 'Normale';
    }
  }

  onInterventionBeforeImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      this.interventionBeforeImageFile = undefined;
      this.interventionBeforeImageName = '';
      return;
    }
    this.interventionBeforeImageFile = input.files[0];
    this.interventionBeforeImageName = this.interventionBeforeImageFile.name;
  }

  onInterventionAfterImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      this.interventionAfterImageFile = undefined;
      this.interventionAfterImageName = '';
      return;
    }
    this.interventionAfterImageFile = input.files[0];
    this.interventionAfterImageName = this.interventionAfterImageFile.name;
  }

  canStartDemande(demande: DemandeMaintenance): boolean {
    return demande.statut === 'EN_ATTENTE' || demande.statut === 'PLANIFIEE';
  }

  canMarkPendingDemande(demande: DemandeMaintenance): boolean {
    return demande.statut !== 'EN_ATTENTE'
      && demande.statut !== 'EN_ATTENTE_VALIDATION'
      && demande.statut !== 'TERMINEE';
  }

  canFinishDemande(demande: DemandeMaintenance): boolean {
    return demande.statut === 'EN_COURS' || demande.statut === 'PLANIFIEE';
  }

  canCancelDemande(demande: DemandeMaintenance): boolean {
    return demande.statut !== 'EN_ATTENTE_VALIDATION'
      && demande.statut !== 'TERMINEE'
      && demande.statut !== 'ANNULEE';
  }

  isDemandeLate(demande: DemandeMaintenance): boolean {
    if (!demande.dateSouhaitee || demande.statut === 'TERMINEE' || demande.statut === 'ANNULEE') {
      return false;
    }
    return new Date(demande.dateSouhaitee).getTime() < this.now.getTime();
  }

  getRetardLabel(demande: DemandeMaintenance): string {
    if (!demande.dateSouhaitee) {
      return '';
    }
    const diffMinutes = Math.floor((this.now.getTime() - new Date(demande.dateSouhaitee).getTime()) / 60000);
    return `Retard: ${this.formatDuration(diffMinutes)}`;
  }

  getDefaultPlanificationDate(demande: DemandeMaintenance): string {
    if (!this.planificationDates[demande.id]) {
      const date = new Date(Date.now() + 60 * 60 * 1000);
      this.planificationDates[demande.id] = this.toDateTimeLocalValue(date.toISOString());
    }
    return this.planificationDates[demande.id];
  }

  private toDateTimeLocalValue(value: string): string {
    const date = new Date(value);
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  private saveOffline(type: 'demande' | 'intervention', payload: unknown): void {
    const items = this.getOfflineItems();
    items.push({ type, payload, createdAt: new Date().toISOString() });
    this.setOfflineItems(items);
  }

  private getOfflineItems(): Array<{ type: 'demande' | 'intervention'; payload: unknown; createdAt: string }> {
    try {
      return JSON.parse(localStorage.getItem('canal-ma-offline-maintenance') || '[]');
    } catch {
      return [];
    }
  }

  private setOfflineItems(items: Array<{ type: 'demande' | 'intervention'; payload: unknown; createdAt: string }>): void {
    localStorage.setItem('canal-ma-offline-maintenance', JSON.stringify(items));
    this.refreshPendingOfflineCount();
  }

  private refreshPendingOfflineCount(): void {
    this.pendingOfflineCount = this.getOfflineItems().length;
  }

  private async showToast(
    message: string,
    color: 'success'|'danger'|'warning'|'medium' = 'medium'
  ): Promise<void> {
    const toast = await this.toastCtrl.create({
      message,
      duration: 2000,
      color,
      position: 'top'
    });
    await toast.present();
  }

  protected readonly history = history;
}
