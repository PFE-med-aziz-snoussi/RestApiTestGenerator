import { inject } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';
import { Observable, from, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export const authGuard = (): Observable<boolean | UrlTree> => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return from(authService.getCurrentUser()).pipe(
    map(user => {
      if (user && user.accountNonExpired) {
        return true;
      } else {
        authService.logout(); 
        return router.parseUrl('/login');
      }
    }),
    catchError(() => {
      authService.logout(); 
      return of(router.parseUrl('/login'));
    })
  );
};
