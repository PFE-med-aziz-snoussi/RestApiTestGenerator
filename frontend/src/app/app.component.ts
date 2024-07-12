import { Component, OnInit } from '@angular/core';
import { PrimeNGConfig } from 'primeng/api';
import { SpinnerService } from './services/Spinner.service';
import { Router, NavigationStart, NavigationEnd, NavigationCancel, NavigationError } from '@angular/router';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {

    constructor(private primengConfig: PrimeNGConfig,private router: Router,private spinnerService: SpinnerService) { 
        this.router.events.subscribe(event => {
            if (event instanceof NavigationStart) {
              this.spinnerService.show();
            } else if (event instanceof NavigationEnd || event instanceof NavigationCancel || event instanceof NavigationError) {
              this.spinnerService.hide();
            }
          });
    }

    ngOnInit() {
        this.primengConfig.ripple = true;
    }
}
