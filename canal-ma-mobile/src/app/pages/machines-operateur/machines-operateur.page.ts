import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';
import {Machine} from "../../models/Machine";
import {MachineService} from "../../services/machine";
import {RouterLink} from "@angular/router";


@Component({
  selector: 'app-machines-operateur',
  standalone: true,
  imports: [IonicModule, CommonModule, FormsModule, RouterLink],
  templateUrl: './machines-operateur.page.html'
})
export class MachinesOperateurPage implements OnInit {

  machines: Machine[] = [];
  loading = false;
  searchTerm = '';

  constructor(private machineService: MachineService) {}

  ngOnInit() {
    this.loadMachines();
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

  get filteredMachines(): Machine[] {
    const query = this.searchTerm.trim().toLowerCase();
    if (!query) {
      return this.machines;
    }
    return this.machines.filter(machine =>
      machine.nom?.toLowerCase().includes(query) ||
      machine.ref?.toLowerCase().includes(query) ||
      machine.typeAction?.libelle?.toLowerCase().includes(query)
    );
  }

  getImageUrl(m: Machine): string {
    if (!m.id) return 'assets/machines/default.png';
    return this.machineService.getMachineImageUrl(m);
  }

  getEtatLabel(m: Machine): string {
    return m.typeAction?.libelle || 'État inconnu';
  }

  getEtatColorStyle(m: Machine) {
    const color = m.typeAction?.color || '#22c55e';
    return {
      'background-color': color + '33', 
      'border-color': color,
      'color': color
    };
  }

  protected readonly HTMLImageElement = HTMLImageElement;
}
