import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { MachinesOperateurPageRoutingModule } from './machines-operateur-routing.module';

import { MachinesOperateurPage } from './machines-operateur.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    MachinesOperateurPageRoutingModule,
    MachinesOperateurPage
  ],
  declarations: []
})
export class MachinesOperateurPageModule {}
