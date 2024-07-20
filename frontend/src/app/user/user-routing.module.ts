import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';


@NgModule({
  imports: [RouterModule.forChild([
    { path: '', loadChildren: () => import('./projects/projects.module').then(m => m.ProjectsModule) },

])],
  exports: [RouterModule]
})
export class UserRoutingModule { }
