import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ProjectsComponent } from './projects.component';
import { VersionsComponent } from './versions/versions.component';

@NgModule({
	imports: [RouterModule.forChild([
		{ path: 'projects', component: ProjectsComponent },
		{ path: 'versions',  component: VersionsComponent},
		{ path: 'versions/:projectId',  component: VersionsComponent},
		{ path: 'version/:projectId/:id',  loadChildren: () => import('./version-form/version-form.module').then(m => m.ProjectFormModule)},
		


	])],
	exports: [RouterModule]
})
export class ProjectsRoutingModule { }
