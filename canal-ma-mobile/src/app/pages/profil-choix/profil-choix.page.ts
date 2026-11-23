import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profil-choix',
  standalone: true,
  imports: [IonicModule, CommonModule],
  templateUrl: './profil-choix.page.html'
})
export class ProfilChoixPage {

  constructor(private router: Router) {}

  choisirOperateur() {
    this.router.navigate(['/machines-operateur']);
  }

  choisirMaintenance() {
    this.router.navigate(['/machines-maintenance']);
  }
}
