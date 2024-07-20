import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StyleClassModule } from 'primeng/styleclass';
import { DividerModule } from 'primeng/divider';
import { ChartModule } from 'primeng/chart';
import { PanelModule } from 'primeng/panel';
import { ButtonModule } from 'primeng/button';
import { HomeRoutingModule } from 'src/app/auth/home/home-routing.module';
import { SharedModule } from 'src/app/auth/shared/shared.module';
import { HomeComponent } from 'src/app/auth/home/home.component';

@NgModule({
    imports: [
        CommonModule,
        HomeRoutingModule,
        DividerModule,
        StyleClassModule,
        ChartModule,
        PanelModule,
        ButtonModule,
        SharedModule
        ],
    declarations: [HomeComponent]
})
export class LandingModule { }
