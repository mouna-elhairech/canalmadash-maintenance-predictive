import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { MachinesOperateurPage } from './machines-operateur.page';

const routes: Routes = [
  {
    path: '',
    component: MachinesOperateurPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class MachinesOperateurPageRoutingModule {}
