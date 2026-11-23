import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MachineService } from '../../services/machine.service';
import { Machine } from '../../models/machine.model';
import { TypeActionService } from '../../services/type-action.service';
import {TypeAction} from "../../models/TypeAction";
import { TypeMaintenance } from '../../models/TypeMaintenance';
import { TypeMaintenanceService } from '../../services/type-maintenance.service';
import { ReglePreventiveService, UniteControlePeriodique } from '../../services/regle-preventive.service';

@Component({
  selector: 'app-machine-form',
  templateUrl: './machine-form.component.html'
})
export class MachineFormComponent implements OnInit {

  isEdit = false;
  machineId?: number;

  nom = '';
  ref = '';
  emplacement = '';
  fabricant = '';
  modele = '';
  numeroSerie = '';
  dateInstallation = '';
  criticite = 'NORMALE';
  heuresMarche: number | null = null;
  coutAchat: number | null = null;
  coutInstallation: number | null = null;
  coutTransport: number | null = null;
  coutFormation: number | null = null;
  coutMiseEnService: number | null = null;
  seuilDisponibiliteAttentionPercent: number | null = 85;
  seuilDisponibiliteCritiquePercent: number | null = 70;
  seuilTempsArretAttentionMinutes: number | null = 120;
  seuilTempsArretCritiqueMinutes: number | null = 480;
  seuilPannesAttention: number | null = 3;
  seuilPannesCritique: number | null = 6;
  seuilTempsReparationAttentionHours: number | null = 4;
  seuilTempsReparationCritiqueHours: number | null = 8;
  seuilRemplacementSurveillancePercent: number | null = 40;
  seuilRemplacementAttentionPercent: number | null = 70;
  seuilRemplacementCritiquePercent: number | null = 100;
  commentaireTechnique = '';

  typeActionId: number | null = null;

  types: TypeAction[] = [];
  typeMaintenances: TypeMaintenance[] = [];

  createControlePeriodique = false;
  controleTypeMaintenanceId?: number;
  controleFrequence = 1;
  controleUnite: UniteControlePeriodique = 'MOIS';
  controleDateDebut = new Date().toISOString().substring(0, 10);
  controleDescription = '';
  controleUnits: UniteControlePeriodique[] = ['JOUR', 'SEMAINE', 'MOIS', 'ANNEE'];

  selectedFile?: File;

  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private machineService: MachineService,
    private typeActionService: TypeActionService,
    private typeMaintenanceService: TypeMaintenanceService,
    private reglePreventiveService: ReglePreventiveService
  ) {}

  ngOnInit(): void {
    this.loadTypes();
    this.loadTypeMaintenances();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEdit = this.route.snapshot.routeConfig?.path?.includes('edit') ?? false;
      this.machineId = Number(idParam);
      if (this.isEdit) {
        this.loadMachine(this.machineId);
      }
    }
  }

  loadTypes(): void {
    this.typeActionService.findActifs().subscribe({
      next: types => {
        this.types = types;
      },
      error: err => {
        console.error(err);
        this.errorMessage = 'Erreur lors du chargement des types d’action.';
      }
    });
  }

  loadTypeMaintenances(): void {
    this.typeMaintenanceService.findActifs().subscribe({
      next: types => this.typeMaintenances = types,
      error: err => {
        console.error(err);
        this.errorMessage = 'Erreur lors du chargement des types de maintenance.';
      }
    });
  }

  loadMachine(id: number): void {
    this.machineService.getMachine(id).subscribe({
      next: m => {
        this.nom = m.nom;
        this.ref = m.ref;
        this.typeActionId = m.typeAction?.id ?? null;
        this.emplacement = m.emplacement || '';
        this.fabricant = m.fabricant || '';
        this.modele = m.modele || '';
        this.numeroSerie = m.numeroSerie || '';
        this.dateInstallation = m.dateInstallation || '';
        this.criticite = m.criticite || 'NORMALE';
        this.heuresMarche = m.heuresMarche ?? null;
        this.coutAchat = m.coutAchat ?? null;
        this.coutInstallation = m.coutInstallation ?? null;
        this.coutTransport = m.coutTransport ?? null;
        this.coutFormation = m.coutFormation ?? null;
        this.coutMiseEnService = m.coutMiseEnService ?? null;
        this.seuilDisponibiliteAttentionPercent = m.seuilDisponibiliteAttentionPercent ?? 85;
        this.seuilDisponibiliteCritiquePercent = m.seuilDisponibiliteCritiquePercent ?? 70;
        this.seuilTempsArretAttentionMinutes = m.seuilTempsArretAttentionMinutes ?? 120;
        this.seuilTempsArretCritiqueMinutes = m.seuilTempsArretCritiqueMinutes ?? 480;
        this.seuilPannesAttention = m.seuilPannesAttention ?? 3;
        this.seuilPannesCritique = m.seuilPannesCritique ?? 6;
        this.seuilTempsReparationAttentionHours = m.seuilTempsReparationAttentionHours ?? 4;
        this.seuilTempsReparationCritiqueHours = m.seuilTempsReparationCritiqueHours ?? 8;
        this.seuilRemplacementSurveillancePercent = m.seuilRemplacementSurveillancePercent ?? 40;
        this.seuilRemplacementAttentionPercent = m.seuilRemplacementAttentionPercent ?? 70;
        this.seuilRemplacementCritiquePercent = m.seuilRemplacementCritiquePercent ?? 100;
        this.commentaireTechnique = m.commentaireTechnique || '';
      },
      error: err => {
        console.error(err);
        this.errorMessage = 'Erreur lors du chargement de la machine.';
      }
    });
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    } else {
      this.selectedFile = undefined;
    }
  }

  onSubmit(): void {
    if (!this.nom || !this.ref || !this.typeActionId) {
      this.errorMessage = 'Merci de remplir tous les champs obligatoires.';
      return;
    }
    if (this.createControlePeriodique && (!this.controleTypeMaintenanceId || !this.controleFrequence || !this.controleDateDebut)) {
      this.errorMessage = 'Merci de completer le controle periodique ou de le desactiver.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const formData = new FormData();
    formData.append('nom', this.nom);
    formData.append('ref', this.ref);
    // Le backend attend l'identifiant du type d'action.
    formData.append('typeActionId', String(this.typeActionId));
    formData.append('emplacement', this.emplacement || '');
    formData.append('fabricant', this.fabricant || '');
    formData.append('modele', this.modele || '');
    formData.append('numeroSerie', this.numeroSerie || '');
    formData.append('dateInstallation', this.dateInstallation || '');
    formData.append('criticite', this.criticite || '');
    if (this.heuresMarche != null) {
      formData.append('heuresMarche', String(this.heuresMarche));
    }
    this.appendOptionalNumber(formData, 'coutAchat', this.coutAchat);
    this.appendOptionalNumber(formData, 'coutInstallation', this.coutInstallation);
    this.appendOptionalNumber(formData, 'coutTransport', this.coutTransport);
    this.appendOptionalNumber(formData, 'coutFormation', this.coutFormation);
    this.appendOptionalNumber(formData, 'coutMiseEnService', this.coutMiseEnService);
    this.appendOptionalNumber(formData, 'seuilDisponibiliteAttentionPercent', this.seuilDisponibiliteAttentionPercent);
    this.appendOptionalNumber(formData, 'seuilDisponibiliteCritiquePercent', this.seuilDisponibiliteCritiquePercent);
    this.appendOptionalNumber(formData, 'seuilTempsArretAttentionMinutes', this.seuilTempsArretAttentionMinutes);
    this.appendOptionalNumber(formData, 'seuilTempsArretCritiqueMinutes', this.seuilTempsArretCritiqueMinutes);
    this.appendOptionalNumber(formData, 'seuilPannesAttention', this.seuilPannesAttention);
    this.appendOptionalNumber(formData, 'seuilPannesCritique', this.seuilPannesCritique);
    this.appendOptionalNumber(formData, 'seuilTempsReparationAttentionHours', this.seuilTempsReparationAttentionHours);
    this.appendOptionalNumber(formData, 'seuilTempsReparationCritiqueHours', this.seuilTempsReparationCritiqueHours);
    this.appendOptionalNumber(formData, 'seuilRemplacementSurveillancePercent', this.seuilRemplacementSurveillancePercent);
    this.appendOptionalNumber(formData, 'seuilRemplacementAttentionPercent', this.seuilRemplacementAttentionPercent);
    this.appendOptionalNumber(formData, 'seuilRemplacementCritiquePercent', this.seuilRemplacementCritiquePercent);
    formData.append('commentaireTechnique', this.commentaireTechnique || '');

    if (this.selectedFile) {
      formData.append('image', this.selectedFile);
    }

    if (this.isEdit && this.machineId != null) {
      this.machineService.updateMachine(this.machineId, formData).subscribe({
        next: machine => this.createControleIfNeeded(machine),
        error: err => {
          console.error(err);
          this.loading = false;
          this.errorMessage = 'Erreur lors de la modification.';
        }
      });
    } else {
      this.machineService.createMachine(formData).subscribe({
        next: machine => this.createControleIfNeeded(machine),
        error: err => {
          console.error(err);
          this.loading = false;
          this.errorMessage = 'Erreur lors de la création.';
        }
      });
    }
  }

  createControleIfNeeded(machine: Machine): void {
    if (!this.createControlePeriodique || !this.controleTypeMaintenanceId) {
      this.loading = false;
      this.router.navigate(['/machinesD', machine.id]);
      return;
    }

    this.reglePreventiveService.create({
      machineId: machine.id,
      typeMaintenanceId: this.controleTypeMaintenanceId,
      typeRegle: 'CONTROLE_PERIODIQUE',
      frequence: this.controleFrequence,
      unite: this.controleUnite,
      dateDebut: `${this.controleDateDebut}T00:00:00`,
      description: this.controleDescription
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/machinesD', machine.id]);
      },
      error: err => {
        console.error(err);
        this.loading = false;
        this.errorMessage = 'Machine enregistree, mais erreur lors de la creation du controle periodique.';
      }
    });
  }

  unitLabel(unit: UniteControlePeriodique): string {
    switch (unit) {
      case 'JOUR': return 'jour(s)';
      case 'SEMAINE': return 'semaine(s)';
      case 'MOIS': return 'mois';
      case 'ANNEE': return 'annee(s)';
      default: return unit;
    }
  }

  totalInvestissement(): number {
    return (this.coutAchat || 0)
      + (this.coutInstallation || 0)
      + (this.coutTransport || 0)
      + (this.coutFormation || 0)
      + (this.coutMiseEnService || 0);
  }

  private appendOptionalNumber(formData: FormData, key: string, value: number | null): void {
    if (value != null) {
      formData.append(key, String(value));
    }
  }
}
