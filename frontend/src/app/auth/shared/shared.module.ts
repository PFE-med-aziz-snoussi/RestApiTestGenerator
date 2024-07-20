import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../navbar/navbar.component';
import { ButtonModule } from 'primeng/button';
import { PanelModule } from 'primeng/panel';
import { ChartModule } from 'primeng/chart';
import { StyleClassModule } from 'primeng/styleclass';
import { DividerModule } from 'primeng/divider';

@NgModule({
  declarations: [
    NavbarComponent
  ],
  imports: [
    CommonModule,
    DividerModule,
    StyleClassModule,
    ChartModule,
    PanelModule,
    ButtonModule,
  ],
  exports: [
    CommonModule,
    NavbarComponent
  ]
})
export class SharedModule { }
