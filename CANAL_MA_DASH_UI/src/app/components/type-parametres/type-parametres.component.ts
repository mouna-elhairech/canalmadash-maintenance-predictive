import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TypeActionService } from '../../services/type-action.service';
import { TypeMaintenanceService } from '../../services/type-maintenance.service';
import {TypeAction} from "../../models/TypeAction";
import {TypeMaintenance} from "../../models/TypeMaintenance";

type ModalTarget = 'ACTION' | 'MAINTENANCE';
type ModalMode = 'CREATE' | 'EDIT';

@Component({
  selector: 'app-type-parametres',
  templateUrl: './type-parametres.component.html'
})
export class TypeParametresComponent implements OnInit {

  // Donnees affichees dans les deux listes.
  typeActions: TypeAction[] = [];
  typeMaintenances: TypeMaintenance[] = [];

  loading = false;

  // Etat du formulaire ouvert dans le modal.
  isModalOpen = false;
  modalTarget: ModalTarget = 'ACTION';
  modalMode: ModalMode = 'CREATE';
  modalTitle = '';

  form!: FormGroup;
  currentId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private typeActionService: TypeActionService,
    private typeMaintenanceService: TypeMaintenanceService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm() {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(50)]],
      libelle: ['', [Validators.required, Validators.maxLength(150)]],
      actif: [true],
      color: ['#22c55e', [Validators.required]]   // Couleur initiale.
    });
  }


  loadData() {
    this.loading = true;
    this.typeActionService.findAll().subscribe({
      next: actions => {
        this.typeActions = actions;
        this.typeMaintenanceService.findAll().subscribe({
          next: maints => {
            this.typeMaintenances = maints;
            this.loading = false;
          },
          error: err => {
            console.error(err);
            this.loading = false;
          }
        });
      },
      error: err => {
        console.error(err);
        this.loading = false;
      }
    });
  }


  openAddAction() {
    this.openModal('ACTION', 'CREATE');
  }

  openEditAction(item: TypeAction) {
    this.openModal('ACTION', 'EDIT', item);
  }

  openAddMaintenance() {
    this.openModal('MAINTENANCE', 'CREATE');
  }

  openEditMaintenance(item: TypeMaintenance) {
    this.openModal('MAINTENANCE', 'EDIT', item);
  }

  private openModal(target: ModalTarget, mode: ModalMode, data?: any) {
    this.modalTarget = target;
    this.modalMode = mode;
    this.isModalOpen = true;

    if (mode === 'CREATE') {
      this.modalTitle = target === 'ACTION'
        ? 'Ajouter un type d’action'
        : 'Ajouter un type de maintenance';
      this.currentId = null;
      this.form.reset({
        code: '',
        libelle: '',
        actif: true,
        color: '#22c55e'
      });
    } else {
      this.modalTitle = target === 'ACTION'
        ? 'Modifier un type d’action'
        : 'Modifier un type de maintenance';

      this.currentId = data.id;
      this.form.reset({
        code: data.code,
        libelle: data.libelle,
        actif: data.actif,
        color: data.color || '#22c55e'
      });
    }
  }

  closeModal() {
    this.isModalOpen = false;
  }

  // Cree ou met a jour le type selon le mode du formulaire.

  onSubmit() {
    if (this.form.invalid) return;

    const payload = this.form.value;

    if (this.modalTarget === 'ACTION') {
      if (this.modalMode === 'CREATE') {
        this.typeActionService.create(payload).subscribe({
          next: () => {
            this.closeModal();
            this.loadData();
          },
          error: err => console.error(err)
        });
      } else if (this.modalMode === 'EDIT' && this.currentId !== null) {
        this.typeActionService.update(this.currentId, payload).subscribe({
          next: () => {
            this.closeModal();
            this.loadData();
          },
          error: err => console.error(err)
        });
      }
    } else { // Type de maintenance.
      if (this.modalMode === 'CREATE') {
        this.typeMaintenanceService.create(payload).subscribe({
          next: () => {
            this.closeModal();
            this.loadData();
          },
          error: err => console.error(err)
        });
      } else if (this.modalMode === 'EDIT' && this.currentId !== null) {
        this.typeMaintenanceService.update(this.currentId, payload).subscribe({
          next: () => {
            this.closeModal();
            this.loadData();
          },
          error: err => console.error(err)
        });
      }
    }
  }

  // Actions de changement d'etat et de suppression.

  toggleActifAction(item: TypeAction) {
    const call = item.actif
      ? this.typeActionService.disable(item.id)
      : this.typeActionService.enable(item.id);

    call.subscribe({
      next: () => this.loadData(),
      error: err => console.error(err)
    });
  }

  deleteAction(item: TypeAction) {
    if (!confirm(`Supprimer le type d’action "${item.libelle}" ?`)) return;
    this.typeActionService.delete(item.id).subscribe({
      next: () => this.loadData(),
      error: err => console.error(err)
    });
  }

  toggleActifMaintenance(item: TypeMaintenance) {
    const call = item.actif
      ? this.typeMaintenanceService.disable(item.id)
      : this.typeMaintenanceService.enable(item.id);

    call.subscribe({
      next: () => this.loadData(),
      error: err => console.error(err)
    });
  }

  deleteMaintenance(item: TypeMaintenance) {
    if (!confirm(`Supprimer le type de maintenance "${item.libelle}" ?`)) return;
    this.typeMaintenanceService.delete(item.id).subscribe({
      next: () => this.loadData(),
      error: err => console.error(err)
    });
  }

}
