
import { LayoutService } from 'src/app/layout/service/app.layout.service';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styles: [`
    :host ::ng-deep .pi-eye,
    :host ::ng-deep .pi-eye-slash {
      transform: scale(1.6);
      margin-right: 1rem;
      color: var(--primary-color) !important;
    }
  `]
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoginFailed = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    public layoutService: LayoutService, 

  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
      rememberMe: [false]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.isLoginFailed = true;
      this.errorMessage = 'Veuillez remplir le formulaire correctement.';
      return;
    }

    const { username, password } = this.loginForm.value;

    this.authService.login({ username, password }).subscribe(
      (response: HttpResponse<any>) => {
        console.log('Response from server:', response);
        // Handle the response here
        // For example, you can navigate to another page
        this.router.navigate(['/dashboard']);
      },
      error => {
        console.error('Error:', error);
        // Handle the error here
        this.errorMessage = 'Erreur lors de la connexion.';
        this.isLoginFailed = true;
      }
    );
  }

  get f() {
    return this.loginForm.controls;
  }
}
