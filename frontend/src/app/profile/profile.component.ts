import { AuthService } from './../services/auth.service';
import { Component, OnInit } from '@angular/core';
import { UserService } from 'src/app/services/user.service';
import { ProjectService } from '../services/project.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { FormsModule, NgForm } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { FileUploadModule } from 'primeng/fileupload';
import { InputTextModule } from 'primeng/inputtext';

@Component({
  selector: 'app-profile',
  standalone: true,
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
  imports: [CommonModule, ButtonModule, DialogModule, FormsModule, ToastModule, FileUploadModule,InputTextModule],
  providers: [MessageService]
})
export class ProfileComponent implements OnInit {
  user: any = {};
  userPhotoUrl: SafeUrl | null = null;
  projects: any[] = [];
  displayDeleteDialog: boolean = false;
  displayChangePasswordDialog: boolean = false;
  password: string = '';
  newPassword: string = '';
  oldPassword: string = '';
  confirmPassword: string = '';
  submitted: boolean = false;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private projectService: ProjectService,
    private sanitizer: DomSanitizer,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.loadUserProfile();
    this.fetchUserPhoto();
    this.fetchProjects();
  }

  fetchUserPhoto() {
    this.userService.getCurrentUserPhoto().subscribe(
      photoBlob => {
        const url = window.URL.createObjectURL(photoBlob);
        this.userPhotoUrl = this.sanitizer.bypassSecurityTrustUrl(url);
      },
      error => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load user photo', life: 3000 });
      }
    );
  }

  loadUserProfile() {
    this.userService.getCurrentUser().subscribe(
      (data: any) => {
        this.user = data;
      },
      error => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load user profile', life: 3000 });
      }
    );
  }

  fetchProjects() {
    this.projectService.getMyProjects().subscribe(
      data => {
        this.projects = data;
      },
      error => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load projects', life: 3000 });
      }
    );
  }

  confirmDelete() {
    this.displayDeleteDialog = true;
  }

  deleteAccount() {
    this.userService.deleteCurrentUser(this.password).subscribe(
      () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Account deleted successfully', life: 3000 });
        this.displayDeleteDialog = false;
        this.authService.logout();
      },
      error => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Error: Wrong Password', life: 3000 });
        this.displayDeleteDialog = false;
      }
    );
  }

  onUpload(event: any) {
    const file = event.files[0];
    const formData = new FormData();
    formData.append('file', file);

    this.userService.uploadCurrent(formData).subscribe(
      response => {
        const url = window.URL.createObjectURL(file);
        this.userPhotoUrl = this.sanitizer.bypassSecurityTrustUrl(url);
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Photo changed successfully', life: 3000 });
      },
      error => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Photo upload failed', life: 3000 });
      }
    );
  }

  deletePhoto() {
    this.userService.deleteCurrentUserPhoto().subscribe(
      response => {
        this.userPhotoUrl = null;
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Photo deleted successfully', life: 3000 });
      },
      error => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Photo deletion failed', life: 3000 });
      }
    );
  }

  getTotalVersions(): number {
    return this.projects.reduce((total, project) => total + (project.versions ? project.versions.length : 0), 0);
  }

  getTotalExecutions(): number {
    return this.projects.reduce((total, project) => {
      return total + (project.versions ? project.versions.reduce((verTotal, version) => verTotal + (version.executions ? version.executions.length : 0), 0) : 0);
    }, 0);
  }

  getTotalChanges(): number {
    return this.projects.reduce((total, project) => {
      return total + (project.versions ? project.versions.reduce((verTotal, version) => {
        return verTotal + (version.changes ? version.changes.length : 0);
      }, 0) : 0);
    }, 0);
  }

  changePassword(form: NgForm) {
    if (this.newPassword !== this.confirmPassword) {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Passwords do not match', life: 3000 });
        return;
    }

    this.userService.changePassword(this.oldPassword, this.newPassword).subscribe(
        (response) => {
            this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Password changed successfully', life: 3000 });
            this.displayChangePasswordDialog = false;
            form.resetForm();  // Reset form and validation states
        },
        (error) => {
            const errorMessage = error.error?.message || 'Failed to change password';
            this.messageService.add({ severity: 'error', summary: 'Error', detail: errorMessage, life: 3000 });
            this.displayChangePasswordDialog = false;
            form.resetForm();  // Reset form and validation states
        }
    );
}


  private resetPasswordFields() {
    this.oldPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
  }
}
