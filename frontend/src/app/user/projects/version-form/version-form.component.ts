import { Version } from '../../../models/version.model';
import { Component, OnInit } from '@angular/core';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { VersionService } from 'src/app/services/version.service';
import { ProjectService } from 'src/app/services/project.service';
import { Change } from 'src/app/models/change.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RequestBodyDTO } from 'src/app/models/RequestBodyDTO.model';
import { Location } from '@angular/common';


@Component({
  selector: 'app-version-form',
  templateUrl: './version-form.component.html',
  styleUrls: ['./version-form.component.scss'],
  providers: [MessageService, ConfirmationService]
})
export class VersionFormComponent implements OnInit {
  currentStep: number = 0;
  openApiFile: File | null = null;
  postmanCollectionGenerated: boolean = false;

  routeItems: MenuItem[] = [];
  version: Version = new Version();

  ProgressBarvalue: number = 0;
  private progressSubscription: Subscription | null = null;

  projectId: number ;
  versionId: number ;

  changes: Change[] = [];
  loading: boolean = true;

  authForm: FormGroup;
  selectedAuthType: string = 'none';
  username: string = '';
  password: string = '';
  clientId: string = '';
  clientSecret: string = '';
  authFormUrl: string = '';

  authOptions = [
    { label: 'No Auth', value: 'none' }, 
    { label: 'Basic Auth', value: 'basic' },
    { label: 'OAuth2', value: 'oauth2' },
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private versionService: VersionService,
    private projectService: ProjectService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private formBuilder: FormBuilder,
    private location: Location,

  ) {
    this.routeItems = [
      { label: 'Étape 1 : Charger le fichier OpenAPI', command: () => this.onStepChange(0), disabled: true },
      { label: 'Étape 2 : Générer la collection Postman', command: () => this.onStepChange(1), disabled: true },
      { label: 'Étape 3 : Exécuter Newman', command: () => this.onStepChange(2), disabled: true },
      { label: 'Fin.', command: () => this.onStepChange(3), disabled: true }
    ];
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const projectId = params['projectId'];
      this.projectId = projectId; 

      const VersionId = params['id'];
      if (VersionId && VersionId !== '0') {
        this.versionId = VersionId; 
        this.fetchVersion(VersionId);
      } else {
        this.loading = false;
      }
    });

    this.authForm = this.formBuilder.group({
      authType: [this.selectedAuthType, Validators.required], 
      authFormUrl: [''], 
      username: [''],
      password: [''],
      clientId: [''],
      clientSecret: ['']
    });

    this.authForm.get('authType').valueChanges.subscribe((value) => {
      this.selectedAuthType = value;
      this.updateValidators();
    });
  }

  fetchVersion(VersionId: number): void {
    this.versionService.getVersionById(VersionId).subscribe(
      version => {
        this.version = version;
        this.changes = version.changes || [];
        this.loading = false;
        this.currentStep = this.calculateCurrentStep(version);
        this.updateRouteItems();
      },
      error => {
        this.loading = false;
        this.router.navigateByUrl('/user/projects');
      }
    );
  }

  calculateCurrentStep(version: Version): number {
    if (version.executions && version.executions.length > 0) {
      const lastExecution = version.executions[version.executions.length - 1];
      if (lastExecution.fichierResultCollection) {
        return 3;
      }
    }
    if (version.fichierPostmanCollection) {
      return 2;
    }
    if (version.fichierOpenAPI) {
      return 1;
    }
    return 0; 
  }
  

  onStepChange(step: number): void {
    if (!this.routeItems[step].disabled) {
      this.currentStep = step;
      sessionStorage.setItem('currentStep', this.currentStep.toString());
    }
  }


  onFileUpload(event: any): void {
    const file = event.files[0];
    if (file && file.name.endsWith('.yaml')) {
      this.openApiFile = file;
      this.messageService.add({ severity: 'info', summary: 'Fichier OpenAPI téléchargé avec succès', detail: file.name });
      this.routeItems[2].disabled = false;

      this.projectService.uploadOpenAPIFile(file, this.projectId, this.versionId).subscribe(
        response => {
          this.messageService.add({ severity: 'success', summary: 'Fichier Téléchargé', detail: 'Fichier OpenAPI téléchargé avec succès' });
          this.fetchVersion(this.version.id);
        },
        error => {
          this.messageService.add({ severity: 'error', summary: 'Erreur de Téléchargement', detail: 'Il y a eu une erreur lors du téléchargement du fichier' });
        }
      );
    } else {
      this.messageService.add({ severity: 'error', summary: 'Fichier Invalide', detail: 'Veuillez télécharger un fichier .yaml valide' });
    }
  }

  generatePostmanCollection(): void {
    if (this.version.id) {
      const requestBody: RequestBodyDTO = {
        authType: this.selectedAuthType,
        authFormUrl: this.authFormUrl || '',
        username: this.username || '',
        password: this.password || '',
        clientId: this.clientId || '',
        clientSecret: this.clientSecret || ''
      };
      this.projectService.generatePostmanCollection(this.projectId, this.version.id, requestBody).subscribe(
        response => {
          this.postmanCollectionGenerated = true;
          this.routeItems[3].disabled = false;
          this.messageService.add({ severity: 'info', summary: 'Collection Postman', detail: 'Collection Postman générée avec succès' });
          this.fetchVersion(this.version.id);
        },
        error => {
          this.messageService.add({ severity: 'error', summary: 'Erreur lors de la génération de la collection Postman', detail: error });
        }
      );
    } 
  }
  

  runNewman(): void {
    if (this.version.id) {
      this.startProgressBar();
      this.projectService.runNewman(this.projectId,this.version.id).subscribe(
        response => {
          this.messageService.add({ severity: 'info', summary: 'Exécution de Newman réussie', detail: 'L\'exécution de Newman a été complétée avec succès.' });
          this.completeProgressBar();
          this.onStepChange(3);
          this.fetchVersion(this.version.id);
        },
        error => {
          this.messageService.add({ severity: 'error', summary: 'Erreur lors de l\'exécution de Newman', detail: 'Il y a eu une erreur lors de l\'exécution de Newman.' });
          this.completeProgressBar();
        }
      );  
    }
  }


  startProgressBar(): void {
    this.ProgressBarvalue = 0;
    this.progressSubscription = interval(100).subscribe(() => {
      if (this.ProgressBarvalue < 90) {
        this.ProgressBarvalue += 1;
      }
    });
  }

  completeProgressBar(): void {
    if (this.progressSubscription) {
      this.progressSubscription.unsubscribe();
      this.progressSubscription = null;
    }
    this.ProgressBarvalue = 100;
  }

  nextStep(): void {
    if (this.currentStep < this.routeItems.length - 1) {
      this.onStepChange(this.currentStep + 1);
    }
  }

  prevStep(): void {
    if (this.currentStep > 0) {
      this.onStepChange(this.currentStep - 1);
    }
  }

  private updateRouteItems(): void {
    for (let i = 0; i <= this.currentStep; i++) {
      this.routeItems[i].disabled = false;
    }
  }

  downloadPostmanCollection(): void {
    if (this.version.id) {
      this.projectService.downloadPostmanCollection(this.projectId, this.version.id).subscribe((response: Blob) => {
        const url = window.URL.createObjectURL(response);

        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = 'ApiCollection.json';
        anchor.style.display = 'none';

        document.body.appendChild(anchor);

        anchor.click();

        window.URL.revokeObjectURL(url);
        document.body.removeChild(anchor);
      }, error => {
        console.error('Erreur lors du téléchargement du fichier:', error);
      });
    }
  }

  showConfirmationDialog() {
    this.confirmationService.confirm({
      key: 'showConfirmationDialog',
      message: 'Êtes-vous sûr de vouloir supprimer ?',
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Oui',
      rejectLabel: 'Non',
      accept: () => {
        this.deleteVersion(this.version.id);
      },
      reject: () => {
        
      }
    });
  }

  deleteVersion(id: number): void {
    this.versionService.deleteVersion(id).subscribe(
      () => {
               this.router.navigate(['/dashboard']);
      },
      error => {
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la suppression du projet.' });
      }
    );
  }


  clear(table: any): void {
    table.clear();
  }

  onGlobalFilter(table: any, event: Event): void {
    table.filterGlobal((event.target as HTMLInputElement).value, 'contains');
  }


  goBack() {
    this.location.back();
  }


    updateValidators(): void {
      const authFormUrlControl = this.authForm.get('authFormUrl');
      const usernameControl = this.authForm.get('username');
      const passwordControl = this.authForm.get('password');
      const clientIdControl = this.authForm.get('clientId');
      const clientSecretControl = this.authForm.get('clientSecret');
  
      // Clear existing validators
      authFormUrlControl.clearValidators();
      usernameControl.clearValidators();
      passwordControl.clearValidators();
      clientIdControl.clearValidators();
      clientSecretControl.clearValidators();
  
      if (this.selectedAuthType === 'basic') {
        // Apply validators for username and password for Basic Authentication
        usernameControl.setValidators([Validators.required]);
        passwordControl.setValidators([Validators.required]);
      } else if (this.selectedAuthType === 'oauth2') {
        // Apply validators for client ID and client secret for OAuth2 Authentication
        clientIdControl.setValidators([Validators.required]);
        clientSecretControl.setValidators([Validators.required]);
      }
  
      // Update the form control validators
      authFormUrlControl.updateValueAndValidity();
      usernameControl.updateValueAndValidity();
      passwordControl.updateValueAndValidity();
      clientIdControl.updateValueAndValidity();
      clientSecretControl.updateValueAndValidity();
    }

}
