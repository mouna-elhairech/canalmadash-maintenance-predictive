import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';


const routes: Routes = [
  {
    path: '',
    redirectTo: 'profil-choix',
    pathMatch: 'full'
  },
  {
    path: 'profil-choix',
    loadChildren: () =>
      import('./pages/profil-choix/profil-choix.module').then(m => m.ProfilChoixPageModule)
  },
  {
    path: 'machines-operateur',
    loadChildren: () =>
      import('./pages/machines-operateur/machines-operateur.module')
        .then(m => m.MachinesOperateurPageModule)
  },
  {
    path: 'machines-operateur/:id',
    loadComponent: () =>
      import('./pages/machines-operateur-detail/machines-operateur-detail.page')
        .then(m => m.MachinesOperateurDetailPage)
  },
  {
    path: 'machines-maintenance/:id',
    loadComponent: () =>
      import('./pages/machines-operateur-detail/machines-operateur-detail.page')
        .then(m => m.MachinesOperateurDetailPage)
  },
  {
    path: 'machines-maintenance',
    loadChildren: () =>
      import('./pages/machines-maintenance/machines-maintenance.module')
        .then(m => m.MachinesMaintenancePageModule)
  },
  {
    path: 'home',
    loadChildren: () =>
      import('./home/home.module').then(m => m.HomePageModule)
  },
  {
    path: '**',
    redirectTo: 'profil-choix'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
