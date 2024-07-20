import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { passwordStrengthValidator } from '../validator/password-strength.validator';
import { MessageService } from 'primeng/api';
import { UserService } from 'src/app/services/user.service';


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  providers: [MessageService]
})
export class RegisterComponent {
  registerForm: FormGroup;
  isSuccessful = false;
  isSignUpFailed = false;
  errorMessage = '';
  selectedFileName: string | null = null;
  userImageUploaded = false;
  fileInputTouched = false;
  
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    private userService: UserService,

  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required,Validators.minLength(6), Validators.maxLength(40), passwordStrengthValidator()]],
      gender: ['', Validators.required],
      imageName: ['']
    });

  }

  onUpload(event: any): void {
    this.fileInputTouched = true;
    const file = event.files[0];
    if (file) {
      const formData = new FormData();
      formData.append('file', file, file.name);

      this.userService.uploadFile(formData).subscribe(
        (response) => {
          this.userImageUploaded = true;
          this.selectedFileName = file.name;
          this.messageService.add({ severity: 'info', summary: 'Image uploaded successfully', detail: file.name });
        },
        (error) => {
          this.userImageUploaded = false;
          this.messageService.add({ severity: 'error', summary: 'Image not uploaded', detail: file.name });
        }
      );
    }
    if (event.target) {
      event.target.value = null;
    }
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.isSignUpFailed = true;
      this.errorMessage = 'Please fill out the form correctly.';
      return;
    }

    const { username, email, password, gender, imageName } = this.registerForm.value;

    const user: User = {
      id: undefined,
      username,
      email,
      password,
      gender,
      imageName: this.selectedFileName,
      registrationDate: undefined,
      lastLoginDate: undefined,
      roles: undefined
    };
    console.log(user)
    this.authService.register(user).subscribe(
      response => {
        console.log('Registration successful:', response);
        this.isSuccessful = true;
        this.isSignUpFailed = false;
        this.router.navigate(['/login']);  
      },
      error => {
        console.error('Registration failed:', error);
        this.errorMessage = error.message || 'An error occurred during registration.';
        this.isSignUpFailed = true;
      }
    );
  }

  get f() {
    return this.registerForm.controls;
  }
}
