import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChangesComponent } from './changes.component';


@NgModule({
  imports: [RouterModule.forChild([
		{ path: '', component: ChangesComponent },

	])],  exports: [RouterModule]
})
export class ChangesRoutingModule { }
