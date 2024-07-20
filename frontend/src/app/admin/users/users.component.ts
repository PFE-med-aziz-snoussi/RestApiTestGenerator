import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { environment } from 'src/environments/environment.prod';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss'],
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
  fileInputTouched: boolean = false;
  genderOptions = ['HOMME', 'FEMME'];
  roleOptions = ['user', 'admin'];
  showPassword: boolean = false;

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
    this.fileInputTouched = false;
    this.editing = false;
    this.userDialog = true;
    this.showPassword = false;
  }

  onUpload(event: any) {
    this.fileInputTouched = true;
    const file = event.files[0];
    if (file) {
      const formData = new FormData();
      formData.append('file', file, file.name);

      this.userService.uploadFile(formData).subscribe(
        (response) => {
          this.userImageUploaded = true;
          this.user.imageName = file.name;
          this.messageService.add({ severity: 'info', summary: 'Image téléchargée avec succès', detail: file.name });
        },
        (error) => {
          this.userImageUploaded = false;
          this.messageService.add({ severity: 'error', summary: 'Image non téléchargée', detail: file.name });
        }
      );
    }
    if (event.target) {
      event.target.value = null;
    }
  }

  editUser(user: User) {
    this.user = { ...user };
    this.submitted = false;
    this.fileInputTouched = false;
    this.editing = true;
    this.userDialog = true;
    this.showPassword = false;
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
      this.messageService.add({ severity: 'success', summary: 'Réussi', detail: 'Utilisateurs supprimés', life: 3000 });
      this.selectedUsers = [];
    });
  }

  confirmDelete() {
    this.userService.deleteUser(this.user.id).subscribe(() => {
      this.deleteUserDialog = false;
      this.loadUsers();
      this.messageService.add({ severity: 'success', summary: 'Réussi', detail: 'Utilisateur supprimé', life: 3000 });
      this.user = new User();
    });
  }

  hideDialog() {
    this.userDialog = false;
    this.submitted = false;
    this.fileInputTouched = false;
    this.showPassword = false;
  }

  saveUser(userForm:any) {
    this.submitted = true;

    if (!this.isFormValid()) {
      this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de l\'enregistrement de l\'utilisateur', life: 3000 });
      return;
    }

    if (this.editing) {
      this.userService.updateUser(this.user.id, this.user).subscribe(() => {
        this.loadUsers();
        this.messageService.add({ severity: 'success', summary: 'Réussi', detail: 'Utilisateur mis à jour', life: 3000 });
      });
    } else {
      this.userService.createUser(this.user).subscribe(() => {
        this.loadUsers();
        this.messageService.add({ severity: 'success', summary: 'Réussi', detail: 'Utilisateur créé', life: 3000 });
      });
    }
    this.userDialog = false;
    this.user = new User();
    this.fileInputTouched = false;
    this.editing = false;
    this.userImageUploaded = false;
    this.showPassword = false;
  }

  isFormValid(): boolean {
    return !!this.user.username && !!this.user.email && !!this.user.password && !!this.user.gender;
  }

  onGlobalFilter(table: any, event: Event) {
    table.filterGlobal((event.target as HTMLInputElement).value, 'contains');
  }

  toggleShowPassword() {
    this.showPassword = !this.showPassword;
  }
}
