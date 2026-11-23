import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../environments/environment';

const STORAGE_KEY = 'canalmadash_auth';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const isApi = req.url.startsWith(environment.apiUrl);
    const isLogin = req.url.endsWith('/auth/login');
    let request = req;

    if (isApi && !isLogin && !req.headers.has('Authorization')) {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      const token = raw ? JSON.parse(raw).token : null;
      if (token) {
        request = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
      }
    }

    return next.handle(request).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse && isApi && !isLogin) {
          if (error.status === 401) {
            sessionStorage.removeItem(STORAGE_KEY);
            this.router.navigate(['/login']);
          } else if (error.status === 403) {
            this.afficherMessage("Accès refusé : votre rôle ne permet pas cette action.");
          }
        }
        return throwError(() => error);
      })
    );
  }

  private afficherMessage(texte: string): void {
    const element = document.createElement('div');
    element.textContent = texte;
    element.setAttribute('role', 'alert');
    element.style.cssText =
      'position:fixed;top:16px;right:16px;z-index:9999;background:#991b1b;color:#fff;' +
      'padding:12px 16px;border-radius:8px;font-size:14px;box-shadow:0 4px 12px rgba(0,0,0,.2)';
    document.body.appendChild(element);
    setTimeout(() => element.remove(), 4000);
  }
}