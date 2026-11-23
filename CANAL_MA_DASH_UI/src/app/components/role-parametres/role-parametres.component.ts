import { Component, OnInit } from '@angular/core';
import {
  CategorieFonctionnalite,
  FonctionnaliteApplication,
  FonctionnaliteApplicationRequest,
  RoleApplication,
  RoleApplicationRequest,
  RoleApplicationService
} from '../../services/role-application.service';

@Component({
  selector: 'app-role-parametres',
  templateUrl: './role-parametres.component.html'
})
export class RoleParametresComponent implements OnInit {
  roles: RoleApplication[] = [];
  fonctionnalites: FonctionnaliteApplication[] = [];
  selectedRole?: RoleApplication;
  selectedFonctionnalite?: FonctionnaliteApplication;
  roleForm: RoleApplicationRequest = this.emptyRole();
  fonctionnaliteForm: FonctionnaliteApplicationRequest = this.emptyFonctionnalite();
  loading = false;
  savingRole = false;
  savingFonctionnalite = false;
  message = '';
  errorMessage = '';
  categories: CategorieFonctionnalite[] = ['PAGE', 'ACTION'];

  constructor(private roleService: RoleApplicationService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.message = '';
    this.errorMessage = '';
    this.roleService.getFonctionnalites().subscribe({
      next: fonctionnalites => {
        this.fonctionnalites = fonctionnalites;
        this.roleService.getRoles().subscribe({
          next: roles => {
            this.roles = roles;
            this.loading = false;
            if (!this.selectedRole && roles.length) {
              this.selectRole(roles[0]);
            } else if (this.selectedRole) {
              const refreshed = roles.find(role => role.id === this.selectedRole?.id);
              if (refreshed) this.selectRole(refreshed);
            }
          },
          error: err => this.fail(err, 'Impossible de charger les roles.')
        });
      },
      error: err => this.fail(err, 'Impossible de charger les fonctionnalites.')
    });
  }

  selectRole(role: RoleApplication): void {
    this.selectedRole = role;
    this.roleForm = {
      code: role.code,
      libelle: role.libelle,
      objectif: role.objectif || '',
      actif: role.actif,
      fonctionnaliteIds: (role.fonctionnalites || []).map(item => item.id)
    };
  }

  newRole(): void {
    this.selectedRole = undefined;
    this.roleForm = this.emptyRole();
  }

  saveRole(): void {
    if (!this.roleForm.code.trim() || !this.roleForm.libelle.trim()) {
      this.errorMessage = 'Le code et le libelle du role sont obligatoires.';
      return;
    }
    this.savingRole = true;
    this.message = '';
    this.errorMessage = '';
    const request = this.normalizeRoleRequest();
    const action = this.selectedRole
      ? this.roleService.updateRole(this.selectedRole.id, request)
      : this.roleService.createRole(request);
    action.subscribe({
      next: role => {
        this.savingRole = false;
        this.message = 'Role enregistre dans la base de donnees.';
        this.selectedRole = role;
        this.load();
      },
      error: err => {
        console.error(err);
        this.savingRole = false;
        this.errorMessage = 'Erreur lors de l enregistrement du role. Verifie que le code est unique.';
      }
    });
  }

  toggleFonctionnalite(id: number, checked: boolean): void {
    const ids = new Set(this.roleForm.fonctionnaliteIds);
    checked ? ids.add(id) : ids.delete(id);
    this.roleForm.fonctionnaliteIds = Array.from(ids);
  }

  hasFonctionnalite(id: number): boolean {
    return this.roleForm.fonctionnaliteIds.includes(id);
  }

  byCategorie(categorie: CategorieFonctionnalite): FonctionnaliteApplication[] {
    return this.fonctionnalites.filter(item => item.categorie === categorie);
  }

  selectFonctionnalite(item: FonctionnaliteApplication): void {
    this.selectedFonctionnalite = item;
    this.fonctionnaliteForm = {
      code: item.code,
      libelle: item.libelle,
      description: item.description || '',
      route: item.route || '',
      categorie: item.categorie,
      actif: item.actif
    };
  }

  newFonctionnalite(): void {
    this.selectedFonctionnalite = undefined;
    this.fonctionnaliteForm = this.emptyFonctionnalite();
  }

  saveFonctionnalite(): void {
    if (!this.fonctionnaliteForm.code.trim() || !this.fonctionnaliteForm.libelle.trim()) {
      this.errorMessage = 'Le code et le libelle de la fonctionnalite sont obligatoires.';
      return;
    }
    this.savingFonctionnalite = true;
    this.message = '';
    this.errorMessage = '';
    const request = this.normalizeFonctionnaliteRequest();
    const action = this.selectedFonctionnalite
      ? this.roleService.updateFonctionnalite(this.selectedFonctionnalite.id, request)
      : this.roleService.createFonctionnalite(request);
    action.subscribe({
      next: fonctionnalite => {
        this.savingFonctionnalite = false;
        this.message = 'Fonctionnalite enregistree dans la base de donnees.';
        this.selectedFonctionnalite = fonctionnalite;
        this.load();
      },
      error: err => {
        console.error(err);
        this.savingFonctionnalite = false;
        this.errorMessage = 'Erreur lors de l enregistrement de la fonctionnalite. Verifie que le code est unique.';
      }
    });
  }

  resetDefaults(): void {
    this.loading = true;
    this.roleService.initialiser().subscribe({
      next: () => {
        this.message = 'Roles et fonctionnalites par defaut synchronises.';
        this.loading = false;
        this.load();
      },
      error: err => this.fail(err, 'Erreur lors de la reinitialisation des roles.')
    });
  }

  private normalizeRoleRequest(): RoleApplicationRequest {
    return {
      ...this.roleForm,
      code: this.roleForm.code.trim().toUpperCase(),
      libelle: this.roleForm.libelle.trim(),
      objectif: this.roleForm.objectif?.trim() || '',
      fonctionnaliteIds: this.roleForm.fonctionnaliteIds || []
    };
  }

  private normalizeFonctionnaliteRequest(): FonctionnaliteApplicationRequest {
    return {
      ...this.fonctionnaliteForm,
      code: this.fonctionnaliteForm.code.trim().toUpperCase(),
      libelle: this.fonctionnaliteForm.libelle.trim(),
      description: this.fonctionnaliteForm.description?.trim() || '',
      route: this.fonctionnaliteForm.route?.trim() || '',
      categorie: this.fonctionnaliteForm.categorie || 'ACTION'
    };
  }

  private emptyRole(): RoleApplicationRequest {
    return { code: '', libelle: '', objectif: '', actif: true, fonctionnaliteIds: [] };
  }

  private emptyFonctionnalite(): FonctionnaliteApplicationRequest {
    return { code: '', libelle: '', description: '', route: '', categorie: 'ACTION', actif: true };
  }

  private fail(err: unknown, message: string): void {
    console.error(err);
    this.loading = false;
    this.savingRole = false;
    this.savingFonctionnalite = false;
    this.errorMessage = message;
  }
}
