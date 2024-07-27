import { Component, OnInit } from '@angular/core';
import { Version } from 'src/app/models/version.model';
import { VersionService } from 'src/app/services/version.service';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ProjectService } from 'src/app/services/project.service';
import { DataViewModule } from 'primeng/dataview';
import { PickListModule } from 'primeng/picklist';
import { Project } from 'src/app/models/project.model';
import { DropdownModule } from 'primeng/dropdown';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-versions',
  standalone: true,
  imports: [CommonModule, TableModule, ToastModule, ToolbarModule, ButtonModule, DialogModule, DataViewModule, PickListModule,DropdownModule,FormsModule],
  templateUrl: './versions.component.html',
  providers: [MessageService],
  styleUrls: ['./versions.component.scss']
})
export class VersionsComponent implements OnInit {
  versions: Version[] = [];
  projects: Project[] = [];
  cols: any[] = [];
  rowsPerPageOptions = [5, 10, 20];
  deleteVersionDialog: boolean = false;
  deleteVersionsDialog: boolean = false;
  versionToDelete: Version;
  selectedVersions: Version[] = [];
  projectId: number;
  versionDialog: boolean = false;  
  selectedProject: Project;       

  constructor(
    private versionService: VersionService,
    private projectService: ProjectService,
    private messageService: MessageService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit() {
    this.projectId = +this.route.snapshot.paramMap.get('projectId')!;
    
    this.projectService.getMyProjects().subscribe(
      data => {
        this.projects = data;
      },
      error => {
        console.error('Error fetching projects:', error);
      }
    );
    
    this.fetchVersionsByProjectId(this.projectId);

    this.cols = [
      { field: 'id', header: 'ID' },
      { field: 'fichierOpenAPI', header: 'OpenAPI File' },
      { field: 'fichierPostmanCollection', header: 'Postman Collection File' },
      { field: 'project', header: 'Project' },
      { field: 'changes', header: 'Changes' },
      { field: 'executions', header: 'Executions' }
    ];
  }

  fetchVersionsByProjectId(projectId: number) {
    this.versionService.getVersionsByProjectId(projectId).subscribe(
        data => {
            this.versions = data;
            console.log(this.versions);

        },
        error => {
            this.router.navigate(['/error']);
        }
    );
  }

  goBack() {
    this.router.navigate(['/user/projects']);
  }

  deleteVersion(version: Version) {
    this.versionToDelete = version;
    this.deleteVersionDialog = true;
  }

  confirmDelete() {
    this.deleteVersionDialog = false;
    this.versionService.deleteVersion(this.versionToDelete.id).subscribe(() => {
      this.versions = this.versions.filter(version => version.id !== this.versionToDelete.id);
      this.messageService.add({ severity: 'warn', summary: 'Version deleted', detail: 'Version deleted successfully' });
    });
  }

  deleteSelectedVersions() {
    if (this.selectedVersions.length === 0) {
      this.messageService.add({ severity: 'warn', summary: 'No Versions Selected', detail: 'Please select versions to delete.', life: 3000 });
      return;
    }
    this.deleteVersionsDialog = true;
  }

  confirmDeleteSelected() {
    this.deleteVersionsDialog = false;
    this.versionService.deleteMultipleVersions(this.selectedVersions).subscribe(
      () => {
        this.messageService.add({ severity: 'success', summary: 'Successful', detail: 'Versions Deleted', life: 3000 });
        this.versions = this.versions.filter(val => !this.selectedVersions.includes(val));
        this.selectedVersions = [];
      },
      (error) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Error deleting versions: ' + error.message, life: 3000 });
      }
    );
  }

  cancelDelete() {
    this.deleteVersionDialog = false;
  }

  cancelDeleteSelected() {
    this.deleteVersionsDialog = false;
  }

  ModifyVersion(version: Version) {
    const project = this.projects.find(p => p.versions.some(v => v.id === version.id));
    if (project) {
      this.router.navigate(['/user/version/', project.id, version.id]);
    } else {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Project not found for the specified version.', life: 3000 });
    }
  }

  addNewVersion() {
    if (!this.projectId) {
      this.versionDialog = true;  // Show dialog for project selection
    } else {
      this.projectService.addVersion(this.projectId).subscribe((addedVersion: Version) => {
        this.versions.push(addedVersion);
        this.messageService.add({ severity: 'success', summary: 'Version added', detail: 'Version added successfully' });
	   this.fetchVersionsByProjectId(this.projectId);

      });
    }
  }

  hideDialog() {
    this.versionDialog = false;
  }

  saveVersion() {
    if (this.selectedProject) {
      this.projectService.addVersion(this.selectedProject.id).subscribe((addedVersion: Version) => {
        this.versions.push(addedVersion);
        this.messageService.add({ severity: 'success', summary: 'Version added', detail: 'Version added successfully' });
        this.versionDialog = false;  
        this.fetchVersionsByProjectId(this.projectId);

      });
    } else {
      this.messageService.add({ severity: 'warn', summary: 'No Project Selected', detail: 'Please select a project before saving.', life: 3000 });
    }
  }

  downloadPostmanCollection(version: any): void {
    if (version.fichierPostmanCollection) {
      this.projectService.downloadPostmanCollection(this.projectId, version.id).subscribe((response: Blob) => {
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
        console.error('Error downloading file:', error);
      });
    }
  }

  downloadOpenAPIFile(version: Version): void {
    if (version.fichierOpenAPI) {
      this.projectService.downloadOpenAPIFile(this.projectId, version.id).subscribe((response: Blob) => {
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
        console.error('Error downloading file:', error);
      });
    }
  }
}
