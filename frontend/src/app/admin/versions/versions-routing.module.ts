import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { VersionsComponent } from './versions.component';


@NgModule({
  imports: [RouterModule.forChild([
		{ path: '', component: VersionsComponent },
		{ path: ':projectId/:id',  loadChildren: () => import('../../user/projects/version-form/version-form.module').then(m => m.ProjectFormModule)},

	])],  exports: [RouterModule]
})
export class VersionsRoutingModule { }
