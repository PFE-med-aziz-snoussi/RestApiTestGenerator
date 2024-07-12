import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuardRedirect {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): Observable<boolean | UrlTree> {
    return this.authService.getCurrentUser().pipe(
      map(user => {
        if (user && user.accountNonExpired) {
          return this.router.parseUrl('/dashboard');  
        } else {
          return true;  
        }
      }),
      catchError((error) => {
        console.error(error);
        return of(true); 
      })
    );
  }
}
