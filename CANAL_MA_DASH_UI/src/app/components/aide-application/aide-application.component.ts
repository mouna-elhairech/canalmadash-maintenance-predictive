import { Component, OnInit } from '@angular/core';
import { RoleApplication, RoleApplicationService } from '../../services/role-application.service';

interface HelpSection {
  title: string;
  subtitle: string;
  points: string[];
}

@Component({
  selector: 'app-aide-application',
  templateUrl: './aide-application.component.html'
})
export class AideApplicationComponent implements OnInit {
  rolesDb: RoleApplication[] = [];
  loadingRoles = false;
  roleErrorMessage = '';

  constructor(private roleService: RoleApplicationService) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.loadingRoles = true;
    this.roleErrorMessage = '';
    this.roleService.getRoles().subscribe({
      next: roles => {
        this.rolesDb = roles;
        this.loadingRoles = false;
      },
      error: err => {
        console.error(err);
        this.roleErrorMessage = 'Impossible de charger les roles depuis la base de donnees.';
        this.loadingRoles = false;
      }
    });
  }

  initialiserRoles(): void {
    this.loadingRoles = true;
    this.roleService.initialiser().subscribe({
      next: () => this.loadRoles(),
      error: err => {
        console.error(err);
        this.roleErrorMessage = 'Erreur lors de l initialisation des roles.';
        this.loadingRoles = false;
      }
    });
  }

  pagesForRole(role: RoleApplication) {
    return (role.fonctionnalites || []).filter(item => item.categorie === 'PAGE');
  }

  actionsForRole(role: RoleApplication) {
    return (role.fonctionnalites || []).filter(item => item.categorie === 'ACTION');
  }

  sections: HelpSection[] = [
    {
      title: 'Dashboard maintenance',
      subtitle: 'Vue globale pour comprendre l etat du parc machines.',
      points: [
        'Affiche les indicateurs principaux : disponibilite, temps d arret, pannes, couts et retards.',
        'Montre les machines qui demandent une attention particuliere.',
        'Donne acces au calendrier, aux rapports PDF et aux exports.'
      ]
    },
    {
      title: 'Machines',
      subtitle: 'Gestion et consultation du parc machines.',
      points: [
        'Chaque machine possede une reference, un etat actuel, un emplacement, des couts et des seuils.',
        'La fiche machine affiche l historique, les indicateurs, les demandes et les interventions.',
        'Le changement d etat permet de suivre marche, arret, panne ou maintenance.'
      ]
    },
    {
      title: 'Demandes de maintenance',
      subtitle: 'Suivi des besoins avant intervention.',
      points: [
        'Une demande peut etre en attente, planifiee, en cours, terminee ou annulee.',
        'La priorite aide a classer les urgences : faible, normale, urgente ou critique.',
        'Une date planifiee permet de detecter automatiquement les retards.'
      ]
    },
    {
      title: 'Interventions',
      subtitle: 'Enregistrement du travail realise sur une machine.',
      points: [
        'Une intervention peut etre interne ou externe.',
        'Elle peut etre liee a une demande existante.',
        'Elle contient description, cause, cout, checklist, photos avant/apres et pieces utilisees.'
      ]
    },
    {
      title: 'Pieces de rechange',
      subtitle: 'Gestion du stock et des pieces compatibles.',
      points: [
        'Une piece peut etre standard ou compatible avec une ou plusieurs machines.',
        'Quand une piece est utilisee dans une intervention, le stock diminue automatiquement.',
        'Le retour stock permet d annuler ou retourner une quantite avec une cause.'
      ]
    },
    {
      title: 'Kanban maintenance',
      subtitle: 'Pilotage visuel des demandes.',
      points: [
        'Les demandes sont separees par statut.',
        'Le glisser-deposer permet de changer rapidement le statut.',
        'Les filtres permettent de rechercher par machine, priorite, statut, type, date, retard et cout.'
      ]
    },
    {
      title: 'Calendrier',
      subtitle: 'Planning des demandes et controles periodiques.',
      points: [
        'Le calendrier affiche les demandes planifiees.',
        'Il affiche aussi les controles periodiques a venir.',
        'Les cases a cocher permettent de choisir ce qui doit apparaitre.'
      ]
    },
    {
      title: 'Controle periodique',
      subtitle: 'Maintenance preventive simple par machine.',
      points: [
        'Une regle peut creer une demande automatiquement selon une frequence.',
        'Une seule demande ouverte est creee a la fois pour la meme regle.',
        'Si la demande precedente n est pas terminee ou annulee, aucune nouvelle demande n est generee.'
      ]
    },
    {
      title: 'Score criticite',
      subtitle: 'Aide a identifier les machines a surveiller.',
      points: [
        'Le score combine cout, pannes, temps d arret, disponibilite et temps moyen de reparation.',
        'Les points, facteurs et seuils sont parametrables.',
        'Le resultat classe la machine en Normal, A surveiller ou Critique.'
      ]
    },
    {
      title: 'Rapports et exports',
      subtitle: 'Sorties PDF et fichiers exploitables.',
      points: [
        'Le dashboard peut etre exporte.',
        'Les retards peuvent etre exportes.',
        'Les fiches machine et historiques peuvent etre generes en rapport PDF.'
      ]
    }
  ];

}
