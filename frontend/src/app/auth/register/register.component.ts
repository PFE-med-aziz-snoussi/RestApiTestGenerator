import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { passwordStrengthValidator } from '../validator/password-strength.validator';


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  registerForm: FormGroup;
  isSuccessful = false;
  isSignUpFailed = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router  
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required,Validators.minLength(6), Validators.maxLength(40), passwordStrengthValidator()]],
      gender: ['', Validators.required],
      imageName: ['']
    });

  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.isSignUpFailed = true;
      this.errorMessage = 'Please fill out the form correctly.';
      return;
    }

    const { username, email, password, gender, imageName } = this.registerForm.value;

    const user: User = {
      id:undefined,
      username,
      email,
      password,
      gender,
      imageName,
      registrationDate: undefined,
      lastLoginDate: undefined,
      role: undefined,
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
