import { NgModule,LOCALE_ID  } from '@angular/core';
import { LocationStrategy, PathLocationStrategy } from '@angular/common';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { AppLayoutModule } from './layout/app.layout.module';
import { NotfoundComponent } from './auth/notfound/notfound.component';
import { AuthService } from './services/auth.service';
import { CookieService } from 'ngx-cookie-service';

import { httpInterceptorProviders } from './_helpers/http.interceptor';
import { HttpClientModule } from '@angular/common/http';
import { BrowserModule } from '@angular/platform-browser';
import { SpinnerComponent } from './spinner/spinner.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BlockUIModule } from 'ng-block-ui';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';

registerLocaleData(localeFr, 'fr');
@NgModule({
    declarations: [AppComponent, 
        NotfoundComponent,
        SpinnerComponent],
    imports: [AppRoutingModule, 
        AppLayoutModule,
        HttpClientModule, 
        BrowserModule,
        HttpClientModule,
        BrowserAnimationsModule,
        RouterModule.forRoot([]),
        ProgressSpinnerModule,
        BlockUIModule.forRoot(),],
    providers: [
        { provide: LOCALE_ID, useValue: 'fr' },
        { provide: LocationStrategy, useClass: PathLocationStrategy },
        AuthService,CookieService,
        httpInterceptorProviders
    ],
    bootstrap: [AppComponent],
})
export class AppModule {}
