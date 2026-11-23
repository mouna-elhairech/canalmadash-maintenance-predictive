import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MachineListComponent } from './components/machine-list/machine-list.component';
import { MachineDetailComponent } from './components/machine-detail/machine-detail.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import { MachineFormComponent } from './components/machine-form/machine-form.component';
import { TypeParametresComponent } from './components/type-parametres/type-parametres.component';
import {CommonModule} from "@angular/common";
import { MaintenanceDashboardComponent } from './components/maintenance-dashboard/maintenance-dashboard.component';
import { MaintenanceKanbanComponent } from './components/maintenance-kanban/maintenance-kanban.component';
import { PiecesRechangeComponent } from './components/pieces-rechange/pieces-rechange.component';
import { ScoreParametresComponent } from './components/score-parametres/score-parametres.component';
import { AideApplicationComponent } from './components/aide-application/aide-application.component';
import { RoleParametresComponent } from './components/role-parametres/role-parametres.component';
import { GlobalAiChatComponent } from './components/global-ai-chat/global-ai-chat.component';
import { LoginComponent } from './components/login/login.component';
import { AuthInterceptor } from './interceptors/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    MachineListComponent,
    MachineDetailComponent,
    MachineFormComponent,
    TypeParametresComponent,
    MaintenanceDashboardComponent,
    MaintenanceKanbanComponent,
    PiecesRechangeComponent,
    ScoreParametresComponent,
    AideApplicationComponent,
    RoleParametresComponent,
    GlobalAiChatComponent,
    LoginComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule,
    CommonModule,
    ReactiveFormsModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }