import { Component, OnInit } from '@angular/core';
import { Version } from 'src/app/models/version.model';
import { VersionService } from 'src/app/services/version.service';
import { ProjectService } from 'src/app/services/project.service';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { Router } from '@angular/router';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DataViewModule } from 'primeng/dataview';
import { PickListModule } from 'primeng/picklist';
import { DropdownModule } from 'primeng/dropdown';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { RippleModule } from 'primeng/ripple';
import { Project } from 'src/app/models/project.model';
import { Location } from '@angular/common';

@Component({
  selector: 'app-versions',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ToastModule,
    ToolbarModule,
    ButtonModule,
    DialogModule,
    DataViewModule,
    PickListModule,
    DropdownModule,
    FormsModule,
    InputTextModule,
    RippleModule
  ],
  templateUrl: './versions.component.html',
  providers: [MessageService],
})
export class VersionsComponent implements OnInit {
  versions: Version[] = [];
  projects: Project[] = [];
  selectedProject: any;
  cols: any[] = [];
  rowsPerPageOptions = [5, 10, 20];
  deleteVersionDialog: boolean = false;
  deleteVersionsDialog: boolean = false;
  versionDialog: boolean = false;
  versionToDelete: Version;
  selectedVersions: Version[] = [];
  version: Version = new Version();

  constructor(
    private versionService: VersionService,
    private projectService: ProjectService,
    private messageService: MessageService,
    private router: Router,
  ) { }

  ngOnInit() {
    this.fetchVersions();
    this.fetchProjects();
    this.cols = [
      { field: 'id', header: 'ID' },
      { field: 'fichierOpenAPI', header: 'Fichier OpenAPI' },
      { field: 'fichierPostmanCollection', header: 'Fichier Postman Collection' },
      { field: 'changes', header: 'Modifications' },
      { field: 'executions', header: 'Exécutions' }
    ];
  }

  fetchVersions() {
    this.versionService.getAllVersions().subscribe(
      data => {
        this.versions = data;
      },
      error => {
        this.router.navigate(['/error']);
      }
    );
  }

  fetchProjects() {
    this.projectService.getAllProjects().subscribe(
      data => {
        this.projects = data;
      },
      error => {
        console.error('Erreur lors de la récupération des projets:', error);
      }
    );
  }


  deleteVersion(version: Version) {
    this.versionToDelete = version;
    this.deleteVersionDialog = true;
  }

  confirmDelete() {
    this.deleteVersionDialog = false;
    this.versionService.deleteVersion(this.versionToDelete.id).subscribe(() => {
      this.versions = this.versions.filter(version => version.id !== this.versionToDelete.id);
      this.messageService.add({ severity: 'warn', summary: 'Version supprimée', detail: 'Version supprimée avec succès' });
    });
  }

  deleteSelectedVersions() {
    if (this.selectedVersions.length === 0) {
      this.messageService.add({ severity: 'warn', summary: 'Aucune version sélectionnée', detail: 'Veuillez sélectionner des versions à supprimer.', life: 3000 });
      return;
    }
    this.deleteVersionsDialog = true;
  }

  confirmDeleteSelected() {
    this.deleteVersionsDialog = false;
    this.versionService.deleteMultipleVersions(this.selectedVersions).subscribe(
      () => {
        this.messageService.add({ severity: 'success', summary: 'Réussi', detail: 'Versions supprimées', life: 3000 });
        this.versions = this.versions.filter(val => !this.selectedVersions.includes(val));
        this.selectedVersions = [];
      },
      error => {
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Erreur lors de la suppression des versions : ' + error.message, life: 3000 });
      }
    );
  }

  cancelDelete() {
    this.deleteVersionDialog = false;
  }

  cancelDeleteSelected() {
    this.deleteVersionsDialog = false;
  }

  editVersion(version: Version) {
    const project = this.projects.find(p => p.versions.some(v => v.id === version.id));
    if (project) {
      this.router.navigate(['/admin/versions', project.id, version.id]);
    } else {
      this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Projet non trouvé pour cette version' });
    }
  }

  openNew() {
    this.version = new Version();
    this.selectedProject = null;
    this.versionDialog = true;
  }

  saveVersion() {
    if (this.selectedProject) {
      this.projectService.addVersionAndAffectProject(this.selectedProject.id, this.version).subscribe((addedVersion: Version) => {
        this.versions.push(addedVersion);
        this.messageService.add({ severity: 'success', summary: 'Version ajoutée', detail: 'Version ajoutée avec succès' });
        this.versionDialog = false;
      }, error => {
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Erreur lors de l\'ajout de la version : ' + error.message });
      });
    } else {
      this.messageService.add({ severity: 'warn', summary: 'Aucun projet sélectionné', detail: 'Veuillez sélectionner un projet.', life: 3000 });
    }
  }

  hideDialog() {
    this.versionDialog = false;
  }

  downloadPostmanCollection(version: Version): void {
    if (version.fichierPostmanCollection) {
      /*
      this.projectService.downloadPostmanCollection(version.projectId, version.id).subscribe((response: Blob) => {
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
      }); */
    } 
  }

  downloadOpenAPIFile(version: Version): void {
    if (version.fichierOpenAPI) { /*
      this.projectService.downloadOpenAPIFile(version.projectId, version.id).subscribe((response: Blob) => {
        const url = window.URL.createObjectURL(response);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = 'OpenAPIFile.yaml';
        anchor.style.display = 'none';
        document.body.appendChild(anchor);
        anchor.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(anchor);
      }, error => {
        console.error('Erreur lors du téléchargement du fichier:', error);
      }); */
    }
  }
}
