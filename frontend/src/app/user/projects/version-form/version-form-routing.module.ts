import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { VersionFormComponent } from './version-form.component';


@NgModule({
  imports: [RouterModule.forChild([
    { path: '', component: VersionFormComponent },
    { path: ':id', component: VersionFormComponent },
  
  ]
  )],
  exports: [RouterModule]
})
export class ProjectFormRoutingModule { }
