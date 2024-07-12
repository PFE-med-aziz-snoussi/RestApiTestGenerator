import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';


@NgModule({
  imports: [RouterModule.forChild([
    { path: 'users', loadChildren: () => import('./users/users.module').then(m => m.UsersModule) },
    { path: 'projects', loadChildren: () => import('../user/projects/projects.module').then(m => m.ProjectsModule) },
    { path: 'versions', loadChildren: () => import('./versions/versions.module').then(m => m.VersionsModule) },

  ])],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
