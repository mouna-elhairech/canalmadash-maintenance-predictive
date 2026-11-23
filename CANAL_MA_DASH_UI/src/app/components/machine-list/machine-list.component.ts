import { Component, OnDestroy, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service';
import { Machine } from '../../models/machine.model';
import { interval, Subscription } from 'rxjs';

import { AuthService } from '../../services/auth.service';
interface StateStat {
  type: string;     
  label: string;     
  color: string;     
  count: number;
  percentage: number;
}

@Component({
  selector: 'app-machine-list',
  templateUrl: './machine-list.component.html'
})
export class MachineListComponent implements OnInit, OnDestroy {

  machines: Machine[] = [];
  now: Date = new Date();
  private timerSub?: Subscription;
  private refreshSub?: Subscription;

  stats: StateStat[] = [];

  constructor(private machineService: MachineService, public auth: AuthService) {}

  ngOnInit(): void {
    this.loadMachines();

    this.timerSub = interval(1000).subscribe(() => {
      this.now = new Date();
    });

    this.refreshSub = interval(15000).subscribe(() => {
      this.loadMachines();
    });
  }

  ngOnDestroy(): void {
    this.timerSub?.unsubscribe();
    this.refreshSub?.unsubscribe();
  }

  loadMachines(): void {
    this.machineService.getMachines().subscribe({
      next: machines => {
        this.machines = machines;
        this.computeStats();
      },
      error: err => console.error(err)
    });
  }

  getElapsedForMachine(machine: Machine): string {
    if (!machine.dateDebutAction) return '--:--:--';
    const start = new Date(machine.dateDebutAction);
    const diffMs = this.now.getTime() - start.getTime();
    if (diffMs < 0) return '00:00:00';

    const totalSeconds = Math.floor(diffMs / 1000);
    const h = Math.floor(totalSeconds / 3600);
    const m = Math.floor((totalSeconds % 3600) / 60);
    const s = totalSeconds % 60;

    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${pad(h)}:${pad(m)}:${pad(s)}`;
  }

  // Image associee a la machine.
  getImage(machine: Machine): string {
    if (machine.imagePath) {
      return this.machineService.getMachineImageUrl(machine);
    }
    return 'assets/machines/img.png';
  }

  // Couleurs d'affichage selon l'etat machine.

  getTypeLabel(machine: Machine): string {
    return machine.typeAction?.libelle || machine.typeAction?.code || 'État inconnu';
  }

  getTypeBadgeStyle(machine: Machine) {
    const base = this.getBaseColor(machine);
    const bg = base.length === 7 ? base + '33' : base;
    const border = base.length === 7 ? base + '88' : base;
    return {
      'background-color': bg,
      'border-color': border,
      'color': base
    };
  }

  getDotStyle(machine: Machine) {
    const base = this.getBaseColor(machine);
    return { 'background-color': base };
  }

  private getBaseColor(machine: Machine): string {
    const code = machine.typeAction?.code || 'UNKNOWN';
    if (machine.typeAction?.color) {
      return machine.typeAction.color;
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







  // Valeurs rapides affichees dans la liste.
  private computeStats(): void {
    if (!this.machines || this.machines.length === 0) {
      this.stats = [];
      return;
    }

    const map = new Map<string, { label: string; color: string; count: number }>();

    for (const m of this.machines) {
      const code = m.typeAction?.code || 'UNKNOWN';
      const label = m.typeAction?.libelle || code;
      const color = this.getBaseColor(m);

      if (!map.has(code)) {
        map.set(code, { label, color, count: 0 });
      }
      map.get(code)!.count++;
    }

    const total = this.machines.length;
    const stats: StateStat[] = [];

    map.forEach((v, code) => {
      const percentage = (v.count / total) * 100;
      stats.push({
        type: code,
        label: v.label,
        color: v.color,
        count: v.count,
        percentage
      });
    });

    this.stats = stats.sort((a, b) => b.percentage - a.percentage);
  }

  getChartStyle() {
    if (this.stats.length === 0) {
      return { background: 'radial-gradient(circle, #020617 0, #0f172a 60%)' };
    }

    let currentAngle = 0;
    const parts: string[] = [];

    this.stats.forEach(stat => {
      const angle = stat.percentage * 3.6;
      const start = currentAngle;
      const end = currentAngle + angle;
      parts.push(`${stat.color} ${start}deg ${end}deg`);
      currentAngle = end;
    });

    const gradient = `conic-gradient(${parts.join(', ')})`;
    return { background: gradient };
  }

  onDelete(id: number, event: MouseEvent): void {
    event.stopPropagation();
    if (!confirm('Supprimer cette machine ?')) return;

    this.machineService.deleteMachine(id).subscribe({
      next: () => {
        this.loadMachines();
      },
      error: err => console.error(err)
    });
  }

}
