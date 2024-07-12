import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { AuthService } from '../services/auth.service';
import { ButtonModule } from 'primeng/button';
import { LayoutService } from './service/app.layout.service';
import { MenubarModule } from 'primeng/menubar';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-topbar',
  templateUrl: './app.topbar.component.html',

})
export class AppTopBarComponent {

    items!: MenuItem[];
    userPhotoUrl: SafeUrl | null = null;

    @ViewChild('menubutton') menuButton!: ElementRef;

    @ViewChild('topbarmenubutton') topbarMenuButton!: ElementRef;

    @ViewChild('topbarmenu') menu!: ElementRef;

    constructor(public layoutService: LayoutService,private authService: AuthService,private userService: UserService, private sanitizer: DomSanitizer) {}

    ngOnInit(): void {
        this.userService.getCurrentUserPhoto().subscribe(
          photoBlob => {
            const url = window.URL.createObjectURL(photoBlob);
            this.userPhotoUrl = this.sanitizer.bypassSecurityTrustUrl(url);
          },
          error => {
            console.error('Error fetching user photo:', error);
          }
        );

      }

    logout(): void {
        this.authService.logout();
      
    }
}
