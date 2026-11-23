import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Verifie que l'utilisateur est connecte.
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn ? true : router.createUrlTree(['/login']);
};

// Verifie, a chaque changement de page, que le role a le droit d'y aller.
export const pageGuard: CanActivateChildFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn) {
    return router.createUrlTree(['/login']);
  }
  if (auth.peutVoirRoute(state.url)) {
    return true;
  }
  return router.createUrlTree([auth.pageAccueil()]);
};