import { Component, OnInit } from '@angular/core';
import {
  ScoreCriticiteParametre,
  ScoreCriticiteParametreService
} from '../../services/score-criticite-parametre.service';

interface ScoreWeight {
  label: string;
  max: number;
  description: string;
}

interface ScoreLevel {
  label: string;
  factor: number;
  description: string;
}

@Component({
  selector: 'app-score-parametres',
  templateUrl: './score-parametres.component.html'
})
export class ScoreParametresComponent implements OnInit {
  parametres?: ScoreCriticiteParametre;
  loading = false;
  saving = false;
  message = '';
  errorMessage = '';

  constructor(private scoreService: ScoreCriticiteParametreService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.scoreService.get().subscribe({
      next: parametres => {
        this.parametres = parametres;
        this.loading = false;
        this.refreshStaticRows();
      },
      error: err => {
        console.error(err);
        this.errorMessage = 'Impossible de charger les parametres du score.';
        this.loading = false;
      }
    });
  }

  save(): void {
    if (!this.parametres) return;
    this.saving = true;
    this.message = '';
    this.errorMessage = '';
    this.scoreService.update(this.parametres).subscribe({
      next: parametres => {
        this.parametres = parametres;
        this.saving = false;
        this.message = 'Parametres du score enregistres.';
        this.refreshStaticRows();
      },
      error: err => {
        console.error(err);
        this.saving = false;
        this.errorMessage = 'Erreur lors de l enregistrement des parametres.';
      }
    });
  }

  resetDefaults(): void {
    this.saving = true;
    this.message = '';
    this.scoreService.reset().subscribe({
      next: parametres => {
        this.parametres = parametres;
        this.saving = false;
        this.message = 'Parametres par defaut restaures.';
        this.refreshStaticRows();
      },
      error: err => {
        console.error(err);
        this.saving = false;
        this.errorMessage = 'Erreur lors de la reinitialisation.';
      }
    });
  }

  weights: ScoreWeight[] = [
    {
      label: 'Cout maintenance',
      max: 20,
      description: 'Compare le cout cumule de maintenance avec le cout machine et installation.'
    },
    {
      label: 'Nombre de pannes',
      max: 25,
      description: 'Mesure la frequence des pannes detectees dans l historique.'
    },
    {
      label: 'Temps d arret',
      max: 25,
      description: 'Mesure le temps total ou la machine reste indisponible.'
    },
    {
      label: 'Disponibilite faible',
      max: 20,
      description: 'Penalise une machine lorsque sa disponibilite passe sous les seuils.'
    },
    {
      label: 'Temps moyen de reparation',
      max: 10,
      description: 'Mesure la rapidite moyenne des remises en service.'
    }
  ];

  levels: ScoreLevel[] = [
    {
      label: 'Normal',
      factor: 0,
      description: 'Aucun point ajoute pour ce critere.'
    },
    {
      label: 'Surveillance',
      factor: 45,
      description: 'Le critere commence a devenir visible dans le score.'
    },
    {
      label: 'Attention',
      factor: 70,
      description: 'Le critere devient important et augmente fortement le score.'
    },
    {
      label: 'Critique',
      factor: 100,
      description: 'Le critere utilise tout son poids maximum.'
    }
  ];

  exampleRows = [
    { label: 'Cout maintenance', max: 20, level: 'Attention', factor: 70, value: 14 },
    { label: 'Nombre de pannes', max: 25, level: 'Critique', factor: 100, value: 25 },
    { label: 'Temps d arret', max: 25, level: 'Attention', factor: 70, value: 17.5 },
    { label: 'Disponibilite faible', max: 20, level: 'Surveillance', factor: 45, value: 9 },
    { label: 'Temps moyen de reparation', max: 10, level: 'Normal', factor: 0, value: 0 }
  ];

  get totalExample(): number {
    return this.exampleRows.reduce((sum, row) => sum + row.value, 0);
  }

  refreshStaticRows(): void {
    if (!this.parametres) return;
    this.weights = [
      {
        label: 'Cout maintenance',
        max: this.parametres.poidsCoutMaintenance,
        description: 'Compare le cout cumule de maintenance avec le cout machine et installation.'
      },
      {
        label: 'Nombre de pannes',
        max: this.parametres.poidsNombrePannes,
        description: 'Mesure la frequence des pannes detectees dans l historique.'
      },
      {
        label: 'Temps d arret',
        max: this.parametres.poidsTempsArret,
        description: 'Mesure le temps total ou la machine reste indisponible.'
      },
      {
        label: 'Disponibilite faible',
        max: this.parametres.poidsDisponibilite,
        description: 'Penalise une machine lorsque sa disponibilite passe sous les seuils.'
      },
      {
        label: 'Temps moyen de reparation',
        max: this.parametres.poidsTempsReparation,
        description: 'Mesure la rapidite moyenne des remises en service.'
      }
    ];
    this.levels = [
      { label: 'Normal', factor: 0, description: 'Aucun point ajoute pour ce critere.' },
      { label: 'Surveillance', factor: this.parametres.facteurSurveillance, description: 'Le critere commence a devenir visible dans le score.' },
      { label: 'Attention', factor: this.parametres.facteurAttention, description: 'Le critere devient important et augmente fortement le score.' },
      { label: 'Critique', factor: this.parametres.facteurCritique, description: 'Le critere utilise son facteur maximum configure.' }
    ];
    this.exampleRows = [
      { label: 'Cout maintenance', max: this.parametres.poidsCoutMaintenance, level: 'Attention', factor: this.parametres.facteurAttention, value: this.exampleValue(this.parametres.poidsCoutMaintenance, this.parametres.facteurAttention) },
      { label: 'Nombre de pannes', max: this.parametres.poidsNombrePannes, level: 'Critique', factor: this.parametres.facteurCritique, value: this.exampleValue(this.parametres.poidsNombrePannes, this.parametres.facteurCritique) },
      { label: 'Temps d arret', max: this.parametres.poidsTempsArret, level: 'Attention', factor: this.parametres.facteurAttention, value: this.exampleValue(this.parametres.poidsTempsArret, this.parametres.facteurAttention) },
      { label: 'Disponibilite faible', max: this.parametres.poidsDisponibilite, level: 'Surveillance', factor: this.parametres.facteurSurveillance, value: this.exampleValue(this.parametres.poidsDisponibilite, this.parametres.facteurSurveillance) },
      { label: 'Temps moyen de reparation', max: this.parametres.poidsTempsReparation, level: 'Normal', factor: 0, value: 0 }
    ];
  }

  exampleValue(max: number, factor: number): number {
    return Math.round(max * factor) / 100;
  }
}
