import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { environment } from 'src/environments/environment.prod';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  providers: [MessageService],
})
export class UsersComponent implements OnInit {
  apiUrl = environment.apiUrl;
  userDialog: boolean = false;
  deleteUserDialog: boolean = false;
  deleteUsersDialog: boolean = false;
  editing: boolean = false;

  users: User[] = [];
  user: User = new User();
  selectedUsers: User[] = [];

  submitted: boolean = false;
  cols: any[] = [];
  userImageUploaded: boolean = false;
  genderOptions = ['HOMME', 'FEMME'];


  constructor(private userService: UserService, private messageService: MessageService) {
  }

  ngOnInit() {
    this.loadUsers();

    this.cols = [
      { field: 'id', header: 'ID' },
      { field: 'imagename', header: 'Image' },
      { field: 'username', header: 'Username' },
      { field: 'email', header: 'Email' },
      { field: 'gender', header: 'Gender' },
      { field: 'role', header: 'Role' },
      { field: 'registrationDate', header: 'Registration Date' },
      { field: 'lastLoginDate', header: 'Last Login Date' },
    ];

  }

  loadUsers() {
    this.userService.getUsers().subscribe((data) => {
      this.users = data;
    });
  }

  openNew() {
    this.user = new User();
    this.submitted = false;
    this.editing = false;
    this.userDialog = true;
  }

  onUpload(event: any) {
    const file = event.files[0];
    if (file) {
      const formData = new FormData();
      formData.append('file', file, file.name);

      this.userService.uploadFile(formData).subscribe(
        (response) => {
          this.userImageUploaded = true;
          this.user.imageName = file.name; 
          this.messageService.add({ severity: 'info', summary: 'Image upload successfully', detail: file.name });
        },
        (error) => {
          this.userImageUploaded = false;
          console.error('File Upload Error:', error);
          this.messageService.add({ severity: 'error', summary: 'image not uploaded', detail: file.name });
        }
      );
    }
    event.target.value = null;
  }

  editUser(user: User) {
    this.user = { ...user };
    this.submitted = false;
    this.editing = true;
    this.userDialog = true;
  }

  deleteUser(user: User) {
    this.deleteUserDialog = true;
    this.user = { ...user };
  }

  deleteSelectedUsers() {
    this.deleteUsersDialog = true;
  }

  confirmDeleteSelected() {
    const ids = this.selectedUsers.map((user) => user.id);
    this.userService.deleteUsers(ids).subscribe(() => {
      this.deleteUsersDialog = false;
      this.loadUsers();
      this.messageService.add({ severity: 'success', summary: 'Successful', detail: 'Users Deleted', life: 3000 });
      this.selectedUsers = [];
    });
  }

  confirmDelete() {
    this.userService.deleteUser(this.user.id).subscribe(() => {
      this.deleteUserDialog = false;
      this.loadUsers();
      this.messageService.add({ severity: 'success', summary: 'Successful', detail: 'User Deleted', life: 3000 });
      this.user = new User();
    });
  }

  hideDialog() {
    this.userDialog = false;
    this.submitted = false;
  }

  saveUser() {
    this.submitted = true;

    if (!this.isFormValid()) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to save user', life: 3000 });
      return;
    }

    if (this.editing) {
      this.userService.updateUser(this.user.id, this.user).subscribe(() => {
        this.loadUsers();
        this.messageService.add({ severity: 'success', summary: 'Successful', detail: 'User Updated', life: 3000 });
      });
    } else {
      this.userService.createUser(this.user).subscribe(() => {
        this.loadUsers();
        this.messageService.add({ severity: 'success', summary: 'Successful', detail: 'User Created', life: 3000 });
      });
    }
    this.userDialog = false;
    this.user = new User();
    this.editing = false;
    this.userImageUploaded = false;
   

  }

  isFormValid(): boolean {
    return !!this.user.username && !!this.user.email && !!this.user.password && !!this.user.gender;
  }

  onGlobalFilter(table: any, event: Event) {
    table.filterGlobal((event.target as HTMLInputElement).value, 'contains');
  }
}
