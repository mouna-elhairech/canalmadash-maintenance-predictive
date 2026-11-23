import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { ProfilChoixPage } from './profil-choix.page';

const routes: Routes = [
  {
    path: '',
    component: ProfilChoixPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ProfilChoixPageRoutingModule {}
