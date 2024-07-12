import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProjectFormRoutingModule } from './version-form-routing.module';
import { TimelineModule } from 'primeng/timeline';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { VersionFormComponent } from './version-form.component';
import { CalendarModule } from 'primeng/calendar';
import { TabMenuModule } from 'primeng/tabmenu';
import { StepsModule } from 'primeng/steps';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { FormsModule } from '@angular/forms';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { ProgressBarModule } from 'primeng/progressbar';
import { ToolbarModule } from 'primeng/toolbar';
import { SplitButtonModule } from 'primeng/splitbutton';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import { ReportComponent } from '../report/report.component';
import { TableModule } from 'primeng/table';
import { SelectButtonModule } from 'primeng/selectbutton';


@NgModule({
  imports: [
    CommonModule,
    ProjectFormRoutingModule,
    TimelineModule,
    ButtonModule,
    CardModule,
    CalendarModule,
    TabMenuModule,
    StepsModule,
    InputTextModule,
    InputTextareaModule,
    FormsModule,
    MessageModule,
    ToastModule,
    FileUploadModule,
    ProgressBarModule,
    ToolbarModule,		
    SplitButtonModule,
    DialogModule,
    ConfirmDialogModule,
    ConfirmPopupModule,
    ReportComponent ,
    TableModule,
    SelectButtonModule
  ],
  declarations: [VersionFormComponent]

})
export class ProjectFormModule { }
