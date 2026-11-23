import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';

import { MachineListPageRoutingModule } from './machine-list-routing.module';
import { MachineListPage } from './machine-list.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    MachineListPageRoutingModule,
    MachineListPage      
  ],
  declarations: []        
})
export class MachineListPageModule {}
