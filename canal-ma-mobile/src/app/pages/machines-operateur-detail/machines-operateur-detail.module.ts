import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { MachinesOperateurDetailPageRoutingModule } from './machines-operateur-detail-routing.module';

import { MachinesOperateurDetailPage } from './machines-operateur-detail.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    MachinesOperateurDetailPageRoutingModule,
    MachinesOperateurDetailPage
  ],
  declarations: []
})
export class MachinesOperateurDetailPageModule {}
