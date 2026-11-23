import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MachineService } from '../../services/machine.service';
import { Machine } from '../../models/machine.model';
import { Action } from '../../models/action.model';
import { interval, Subscription } from 'rxjs';
import { TypeAction } from '../../models/TypeAction';
import { DemandeMaintenance } from '../../models/demande-maintenance.model';
import {
  DemandeMaintenanceService,
  StatutDemandeMaintenance
} from '../../services/demande-maintenance.service';
import { DashboardMaintenanceService } from '../../services/dashboard-maintenance.service';
import { MachineMaintenanceIndicators } from '../../models/dashboard-maintenance.model';
import {
  DemandeInterventionExterne,
  InterventionExterneService
} from '../../services/intervention-externe.service';
import { MaintenanceService } from '../../services/maintenance.service';
import { TypeMaintenance } from '../../models/TypeMaintenance';
import { TypeMaintenanceService } from '../../services/type-maintenance.service';
import { TypeActionService } from '../../services/type-action.service';
import { PieceRechangeService } from '../../services/piece-rechange.service';
import { AiDiagnosticResponse, AiMaintenanceService } from '../../services/ai-maintenance.service';
import { MlPredictionService, MlPrediction, MlHistory, MlReport } from '../../services/ml-prediction.service';
import { AuthService } from '../../services/auth.service';

interface StateStat {
  type: string;
  label: string;
  color: string;
  durationMs: number;
  percentage: number;
}

interface TimelineSegment {
  type: string;
  label: string;
  color: string;
  widthPercent: number;
}

interface AiChatMessage {
  role: 'USER' | 'AI';
  content: string;
  sections?: Array<{ title: string; content: string }>;
}

@Component({
  selector: 'app-machine-detail',
  templateUrl: './machine-detail.component.html'
})
export class MachineDetailComponent implements OnInit, OnDestroy {

  machine?: Machine;
  actions: Action[] = [];
  demandes: DemandeMaintenance[] = [];
  maintenances: any[] = [];
  demandesExternes: DemandeInterventionExterne[] = [];
  typeMaintenances: TypeMaintenance[] = [];
  typeActions: TypeAction[] = [];
  piecesCompatibles: any[] = [];
  indicators?: MachineMaintenanceIndicators;
  aiDiagnostic?: AiDiagnosticResponse;
  aiDiagnosticLoading = false;
  aiTestLoading = false;
  aiChatInput = '';
  aiChatLoading = false;
  aiChatMessages: AiChatMessage[] = [
    {
      role: 'AI',
      content: 'Bonjour. Pose une question sur cette machine, par exemple: pourquoi cette machine est critique ? ou quelles pieces verifier ?'
    }
  ];
  mlPrediction?: MlPrediction;
  mlPredictionLoading = false;
  mlPredictionError = '';
    mlHistory?: MlHistory;
  mlChart?: { line: string; thr: number; failures: number[]; first: string; last: string };
    mlReport?: MlReport;
  mlReportLoading = false;
  mlReportError = '';
  errorMessage = '';
  successMessage = '';

  demandeForm = {
    typeMaintenanceId: undefined as number | undefined,
    nouveauTypeLibelle: '',
    description: '',
    priorite: 'NORMALE' as any,
    demandeur: 'OPERATEUR'
  };
  demandeImageFile?: File;

  interventionForm = {
    demandeMaintenanceId: undefined as number | undefined,
    typeMaintenanceId: undefined as number | undefined,
    nouveauTypeLibelle: '',
    description: '',
    note: '',
    piecesUtilisees: '',
    cout: null as number | null,
    typeIntervention: 'INTERNE' as 'INTERNE' | 'EXTERNE',
    fournisseurNom: '',
    fournisseurContact: '',
    fournisseurTelephone: '',
    fournisseurEmail: '',
    fournisseurAdresse: '',
    referenceDevis: '',
    messageDemandeExterne: '',
    causePanne: '',
    checklist: '',
    controleEffectue: false,
    nettoyageEffectue: false,
    testFinalEffectue: false,
    validationSignature: ''
  };
  selectedPieceId?: number;
  selectedPieceQuantity = 1;
  piecesIntervention: Array<{ pieceId: number; quantite: number; label: string }> = [];
  interventionImageFile?: File;
  interventionBeforeImageFile?: File;
  interventionAfterImageFile?: File;
  creatingDemande = false;
  creatingIntervention = false;
  externalDraft = {
    fournisseurNom: '',
    fournisseurContact: '',
    fournisseurTelephone: '',
    fournisseurEmail: '',
    fournisseurAdresse: '',
    objet: '',
    description: '',
    dateSouhaitee: ''
  };

  now: Date = new Date();

  fromDate!: string;
  toDate!: string;

  lastUpdate?: Date;

  stats: StateStat[] = [];
  timelineSegments: TimelineSegment[] = [];
  timelineFrom?: Date;
  timelineTo?: Date;

  loadingDemandes = false;
  updatingDemandeId?: number;
  planificationDates: Record<number, string> = {};
  retourPieceForms: Record<number, { quantite: number; cause: string }> = {};
  returningPieceId?: number;

  private timerSub?: Subscription;
  private refreshSub?: Subscription;

  constructor(
    public auth: AuthService,
    private route: ActivatedRoute,
    private machineService: MachineService,
    private demandeMaintenanceService: DemandeMaintenanceService,
    private dashboardMaintenanceService: DashboardMaintenanceService,
    private maintenanceService: MaintenanceService,
    private interventionExterneService: InterventionExterneService,
    private typeMaintenanceService: TypeMaintenanceService,
    private typeActionService: TypeActionService,
    private pieceRechangeService: PieceRechangeService,
    private aiMaintenanceService: AiMaintenanceService,
    private mlPredictionService: MlPredictionService
     ) {}
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.initDates();
    this.loadMachine(id);
    this.loadActions(id);
    this.loadDemandes(id);
    this.loadMaintenances(id);
    this.loadDemandesExternes(id);
    this.loadIndicators(id);
    this.loadTypeMaintenances();
    this.loadTypeActions();
    this.loadPiecesCompatibles(id);

    this.timerSub = interval(1000).subscribe(() => this.now = new Date());
    this.refreshSub = interval(15000).subscribe(() => {
      this.loadMachine(id);
      this.loadActions(id, false);
      this.loadDemandes(id, false);
      this.loadMaintenances(id);
      this.loadDemandesExternes(id);
      this.loadIndicators(id);
    });
  }

  ngOnDestroy(): void {
    this.timerSub?.unsubscribe();
    this.refreshSub?.unsubscribe();
  }

  // Periode initiale affichee dans les statistiques.
  initDates(): void {
    const now = new Date();
    const past = new Date(now.getTime() - 24 * 60 * 60 * 1000);
    this.fromDate = past.toISOString().substring(0, 10);
    this.toDate = now.toISOString().substring(0, 10);
  }

  loadMachine(id: number): void {
    this.machineService.getMachine(id).subscribe({
      next: m => {
        this.machine = m;
        this.updateLastUpdate();
      },
      error: err => console.error(err)
    });
  }

  loadActions(id: number, useDates: boolean = true): void {
    let fromIso: string | undefined;
    let toIso: string | undefined;

    if (useDates && this.fromDate && this.toDate) {
      fromIso = `${this.fromDate}T00:00:00`;
      toIso = `${this.toDate}T23:59:59`;
    }

    this.machineService.getActionsForMachine(id, fromIso, toIso).subscribe({
      next: actions => {
        this.actions = actions;
        this.updateLastUpdate();
        this.computeStats();
        this.computeTimeline();
      },
      error: err => console.error(err)
    });
  }

  // Charge les demandes liees a la machine courante.
  loadDemandes(id: number, useDates: boolean = true): void {
    let fromIso: string | undefined;
    let toIso: string | undefined;

    if (useDates && this.fromDate && this.toDate) {
      fromIso = `${this.fromDate}T00:00:00`;
      toIso = `${this.toDate}T23:59:59`;
    }

    this.loadingDemandes = true;
    this.demandeMaintenanceService.getByMachine(id, fromIso, toIso).subscribe({
      next: demandes => {
        this.demandes = demandes;
        for (const demande of demandes) {
          if (demande.dateSouhaitee && !this.planificationDates[demande.id]) {
            this.planificationDates[demande.id] = this.toDateTimeLocalValue(demande.dateSouhaitee);
          }
        }
        this.loadingDemandes = false;
      },
      error: err => {
        console.error(err);
        this.loadingDemandes = false;
      }
    });
  }

  loadIndicators(id: number): void {
    this.dashboardMaintenanceService.getMachineMaintenanceIndicators(id).subscribe({
      next: indicators => {
        this.indicators = indicators;
      },
      error: err => console.error(err)
    });
  }

  runAiDiagnostic(): void {
    if (!this.machine) return;
    this.aiDiagnosticLoading = true;
    this.aiDiagnostic = undefined;
    this.errorMessage = '';
    this.aiMaintenanceService.diagnoseMachine(this.machine.id).subscribe({
      next: diagnostic => {
        this.aiDiagnostic = diagnostic;
        this.aiDiagnosticLoading = false;
      },
      error: err => {
        console.error(err);
        this.aiDiagnosticLoading = false;
        this.errorMessage = 'Impossible de lancer le diagnostic IA. Verifie que le backend et Ollama sont demarres.';
      }
    });
  }
  loadMlPrediction(): void {
  if (!this.machine) return;
  this.mlPredictionLoading = true;
  this.mlPredictionError = '';
  this.mlPrediction = undefined;
  this.mlPredictionService.predictMachine(this.machine.id).subscribe({
    next: prediction => {
      this.mlPrediction = prediction;
      this.mlPredictionLoading = false;
      this.loadMlHistory();
    },
    error: err => {
      console.error(err);
      this.mlPredictionLoading = false;
      this.mlPredictionError = 'ML API non disponible. Verifie que Flask tourne sur le port 5000.';
    }
  });
}

getMlRiskClass(): string {
  switch (this.mlPrediction?.riskLevel) {
    case 'CRITIQUE': return 'text-red-600 dark:text-red-400';
    case 'ATTENTION': return 'text-amber-600 dark:text-amber-400';
    default: return 'text-emerald-600 dark:text-emerald-400';
  }
}

getMlProgressColor(): string {
  switch (this.mlPrediction?.riskLevel) {
    case 'CRITIQUE': return '#dc2626';
    case 'ATTENTION': return '#d97706';
    default: return '#16a34a';
  }
}
  loadMlHistory(): void {
    if (!this.machine) return;
    this.mlPredictionService.history(this.machine.id).subscribe({
      next: h => {
        this.mlHistory = h;
        this.buildMlChart();
      },
      error: err => console.error(err)
    });
  }

  private buildMlChart(): void {
    const h = this.mlHistory;
    if (!h || !h.points || h.points.length < 2) {
      this.mlChart = undefined;
      return;
    }
    const first = h.points[0].d;
    const last = h.points[h.points.length - 1].d;
    const t0 = new Date(first).getTime();
    const span = Math.max(new Date(last).getTime() - t0, 1);
    const seuilPct = h.seuil * 100;
    const max = Math.max(seuilPct * 1.3, 10, ...h.points.map(p => p.s));
    const x = (d: string) => 10 + ((new Date(d).getTime() - t0) / span) * 780;
    const y = (s: number) => 190 - (s / max) * 170;
    this.mlChart = {
      line: h.points.map(p => `${x(p.d).toFixed(1)},${y(p.s).toFixed(1)}`).join(' '),
      thr: y(seuilPct),
      failures: h.pannes.filter(d => d >= first && d <= last).map(d => x(d)),
      first,
      last
    };
  }
    loadMlReport(): void {
    if (!this.machine) return;
    this.mlReportLoading = true;
    this.mlReportError = '';
    this.mlReport = undefined;
    this.mlPredictionService.report(this.machine.id).subscribe({
      next: r => {
        this.mlReportLoading = false;
        if (r.error) {
          this.mlReportError = r.error;
        } else {
          this.mlReport = r;
        }
      },
      error: err => {
        console.error(err);
        this.mlReportLoading = false;
        this.mlReportError = 'Rapport indisponible. Verifie que Flask et Ollama sont demarres.';
      }
    });
  }

  cleanReport(text?: string): string {
    return (text || '').replace(/\*/g, '').trim();
  }

  getEconomicConclusion(): string {
    const p = this.mlPrediction;
    const e = p?.economics;
    if (!p || !e || !e.cout_panne_moyen || !e.cout_visite_moyen || e.seuil_rentabilite_pct == null) {
      return 'Données de coûts insuffisantes pour conclure sur cet équipement.';
    }
    const prec = Math.round(p.modelMetrics.precision * 100);
    if (p.riskLevel === 'CRITIQUE') {
      return `Une visite préventive est justifiée : elle coûte environ ${e.cout_visite_moyen} DH contre environ ${e.cout_panne_moyen} DH pour une panne. Elle est rentable dès ${e.seuil_rentabilite_pct} % d'alertes justes ; le modèle en obtient ${prec} % par jour d'alerte au-dessus du seuil (données simulées).`;
    }
    if (p.riskLevel === 'ATTENTION') {
      return `Une inspection légère cette semaine est raisonnable (une visite coûte environ ${e.cout_visite_moyen} DH). Le score est entre la moitié du seuil et le seuil d'alerte : la précision de ${prec} % n'est mesurée qu'au-dessus du seuil, la rentabilité n'est donc pas démontrée à ce niveau.`;
    }
    return 'Aucune visite supplémentaire n\'est justifiée aujourd\'hui : le score est sous la moitié du seuil d\'alerte.';
  }

  getEconomicClass(): string {
    switch (this.mlPrediction?.riskLevel) {
      case 'CRITIQUE': return 'border-red-200 bg-red-50 text-red-800';
      case 'ATTENTION': return 'border-amber-200 bg-amber-50 text-amber-800';
      default: return 'border-emerald-200 bg-emerald-50 text-emerald-800';
    }
  }
  testAiModel(): void {
    this.aiTestLoading = true;
    this.aiDiagnostic = undefined;
    this.errorMessage = '';
    this.aiMaintenanceService.testModel().subscribe({
      next: diagnostic => {
        this.aiDiagnostic = diagnostic;
        this.aiTestLoading = false;
      },
      error: err => {
        console.error(err);
        this.aiTestLoading = false;
        this.errorMessage = 'Impossible de tester Ollama. Verifie que Ollama est demarre et que le modele est installe.';
      }
    });
  }

  getFactorLabel(name: string): string {
  const labels: Record<string, string> = {
    pente_vib: 'Tendance des vibrations (7 j)',
    z_vib: 'Vibrations par rapport à la normale',
    pente_temp: 'Tendance de la température (7 j)',
    z_temp: 'Température par rapport à la normale',
    pente_pres: 'Tendance de la pression (7 j)',
    z_pres: 'Pression par rapport à la normale',
    pente_cycle: 'Tendance du temps de cycle (7 j)',
    z_cycle: 'Temps de cycle par rapport à la normale',
    bruit_7j: 'Bruits anormaux signalés (7 j)',
    jours_depuis_maintenance: 'Jours depuis la dernière maintenance',
    retard_maintenance_ratio: 'Retard de maintenance',
    pannes_30j: 'Pannes sur 30 jours',
    pannes_90j: 'Pannes sur 90 jours',
    jours_depuis_derniere_panne: 'Jours depuis la dernière panne',
    age_mois: 'Âge de l\'équipement (mois)',
    amb_7j: 'Température ambiante (7 j)',
    intensite_7j: 'Intensité d\'utilisation (7 j)',
    durete_7j: 'Dureté de l\'eau (7 j)'
  };
  return labels[name] || name;
  }
  sendAiChatMessage(): void {
    if (!this.aiChatInput.trim() || this.aiChatLoading) return;
    const message = this.aiChatInput.trim();
    this.aiChatMessages.push({ role: 'USER', content: message });
    this.aiChatInput = '';
    this.aiChatLoading = true;
    this.errorMessage = '';

    this.aiMaintenanceService.chat({
      machineId: this.machine?.id,
      message
    }).subscribe({
      next: response => {
        this.aiChatMessages.push({
          role: 'AI',
          content: response.diagnostic,
          sections: response.sections || []
        });
        this.aiChatLoading = false;
      },
      error: err => {
        console.error(err);
        this.aiChatLoading = false;
        this.aiChatMessages.push({
          role: 'AI',
          content: 'Impossible de repondre pour le moment. Verifie que Ollama est demarre et que le modele est installe.'
        });
      }
    });
  }

  loadMaintenances(id: number): void {
    this.maintenanceService.getByMachine(id).subscribe({
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

  loadDemandesExternes(id: number): void {
    this.interventionExterneService.findByMachine(id).subscribe({
      next: demandes => this.demandesExternes = demandes,
      error: err => console.error(err)
    });
  }

  loadTypeMaintenances(): void {
    this.typeMaintenanceService.findActifs().subscribe({
      next: types => this.typeMaintenances = types,
      error: err => console.error(err)
    });
  }

  loadTypeActions(): void {
    this.typeActionService.findActifs().subscribe({
      next: types => this.typeActions = types,
      error: err => console.error(err)
    });
  }

  changeMachineState(typeAction: TypeAction): void {
    if (!this.machine || this.machine.typeAction?.id === typeAction.id) {
      return;
    }
    this.machineService.changeEtat(this.machine.id, typeAction.id).subscribe({
      next: action => {
        this.machine!.typeAction = typeAction;
        this.machine!.dateDebutAction = action.dateDebut;
        this.successMessage = `Etat change: ${typeAction.libelle || typeAction.code}`;
        this.loadActions(this.machine!.id, false);
        this.loadIndicators(this.machine!.id);
      },
      error: err => {
        console.error(err);
        this.errorMessage = "Erreur lors du changement d'etat.";
      }
    });
  }

  loadPiecesCompatibles(machineId: number): void {
    this.pieceRechangeService.findCompatiblesForMachine(machineId).subscribe({
      next: pieces => this.piecesCompatibles = pieces,
      error: err => console.error(err)
    });
  }

  createDemandeMaintenance(): void {
    if (!this.machine || !this.demandeForm.description.trim()) {
      this.errorMessage = 'Merci de saisir une description pour la demande.';
      return;
    }
    if (!this.demandeForm.typeMaintenanceId && !this.demandeForm.nouveauTypeLibelle.trim()) {
      this.errorMessage = 'Merci de choisir un type ou saisir un nouveau type.';
      return;
    }

    this.creatingDemande = true;
    this.errorMessage = '';
    const dto = {
      machineId: this.machine.id,
      typeMaintenanceId: this.demandeForm.typeMaintenanceId || null,
      nouveauTypeLibelle: this.demandeForm.typeMaintenanceId ? null : this.demandeForm.nouveauTypeLibelle.trim(),
      description: this.demandeForm.description.trim(),
      priorite: this.demandeForm.priorite,
      demandeur: this.demandeForm.demandeur || 'OPERATEUR'
    };
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(dto)], { type: 'application/json' }));
    if (this.demandeImageFile) {
      formData.append('image', this.demandeImageFile);
    }

    this.demandeMaintenanceService.createDemande(formData).subscribe({
      next: () => {
        this.creatingDemande = false;
        this.successMessage = 'Demande de maintenance creee.';
        this.demandeForm = {
          typeMaintenanceId: undefined,
          nouveauTypeLibelle: '',
          description: '',
          priorite: 'NORMALE',
          demandeur: 'OPERATEUR'
        };
        this.demandeImageFile = undefined;
        this.loadDemandes(this.machine!.id);
        this.loadIndicators(this.machine!.id);
      },
      error: err => {
        console.error(err);
        this.creatingDemande = false;
        this.errorMessage = 'Erreur lors de la creation de la demande.';
      }
    });
  }

  createIntervention(): void {
    if (!this.machine || !this.interventionForm.description.trim()) {
      this.errorMessage = 'Merci de saisir une description pour l intervention.';
      return;
    }
    if (!this.interventionForm.typeMaintenanceId && !this.interventionForm.nouveauTypeLibelle.trim()) {
      this.errorMessage = 'Merci de choisir un type ou saisir un nouveau type.';
      return;
    }
    if (this.interventionForm.typeIntervention === 'EXTERNE' && !this.interventionForm.fournisseurNom.trim()) {
      this.errorMessage = 'Le fournisseur est obligatoire pour une intervention externe.';
      return;
    }
    if (this.selectedPieceId) {
      this.addPieceIntervention();
    }

    this.creatingIntervention = true;
    this.errorMessage = '';
    const dto = {
      machineId: this.machine.id,
      demandeMaintenanceId: this.interventionForm.demandeMaintenanceId || null,
      typeMaintenanceId: this.interventionForm.typeMaintenanceId || null,
      nouveauTypeLibelle: this.interventionForm.typeMaintenanceId ? null : this.interventionForm.nouveauTypeLibelle.trim(),
      description: this.interventionForm.description.trim(),
      note: this.interventionForm.note.trim(),
      piecesUtilisees: this.interventionForm.piecesUtilisees.trim(),
      pieces: this.piecesIntervention.map(piece => ({ pieceId: piece.pieceId, quantite: piece.quantite })),
      cout: this.interventionForm.cout,
      typeIntervention: this.interventionForm.typeIntervention,
      fournisseurNom: this.interventionForm.fournisseurNom.trim(),
      fournisseurContact: this.interventionForm.fournisseurContact.trim(),
      fournisseurTelephone: this.interventionForm.fournisseurTelephone.trim(),
      fournisseurEmail: this.interventionForm.fournisseurEmail.trim(),
      fournisseurAdresse: this.interventionForm.fournisseurAdresse.trim(),
      referenceDevis: this.interventionForm.referenceDevis.trim(),
      messageDemandeExterne: this.interventionForm.messageDemandeExterne.trim(),
      causePanne: this.interventionForm.causePanne.trim(),
      checklist: this.interventionForm.checklist.trim(),
      controleEffectue: this.interventionForm.controleEffectue,
      nettoyageEffectue: this.interventionForm.nettoyageEffectue,
      testFinalEffectue: this.interventionForm.testFinalEffectue,
      validationSignature: this.interventionForm.validationSignature.trim(),
      user: 'MAINTENANCE'
    };
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(dto)], { type: 'application/json' }));
    if (this.interventionImageFile) formData.append('image', this.interventionImageFile);
    if (this.interventionBeforeImageFile) formData.append('imageAvant', this.interventionBeforeImageFile);
    if (this.interventionAfterImageFile) formData.append('imageApres', this.interventionAfterImageFile);

    this.maintenanceService.createIntervention(formData).subscribe({
      next: () => {
        this.creatingIntervention = false;
        this.successMessage = 'Intervention enregistree.';
        this.resetInterventionForm();
        this.loadMaintenances(this.machine!.id);
        this.loadDemandes(this.machine!.id);
        this.loadPiecesCompatibles(this.machine!.id);
        this.loadIndicators(this.machine!.id);
      },
      error: err => {
        console.error(err);
        this.creatingIntervention = false;
        this.errorMessage = 'Erreur lors de la creation de l intervention.';
      }
    });
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

  onDemandeImageSelected(event: Event): void {
    this.demandeImageFile = this.firstFile(event);
  }

  onInterventionImageSelected(event: Event): void {
    this.interventionImageFile = this.firstFile(event);
  }

  onInterventionBeforeImageSelected(event: Event): void {
    this.interventionBeforeImageFile = this.firstFile(event);
  }

  onInterventionAfterImageSelected(event: Event): void {
    this.interventionAfterImageFile = this.firstFile(event);
  }

  openInterventionForDemande(demande: DemandeMaintenance): void {
    this.interventionForm.demandeMaintenanceId = demande.id;
    this.interventionForm.description = demande.description || '';
    if (demande.typeMaintenance?.id) {
      this.interventionForm.typeMaintenanceId = demande.typeMaintenance.id;
      this.interventionForm.nouveauTypeLibelle = '';
    }
  }

  createExternalDraft(): void {
    if (!this.machine || !this.externalDraft.fournisseurNom || !this.externalDraft.objet) {
      return;
    }
    this.interventionExterneService.create({
      machineId: this.machine.id,
      fournisseurNom: this.externalDraft.fournisseurNom,
      fournisseurContact: this.externalDraft.fournisseurContact,
      fournisseurTelephone: this.externalDraft.fournisseurTelephone,
      fournisseurEmail: this.externalDraft.fournisseurEmail,
      fournisseurAdresse: this.externalDraft.fournisseurAdresse,
      objet: this.externalDraft.objet,
      description: this.externalDraft.description,
      dateSouhaitee: this.externalDraft.dateSouhaitee ? `${this.externalDraft.dateSouhaitee}T00:00:00` : undefined
    }).subscribe({
      next: () => {
        this.externalDraft = {
          fournisseurNom: '',
          fournisseurContact: '',
          fournisseurTelephone: '',
          fournisseurEmail: '',
          fournisseurAdresse: '',
          objet: '',
          description: '',
          dateSouhaitee: ''
        };
        this.loadDemandesExternes(this.machine!.id);
      },
      error: err => console.error(err)
    });
  }

  updateDemandeStatut(
    demande: DemandeMaintenance,
    statut: StatutDemandeMaintenance,
    dateSouhaitee?: string
  ): void {
    if (!this.machine || demande.statut === statut) {
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
        this.loadIndicators(this.machine!.id);
      },
      error: err => {
        console.error(err);
        this.updatingDemandeId = undefined;
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
    if (!this.machine) {
      return;
    }
    this.updatingDemandeId = demande.id;
    this.demandeMaintenanceService.validerIntervention(demande.id).subscribe({
      next: updated => {
        this.demandes = this.demandes.map(item => item.id === updated.id ? updated : item);
        this.updatingDemandeId = undefined;
        this.loadIndicators(this.machine!.id);
      },
      error: err => {
        console.error(err);
        this.updatingDemandeId = undefined;
      }
    });
  }

  refuserIntervention(demande: DemandeMaintenance): void {
    if (!this.machine) {
      return;
    }
    this.updatingDemandeId = demande.id;
    this.demandeMaintenanceService.refuserIntervention(demande.id).subscribe({
      next: updated => {
        this.demandes = this.demandes.map(item => item.id === updated.id ? updated : item);
        this.updatingDemandeId = undefined;
        this.loadIndicators(this.machine!.id);
      },
      error: err => {
        console.error(err);
        this.updatingDemandeId = undefined;
      }
    });
  }

  retournerPiece(pieceUtilisee: any): void {
    if (!this.machine || !pieceUtilisee?.id) {
      return;
    }
    const form = this.retourPieceForms[pieceUtilisee.id];
    if (!form || form.quantite <= 0 || !form.cause.trim()) {
      return;
    }
    this.returningPieceId = pieceUtilisee.id;
    this.maintenanceService.retournerPiece(pieceUtilisee.id, {
      quantite: form.quantite,
      cause: form.cause.trim()
    }).subscribe({
      next: () => {
        form.cause = '';
        this.returningPieceId = undefined;
        this.loadMaintenances(this.machine!.id);
      },
      error: err => {
        console.error(err);
        this.returningPieceId = undefined;
      }
    });
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
    return `Retard: ${this.formatIndicatorDuration(diffMinutes)}`;
  }

  private toDateTimeLocalValue(value: string): string {
    const date = new Date(value);
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  getDefaultPlanificationDate(demande: DemandeMaintenance): string {
    if (!this.planificationDates[demande.id]) {
      const date = new Date(Date.now() + 60 * 60 * 1000);
      this.planificationDates[demande.id] = this.toDateTimeLocalValue(date.toISOString());
    }
    return this.planificationDates[demande.id];
  }


  formatIndicatorDuration(minutes: number): string {
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

  getAverageBetweenFailuresLabel(indicators: MachineMaintenanceIndicators): string {
    if (indicators.failureCount <= 0 || indicators.mtbfHours <= 0) {
      return 'Non calculable';
    }
    return `${indicators.mtbfHours.toFixed(1)} h`;
  }

  getAverageRepairTimeLabel(indicators: MachineMaintenanceIndicators): string {
    if (indicators.failureCount <= 0 || indicators.mttrHours <= 0) {
      return 'Non calculable';
    }
    return `${indicators.mttrHours.toFixed(1)} h`;
  }

  getAverageBetweenFailuresHelp(indicators: MachineMaintenanceIndicators): string {
    if (indicators.failureCount <= 0) {
      return 'Aucune panne detectee sur la periode.';
    }
    return `${indicators.failureCount} panne(s) utilisee(s) pour le calcul.`;
  }

  getAverageRepairTimeHelp(indicators: MachineMaintenanceIndicators): string {
    if (indicators.failureCount <= 0) {
      return 'Il faut au moins une panne terminee pour calculer ce temps.';
    }
    return `Base: ${this.formatIndicatorDuration(indicators.downtimeMinutes)} d'arret.`;
  }

  formatMoney(value?: number | null): string {
    return `${(value || 0).toFixed(2)} DH`;
  }

  getReplacementClasses(level?: string): string {
    switch (level) {
      case 'OK':
        return 'border-emerald-200 bg-emerald-50 text-emerald-800 dark:border-emerald-900 dark:bg-emerald-950/30 dark:text-emerald-200';
      case 'SURVEILLANCE':
        return 'border-sky-200 bg-sky-50 text-sky-800 dark:border-sky-900 dark:bg-sky-950/30 dark:text-sky-200';
      case 'ATTENTION':
        return 'border-amber-200 bg-amber-50 text-amber-800 dark:border-amber-900 dark:bg-amber-950/30 dark:text-amber-200';
      case 'CRITIQUE':
        return 'border-red-200 bg-red-50 text-red-800 dark:border-red-900 dark:bg-red-950/30 dark:text-red-200';
      default:
        return 'border-slate-200 bg-slate-50 text-slate-700 dark:border-slate-800 dark:bg-slate-950 dark:text-slate-200';
    }
  }

  onFilterChange(): void {
    if (!this.machine) return;
    this.loadActions(this.machine.id);
    this.loadDemandes(this.machine.id);
  }

  openMachineHistoryExport(): void {
    if (!this.machine) {
      return;
    }
    window.open(this.dashboardMaintenanceService.getMachineHistoryExportUrl(this.machine.id), '_blank');
  }

  openMachineHistoryReport(): void {
    if (!this.machine) {
      return;
    }
    window.open(this.dashboardMaintenanceService.getMachineHistoryReportUrl(this.machine.id), '_blank');
  }

  // Date affichee pour indiquer quand les donnees ont ete rafraichies.
  getMachineQrValue(): string {
    if (!this.machine) {
      return '';
    }
    return `CANAL-MACHINE-${this.machine.id}-${this.machine.ref || ''}`;
  }

  getMachineDetailUrl(): string {
    if (!this.machine) {
      return '';
    }
    return `${window.location.origin}/machinesD/${this.machine.id}`;
  }

  private updateLastUpdate(): void {
    if (!this.machine) {
      this.lastUpdate = undefined;
      return;
    }

    let last: Date | null = this.machine.dateDebutAction
      ? new Date(this.machine.dateDebutAction)
      : null;

    for (const a of this.actions) {
      const d1 = new Date(a.dateDebut);
      const d2 = a.dateFin ? new Date(a.dateFin) : this.now;
      if (!last || d1 > last) last = d1;
      if (d2 > last) last = d2;
    }

    this.lastUpdate = last || new Date(this.now);
  }

  private getRangeDates(): { from: Date; to: Date } {
    if (this.fromDate && this.toDate) {
      const from = new Date(this.fromDate + 'T00:00:00');
      const to = new Date(this.toDate + 'T23:59:59');
      return { from, to };
    }

    if (this.actions.length > 0) {
      let from = new Date(this.actions[0].dateDebut);
      let to = this.actions[0].dateFin ? new Date(this.actions[0].dateFin) : this.now;

      for (const a of this.actions) {
        const sd = new Date(a.dateDebut);
        const ed = a.dateFin ? new Date(a.dateFin) : this.now;
        if (sd < from) from = sd;
        if (ed > to) to = ed;
      }
      return { from, to };
    }

    const now = new Date();
    return { from: new Date(now.getTime() - 24 * 60 * 60 * 1000), to: now };
  }

  // Libelles et couleurs fournis par le type d'action.
  private getBaseColorFromTypeAction(ta?: TypeAction | null): string {
    const code = ta?.code || 'UNKNOWN';

    if (ta?.color) {
      return ta.color;
    }

    switch (code) {
      case 'MARCH': return '#22c55e';
      case 'STOP_NORMAL': return '#e5e7eb';
      case 'STOP_MAINTENANCE': return '#f97316';
      case 'MAINTENANCE_ELECTRIC': return '#38bdf8';
      case 'MAINTENANCE_MECANIQUE': return '#a855f7';
      default: return '#64748b';
    }
  }

  private getLabelFromTypeAction(ta?: TypeAction | null): string {
    return ta?.libelle || ta?.code || 'État inconnu';
  }

  getMachineStateLabel(machine: Machine): string {
    return this.getLabelFromTypeAction(machine.typeAction);
  }

  getMachineBadgeStyle(machine: Machine) {
    const base = this.getBaseColorFromTypeAction(machine.typeAction);
    const bg = base.length === 7 ? base + '33' : base;
    const border = base.length === 7 ? base + '88' : base;

    return {
      'background-color': bg,
      'border-color': border,
      'color': base
    };
  }

  getMachineDotStyle(machine: Machine) {
    const base = this.getBaseColorFromTypeAction(machine.typeAction);
    return { 'background-color': base };
  }

  // Chronometre de l'action en cours.
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

  durationToString(startIso: string, endIso: string | null): string {
    const start = new Date(startIso);
    const end = endIso ? new Date(endIso) : this.now;
    const diffMs = end.getTime() - start.getTime();
    if (diffMs < 0) return '0 min';

    const totalMinutes = Math.floor(diffMs / 60000);
    const h = Math.floor(totalMinutes / 60);
    const m = totalMinutes % 60;

    if (h === 0) return `${m} min`;
    return `${h}h ${m}min`;
  }

  // Calculs utilises par les cartes de statistiques.
  private computeStats(): void {
    if (!this.actions || this.actions.length === 0) {
      this.stats = [];
      return;
    }

    const { from, to } = this.getRangeDates();
    const totalWindowMs = to.getTime() - from.getTime();
    if (totalWindowMs <= 0) {
      this.stats = [];
      return;
    }

    const map = new Map<string, { durationMs: number; label: string; color: string }>();
    let totalUsed = 0;

    for (const a of this.actions) {
      const sd = new Date(a.dateDebut);
      const ed = a.dateFin ? new Date(a.dateFin) : this.now;
      const start = sd < from ? from : sd;
      const end = ed > to ? to : ed;
      const lenMs = end.getTime() - start.getTime();
      if (lenMs <= 0) continue;

      totalUsed += lenMs;

      const ta = a.typeAction;
      const code = ta?.code || 'UNKNOWN';
      const label = this.getLabelFromTypeAction(ta);
      const color = this.getBaseColorFromTypeAction(ta);

      if (!map.has(code)) {
        map.set(code, { durationMs: 0, label, color });
      }
      map.get(code)!.durationMs += lenMs;
    }

    if (totalUsed === 0) {
      this.stats = [];
      return;
    }

    const stats: StateStat[] = [];
    map.forEach((info, code) => {
      const percentage = (info.durationMs / totalUsed) * 100;
      stats.push({
        type: code,
        label: info.label,
        color: info.color,
        durationMs: info.durationMs,
        percentage
      });
    });

    this.stats = stats.sort((a, b) => b.percentage - a.percentage);
  }

  getConicStyle(pct: number, color: string) {
    const deg = pct * 3.6;
    return {
      background: `conic-gradient(${color} ${deg}deg, rgb(15 23 42) 0deg)`
    };
  }

  // Donnees preparees pour la timeline.
  private computeTimeline(): void {
    if (!this.actions || this.actions.length === 0) {
      this.timelineSegments = [];
      this.timelineFrom = undefined;
      this.timelineTo = undefined;
      return;
    }

    const { from, to } = this.getRangeDates();
    this.timelineFrom = from;
    this.timelineTo = to;

    const totalMs = to.getTime() - from.getTime();
    if (totalMs <= 0) {
      this.timelineSegments = [];
      return;
    }

    const segments: TimelineSegment[] = [];

    for (const a of this.actions) {
      const sd = new Date(a.dateDebut);
      const ed = a.dateFin ? new Date(a.dateFin) : this.now;
      const start = sd < from ? from : sd;
      const end = ed > to ? to : ed;
      const lenMs = end.getTime() - start.getTime();
      if (lenMs <= 0) continue;

      const widthPercent = (lenMs / totalMs) * 100;

      const ta = a.typeAction;
      const code = ta?.code || 'UNKNOWN';
      const label = this.getLabelFromTypeAction(ta);
      const color = this.getBaseColorFromTypeAction(ta);

      segments.push({
        type: code,
        label,
        color,
        widthPercent
      });
    }

    this.timelineSegments = segments;
  }

  getImage(): string {
    if (this.machine?.imagePath) {
      return this.machineService.getMachineImageUrl(this.machine);
    }
    return 'assets/machines/img.png';
  }

  getDemandeImageUrl(demande: any): string {
    return this.maintenanceService.getDemandeImageUrl(demande.id);
  }

  getMaintenanceImageUrl(maintenance: any): string {
    return this.maintenanceService.getImageUrl(maintenance.id);
  }

  getMaintenanceBeforeImageUrl(maintenance: any): string {
    return this.maintenanceService.getBeforeImageUrl(maintenance.id);
  }

  getMaintenanceAfterImageUrl(maintenance: any): string {
    return this.maintenanceService.getAfterImageUrl(maintenance.id);
  }

  // Libelle visuel du statut de demande.
  getStatutLabel(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'En attente';
      case 'PLANIFIEE': return 'Planifiée';
      case 'EN_COURS': return 'En cours';
      case 'EN_ATTENTE_VALIDATION': return 'En attente validation';
      case 'TERMINEE': return 'Terminée';
      case 'ANNULEE': return 'Annulée';
      default: return statut;
    }
  }

  getStatutBadgeClasses(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE':
        return 'bg-amber-500/10 text-amber-300 border border-amber-500/60';
      case 'PLANIFIEE':
        return 'bg-sky-500/10 text-sky-300 border border-sky-500/60';
      case 'EN_COURS':
        return 'bg-emerald-500/10 text-emerald-300 border border-emerald-500/60';
      case 'EN_ATTENTE_VALIDATION':
        return 'bg-violet-500/10 text-violet-300 border border-violet-500/60';
      case 'TERMINEE':
        return 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/60';
      case 'ANNULEE':
        return 'bg-red-500/10 text-red-300 border border-red-500/60';
      default:
        return 'bg-slate-700/20 text-slate-300 border border-slate-600/60';
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

  getPrioriteBadgeClasses(priorite?: string): string {
    switch (priorite) {
      case 'FAIBLE':
        return 'bg-slate-100 text-slate-600 border border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700';
      case 'URGENTE':
        return 'bg-amber-500/10 text-amber-700 border border-amber-400 dark:text-amber-200 dark:border-amber-800';
      case 'CRITIQUE':
        return 'bg-red-500/10 text-red-700 border border-red-400 dark:text-red-200 dark:border-red-900';
      default:
        return 'bg-sky-500/10 text-sky-700 border border-sky-300 dark:text-sky-200 dark:border-sky-800';
    }
  }

  private resetInterventionForm(): void {
    this.interventionForm = {
      demandeMaintenanceId: undefined,
      typeMaintenanceId: undefined,
      nouveauTypeLibelle: '',
      description: '',
      note: '',
      piecesUtilisees: '',
      cout: null,
      typeIntervention: 'INTERNE',
      fournisseurNom: '',
      fournisseurContact: '',
      fournisseurTelephone: '',
      fournisseurEmail: '',
      fournisseurAdresse: '',
      referenceDevis: '',
      messageDemandeExterne: '',
      causePanne: '',
      checklist: '',
      controleEffectue: false,
      nettoyageEffectue: false,
      testFinalEffectue: false,
      validationSignature: ''
    };
    this.piecesIntervention = [];
    this.selectedPieceId = undefined;
    this.selectedPieceQuantity = 1;
    this.interventionImageFile = undefined;
    this.interventionBeforeImageFile = undefined;
    this.interventionAfterImageFile = undefined;
  }

  private firstFile(event: Event): File | undefined {
    const input = event.target as HTMLInputElement;
    return input.files && input.files.length > 0 ? input.files[0] : undefined;
  }
}
