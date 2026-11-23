import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {MachineListComponent} from "./components/machine-list/machine-list.component";
import {MachineDetailComponent} from "./components/machine-detail/machine-detail.component";
import {MachineFormComponent} from "./components/machine-form/machine-form.component";
import {TypeParametresComponent} from "./components/type-parametres/type-parametres.component";
import {MaintenanceDashboardComponent} from "./components/maintenance-dashboard/maintenance-dashboard.component";
import {MaintenanceKanbanComponent} from "./components/maintenance-kanban/maintenance-kanban.component";
import {PiecesRechangeComponent} from "./components/pieces-rechange/pieces-rechange.component";
import {ScoreParametresComponent} from "./components/score-parametres/score-parametres.component";
import {AideApplicationComponent} from "./components/aide-application/aide-application.component";
import {RoleParametresComponent} from "./components/role-parametres/role-parametres.component";
import {LoginComponent} from "./components/login/login.component";
import {authGuard, pageGuard} from "./guards/auth.guard";


const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    canActivate: [authGuard],
    canActivateChild: [pageGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: MaintenanceDashboardComponent },
      { path: 'maintenance/kanban', component: MaintenanceKanbanComponent },
      { path: 'maintenance/pieces', component: PiecesRechangeComponent },
      { path: 'machines', component: MachineListComponent },
      { path: 'machinesD/:id', component: MachineDetailComponent },
      { path: 'machines/new', component: MachineFormComponent },
      { path: 'machines/:id/edit', component: MachineFormComponent },
      { path: 'parametres/types', component: TypeParametresComponent },
      { path: 'parametres/score', component: ScoreParametresComponent },
      { path: 'parametres/roles', component: RoleParametresComponent },
      { path: 'aide', component: AideApplicationComponent },
    ]
  },
  { path: '**', redirectTo: 'dashboard' },

];
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }