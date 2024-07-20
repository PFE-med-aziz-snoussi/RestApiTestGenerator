import { DropdownModule } from 'primeng/dropdown';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { FormsModule,ReactiveFormsModule } from '@angular/forms';
import { PasswordModule } from 'primeng/password';
import { InputTextModule } from 'primeng/inputtext';
import { RegisterRoutingModule } from './register-routing.module';
import { RegisterComponent } from './register.component';
import { FileUploadModule } from 'primeng/fileupload';
import { SharedModule } from '../shared/shared.module';


@NgModule({

  imports: [
    ReactiveFormsModule,
    CommonModule,
    RegisterRoutingModule,
    ButtonModule,
    CheckboxModule,
    InputTextModule,
    FormsModule,
    PasswordModule,
    DropdownModule,
    FileUploadModule,
    SharedModule
  ],
  declarations: [RegisterComponent]
})
export class RegisterModule { }
