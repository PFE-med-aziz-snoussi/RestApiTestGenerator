import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ProjectsComponent } from '../user/projects/projects.component';


@NgModule({
  imports: [RouterModule.forChild([
    { path: 'users', loadChildren: () => import('./users/users.module').then(m => m.UsersModule) },
    { path: 'projects', component: ProjectsComponent },
    { path: 'versions', loadChildren: () => import('./versions/versions.module').then(m => m.VersionsModule) },
    { path: 'executions', loadChildren: () => import('./executions/executions.module').then(m => m.ExecutionsModule) },
    { path: 'changes', loadChildren: () => import('./changes/changes.module').then(m => m.ChangesModule) },


  ])],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
