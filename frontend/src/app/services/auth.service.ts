import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { CookieService } from 'ngx-cookie-service';
import { User } from '../models/user.model';
import { environment } from 'src/environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private router: Router,
    private cookieService: CookieService,
  ) {
  }

  login(credentials: { username: string, password: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/auth/signin`, credentials, { observe: 'response', withCredentials: true }).pipe(
      tap((response: HttpResponse<any>) => {
        window.sessionStorage.removeItem("auth-user");
        window.sessionStorage.setItem("auth-user", JSON.stringify(response.body));        
        const jwtCookie = this.getJwtCookieFromResponse(response);
        if (jwtCookie) {
         // console.log('Setting JWT Cookie:', jwtCookie); 
          //localStorage.setItem('jwtCookie',jwtCookie);
          window.sessionStorage.setItem('jwtCookie',jwtCookie);
        }
        this.router.navigate(['/dashboard']); 
      })
    );
  }

  private getJwtCookieFromResponse(response: HttpResponse<any>): string | null {
    const cookies = response.body.token;
    if (cookies) {
      const cookieArray = cookies.split(';');
      for (const cookie of cookieArray) {
        if (cookie.trim().startsWith('jwtCookie=')) {
          const cookieValue = cookie.split('=')[1];
          //console.log('Extracted JWT Cookie:', cookieValue); 
          return cookieValue;
        }
      }
    }
    return null;
  }

  register(data: User): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/auth/signup`, data);
  }

  logout(): void {
    // Make the HTTP POST request to your logout endpoint
    this.http.post<any>(`${this.apiUrl}/auth/signout`, {}, { observe: 'response', withCredentials: true }).subscribe(
      response => {
        this.cookieService.delete('jwtCookie');
        window.sessionStorage.removeItem('jwtCookie');
        window.sessionStorage.removeItem("auth-user");
        window.sessionStorage.clear();
        this.router.navigate(['/login']);
      },
      error => {
        console.error('Logout error:', error);
        this.cookieService.delete('jwtCookie');
        window.sessionStorage.removeItem('jwtCookie');
        window.sessionStorage.removeItem("auth-user");
        window.sessionStorage.clear();
        this.router.navigate(['/login']);
      }
    );
  }

  isAuthenticated(): boolean {
    const jwtToken = window.sessionStorage.getItem("jwtCookie");
    const authUser = window.sessionStorage.getItem("auth-user");
    //console.log(jwtToken)
    //console.log(authUser)
    //console.log(this.cookieService.get('jwtCookie'));
    return !!jwtToken && !!authUser; 
  }

  getToken(): string | null {
    return this.cookieService.get('jwtCookie');
  }

  getCurrentUser(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/auth/currentuser`, { withCredentials: true });
  }
}
