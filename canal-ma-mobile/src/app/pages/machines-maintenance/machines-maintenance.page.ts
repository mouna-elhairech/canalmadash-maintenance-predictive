import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import {Machine} from "../../models/Machine";
import {MachineService} from "../../services/machine";
import {DashboardMaintenance} from "../../models/DashboardMaintenance";
import {DashboardMaintenanceService} from "../../services/dashboard-maintenance";


@Component({
  selector: 'app-machines-maintenance',
  standalone: true,
  imports: [IonicModule, CommonModule, RouterLink],
  templateUrl: './machines-maintenance.page.html'
})
export class MachinesMaintenancePage implements OnInit {

  machines: Machine[] = [];
  dashboard?: DashboardMaintenance;
  loading = false;
  loadingDashboard = false;
  activeView: 'resume' | 'alertes' | 'analyse' | 'machines' = 'resume';

  constructor(
    private machineService: MachineService,
    private dashboardMaintenanceService: DashboardMaintenanceService
  ) {}

  ngOnInit() {
    this.loadMachines();
    this.loadDashboard();
  }

  loadMachines() {
    this.loading = true;
    this.machineService.getMachines().subscribe({
      next: machines => {
        this.machines = machines;
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  loadDashboard() {
    this.loadingDashboard = true;
    this.dashboardMaintenanceService.getMaintenanceDashboard().subscribe({
      next: dashboard => {
        this.dashboard = dashboard;
        this.loadingDashboard = false;
      },
      error: err => {
        console.error(err);
        this.loadingDashboard = false;
      }
    });
  }

  refreshAll() {
    this.loadMachines();
    this.loadDashboard();
  }

  formatDuration(minutes?: number): string {
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

  formatMoney(value?: number): string {
    return `${(value || 0).toFixed(2)} DH`;
  }

  getPriorityLabel(priority?: string): string {
    switch (priority) {
      case 'FAIBLE': return 'Faible';
      case 'NORMALE': return 'Normale';
      case 'URGENTE': return 'Urgente';
      case 'CRITIQUE': return 'Critique';
      default: return priority || 'Normale';
    }
  }

  getGlobalAverageBetweenFailuresLabel(): string {
    if (!this.dashboard || this.dashboard.mtbfHours <= 0) {
      return 'Non calculable';
    }
    return `${this.dashboard.mtbfHours.toFixed(1)} h`;
  }

  getGlobalAverageRepairTimeLabel(): string {
    if (!this.dashboard || this.dashboard.mttrHours <= 0) {
      return 'Non calculable';
    }
    return `${this.dashboard.mttrHours.toFixed(1)} h`;
  }

  getImageUrl(m: Machine): string {
    if (!m.id) return 'assets/machines/default.png';
    return this.machineService.getMachineImageUrl(m);
  }

  getEtatLabel(m: Machine): string {
    return m.typeAction?.libelle || 'État inconnu';
  }

  getEtatColorStyle(m: Machine) {
    const color = m.typeAction?.color || '#38bdf8';
    return {
      'background-color': color + '33',
      'border-color': color,
      'color': color
    };
  }
}
