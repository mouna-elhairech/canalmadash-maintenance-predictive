// Noms lisibles des variables du modele (utilises par le Dashboard)
const LABELS: Record<string, string> = {
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

export function factorLabel(name: string): string {
  return LABELS[name] || name;
}