import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { MachinesMaintenancePageRoutingModule } from './machines-maintenance-routing.module';

import { MachinesMaintenancePage } from './machines-maintenance.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    MachinesMaintenancePageRoutingModule,
    MachinesMaintenancePage
  ],
  declarations: []
})
export class MachinesMaintenancePageModule {}
