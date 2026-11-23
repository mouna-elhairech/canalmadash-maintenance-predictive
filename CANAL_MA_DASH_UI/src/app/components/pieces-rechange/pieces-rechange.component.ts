import { Component, OnInit } from '@angular/core';
import {
  DemandePieceRechange,
  PieceRechange,
  StatutDemandePiece
} from '../../models/piece-rechange.model';
import { PieceRechangeService } from '../../services/piece-rechange.service';
import { Machine } from '../../models/machine.model';
import { MachineService } from '../../services/machine.service';

@Component({
  selector: 'app-pieces-rechange',
  templateUrl: './pieces-rechange.component.html'
})
export class PiecesRechangeComponent implements OnInit {
  pieces: PieceRechange[] = [];
  demandes: DemandePieceRechange[] = [];
  machines: Machine[] = [];
  loading = false;
  editingId?: number;
  selectedMouvementsPieceId?: number;
  mouvements: any[] = [];

  form: PieceRechange = this.emptyForm();
  entreeForm = {
    pieceId: undefined as number | undefined,
    quantite: 1,
    prixTotal: 0,
    commentaire: 'Entree stock'
  };
  demandeForm = {
    pieceId: undefined as number | undefined,
    quantiteDemandee: 1,
    motif: 'Reapprovisionnement'
  };
  receptionPrices: Record<number, number | null> = {};

  constructor(
    private service: PieceRechangeService,
    private machineService: MachineService
  ) {}

  ngOnInit(): void {
    this.loadPieces();
    this.loadDemandes();
    this.loadMachines();
  }

  loadPieces(): void {
    this.loading = true;
    this.service.findAll().subscribe({
      next: pieces => {
        this.pieces = pieces;
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  loadDemandes(): void {
    this.service.findDemandes().subscribe({
      next: demandes => {
        this.demandes = demandes;
        for (const demande of demandes) {
          if (this.receptionPrices[demande.id] == null) {
            this.receptionPrices[demande.id] = demande.prixTotalReception ?? this.defaultReceptionPrice(demande);
          }
        }
      },
      error: err => console.error(err)
    });
  }

  loadMachines(): void {
    this.machineService.getMachines().subscribe({
      next: machines => this.machines = machines,
      error: err => console.error(err)
    });
  }

  save(): void {
    if (!this.form.reference || !this.form.libelle) {
      return;
    }

    const payload: PieceRechange = {
      ...this.form,
      machineIds: this.form.standard ? [] : this.form.machineIds || []
    };

    const request = this.editingId
      ? this.service.update(this.editingId, payload)
      : this.service.create(payload);

    request.subscribe({
      next: () => {
        this.resetForm();
        this.loadPieces();
        this.loadDemandes();
      },
      error: err => console.error(err)
    });
  }

  edit(piece: PieceRechange): void {
    this.editingId = piece.id;
    this.form = {
      ...piece,
      standard: piece.standard !== false,
      machineIds: piece.machineIds || []
    };
  }

  ajouterStock(): void {
    if (!this.entreeForm.pieceId || this.entreeForm.quantite <= 0 || this.entreeForm.prixTotal < 0) {
      return;
    }
    this.service.ajouterStock(this.entreeForm.pieceId, {
      quantite: this.entreeForm.quantite,
      prixTotal: this.entreeForm.prixTotal,
      commentaire: this.entreeForm.commentaire
    }).subscribe({
      next: () => {
        this.entreeForm = { pieceId: undefined, quantite: 1, prixTotal: 0, commentaire: 'Entree stock' };
        this.loadPieces();
        this.loadDemandes();
      },
      error: err => console.error(err)
    });
  }

  loadMouvements(piece: PieceRechange): void {
    if (!piece.id) {
      return;
    }
    if (this.selectedMouvementsPieceId === piece.id) {
      this.selectedMouvementsPieceId = undefined;
      this.mouvements = [];
      return;
    }
    this.selectedMouvementsPieceId = piece.id;
    this.service.findMouvements(piece.id).subscribe({
      next: mouvements => this.mouvements = mouvements,
      error: err => console.error(err)
    });
  }

  onStandardChange(): void {
    if (this.form.standard) {
      this.form.machineIds = [];
    }
  }

  isMachineSelected(machineId: number): boolean {
    return (this.form.machineIds || []).includes(machineId);
  }

  toggleMachine(machineId: number, checked: boolean): void {
    const ids = new Set(this.form.machineIds || []);
    if (checked) {
      ids.add(machineId);
    } else {
      ids.delete(machineId);
    }
    this.form.machineIds = Array.from(ids);
  }

  selectAllMachines(): void {
    this.form.standard = false;
    this.form.machineIds = this.machines.map(machine => machine.id);
  }

  clearMachineSelection(): void {
    this.form.machineIds = [];
  }

  getMachineNames(piece: PieceRechange): string {
    if (piece.standard !== false) {
      return 'Toutes les machines';
    }
    const ids = piece.machineIds || [];
    if (ids.length === 0) {
      return 'Aucune machine';
    }
    return this.machines
      .filter(machine => ids.includes(machine.id))
      .map(machine => machine.nom || machine.ref || `Machine ${machine.id}`)
      .join(', ');
  }

  remove(piece: PieceRechange): void {
    if (!piece.id) {
      return;
    }
    this.service.delete(piece.id).subscribe({
      next: () => this.loadPieces(),
      error: err => console.error(err)
    });
  }

  createDemande(): void {
    if (!this.demandeForm.pieceId || this.demandeForm.quantiteDemandee <= 0) {
      return;
    }
    this.service.createDemande({
      pieceId: this.demandeForm.pieceId,
      quantiteDemandee: this.demandeForm.quantiteDemandee,
      motif: this.demandeForm.motif
    }).subscribe({
      next: () => {
        this.demandeForm = { pieceId: undefined, quantiteDemandee: 1, motif: 'Reapprovisionnement' };
        this.loadDemandes();
      },
      error: err => console.error(err)
    });
  }

  updateDemandeStatut(demande: DemandePieceRechange, statut: StatutDemandePiece): void {
    const prixTotalReception = statut === 'RECUE'
      ? this.receptionPrices[demande.id] ?? this.defaultReceptionPrice(demande)
      : undefined;
    this.service.updateDemandeStatut(demande.id, statut, prixTotalReception).subscribe({
      next: () => {
        this.loadDemandes();
        this.loadPieces();
      },
      error: err => console.error(err)
    });
  }

  getStatutLabel(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'En attente';
      case 'COMMANDEE': return 'Commandee';
      case 'RECUE': return 'Recue';
      case 'ANNULEE': return 'Annulee';
      default: return statut;
    }
  }

  resetForm(): void {
    this.editingId = undefined;
    this.form = this.emptyForm();
  }

  stockValue(piece: PieceRechange): number {
    return piece.valeurStock ?? ((piece.quantite || 0) * (piece.coutUnitaire || 0));
  }

  defaultReceptionPrice(demande: DemandePieceRechange): number {
    return (demande.quantiteDemandee || 0) * (demande.piece?.coutUnitaire || 0);
  }

  private emptyForm(): PieceRechange {
    return {
      reference: '',
      libelle: '',
      quantite: 0,
      seuilMinimum: 1,
      coutUnitaire: 0,
      valeurStock: 0,
      standard: true,
      machineIds: []
    };
  }
}
