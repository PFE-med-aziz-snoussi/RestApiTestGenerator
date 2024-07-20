import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HomeComponent } from 'src/app/auth/home/home.component';

@NgModule({
    imports: [RouterModule.forChild([
        { path: '', component: HomeComponent }
    ])],
    exports: [RouterModule]
})
export class HomeRoutingModule { }
