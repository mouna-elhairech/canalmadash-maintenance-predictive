import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { MachinesOperateurDetailPage } from './machines-operateur-detail.page';

const routes: Routes = [
  {
    path: '',
    component: MachinesOperateurDetailPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class MachinesOperateurDetailPageRoutingModule {}
