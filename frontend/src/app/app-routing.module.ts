import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotfoundComponent } from './auth/notfound/notfound.component';
import { AppLayoutComponent } from "./layout/app.layout.component";
import { authGuard } from './services/auth-guard.service';
import { AdminGuard } from './services/admin-guard.service';
import { UserGuard } from './services/user-guard.service';
import { AuthGuardRedirect } from './services/auth-guard-redirect.service';
import { ProfileComponent } from './profile/profile.component';

const routes: Routes = [
  {
    path: '', component: AppLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadChildren: () => import('./dashboard/dashboard.module').then(m => m.DashboardModule), canActivate: [authGuard] },
      { path: 'profile',  component: ProfileComponent , canActivate: [authGuard] },
      { path: 'admin', loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule), canActivate: [authGuard,AdminGuard] },
      { path: 'user', loadChildren: () => import('./user/user.module').then(m => m.UserModule), canActivate: [authGuard,UserGuard] },


     // { path: 'landing', loadChildren: () => import('./auth/home/home.module').then(m => m.LandingModule), canActivate: [authGuard] },
    ]
  },
  { path: '', loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule), canActivate: [AuthGuardRedirect] },
  { path: 'notfound', component: NotfoundComponent },
  { path: 'error', loadChildren: () => import('./auth/error/error.module').then(m => m.ErrorModule) },
  { path: 'access', loadChildren: () => import('./auth/access/access.module').then(m => m.AccessModule) },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/notfound' },

];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled', anchorScrolling: 'enabled', onSameUrlNavigation: 'reload' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
