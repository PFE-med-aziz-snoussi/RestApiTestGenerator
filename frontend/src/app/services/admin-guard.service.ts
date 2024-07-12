import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    return this.authService.getCurrentUser().toPromise().then(user => {
        console.log(user)
      if (user && user.authorities[0].authority === 'ROLE_ADMIN') {
        return true;
      } else {
        return this.router.parseUrl('/access');
      }
    });
  }
}
