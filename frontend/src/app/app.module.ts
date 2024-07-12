import { NgModule } from '@angular/core';
import { HashLocationStrategy, LocationStrategy, PathLocationStrategy } from '@angular/common';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { AppLayoutModule } from './layout/app.layout.module';
import { NotfoundComponent } from './demo/components/notfound/notfound.component';
import { ProductService } from './demo/service/product.service';
import { CountryService } from './demo/service/country.service';
import { CustomerService } from './demo/service/customer.service';
import { EventService } from './demo/service/event.service';
import { IconService } from './demo/service/icon.service';
import { NodeService } from './demo/service/node.service';
import { PhotoService } from './demo/service/photo.service';
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
        { provide: LocationStrategy, useClass: PathLocationStrategy },
        CountryService, CustomerService, EventService, IconService, NodeService,
        PhotoService, ProductService,
        AuthService,CookieService,
        httpInterceptorProviders
    ],
    bootstrap: [AppComponent],
})
export class AppModule {}
