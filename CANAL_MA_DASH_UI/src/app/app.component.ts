import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService, AuthUser } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'CANAL_MA_DASH_UI';

  isDarkMode = true;

  constructor(private auth: AuthService, private router: Router) {}

  get estPageLogin(): boolean {
    return this.router.url.startsWith('/login');
  }

  get utilisateur(): AuthUser | null {
    return this.auth.user;
  }

  peutFaire(code: string): boolean {
    return this.auth.peutFaire(code);
  }

  deconnexion(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  ngOnInit(): void {
    const storedTheme = localStorage.getItem('theme');
    if (storedTheme === 'light') {
      this.isDarkMode = false;
    } else if (storedTheme === 'dark') {
      this.isDarkMode = true;
    }

    this.applyThemeClass();
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
    this.applyThemeClass();
  }

  private applyThemeClass(): void {
    const root = document.documentElement;
    if (this.isDarkMode) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }
}