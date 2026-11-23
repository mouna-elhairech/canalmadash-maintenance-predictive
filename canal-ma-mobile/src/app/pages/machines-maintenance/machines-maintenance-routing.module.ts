import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { MachinesMaintenancePage } from './machines-maintenance.page';

const routes: Routes = [
  {
    path: '',
    component: MachinesMaintenancePage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class MachinesMaintenancePageRoutingModule {}
