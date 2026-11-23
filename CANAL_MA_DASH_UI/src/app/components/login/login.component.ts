import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  email = '';
  motDePasse = '';
  erreur = '';
  chargement = false;

  constructor(private auth: AuthService, private router: Router) {
    if (this.auth.isLoggedIn) {
      this.router.navigate([this.auth.pageAccueil()]);
    }
  }

  seConnecter(): void {
    if (!this.email || !this.motDePasse) {
      this.erreur = 'Saisis ton email et ton mot de passe.';
      return;
    }
    this.chargement = true;
    this.erreur = '';
    this.auth.login(this.email, this.motDePasse).subscribe({
      next: () => {
        this.chargement = false;
        this.router.navigate([this.auth.pageAccueil()]);
      },
      error: err => {
        this.chargement = false;
        this.erreur = err.status === 401
          ? 'Email ou mot de passe incorrect.'
          : 'Serveur indisponible. Vérifie que le backend est démarré.';
      }
    });
  }
}