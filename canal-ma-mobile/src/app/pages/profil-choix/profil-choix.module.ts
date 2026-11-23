import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonicModule } from '@ionic/angular';

import { ProfilChoixPageRoutingModule } from './profil-choix-routing.module';
import { ProfilChoixPage } from './profil-choix.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ProfilChoixPageRoutingModule,
    ProfilChoixPage      
  ],
  declarations: []        
})
export class ProfilChoixPageModule {


}
