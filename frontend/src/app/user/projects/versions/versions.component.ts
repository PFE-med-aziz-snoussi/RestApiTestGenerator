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


@Component({
  selector: 'app-versions',
  standalone: true,
  imports: [CommonModule, TableModule, ToastModule, ToolbarModule, ButtonModule, DialogModule,DataViewModule,
		PickListModule,],
  templateUrl: './versions.component.html',
  providers: [MessageService],
  styleUrl: './versions.component.scss'
})
export class VersionsComponent implements OnInit {
  versions: Version[] = [];
  cols: any[] = [];
  rowsPerPageOptions = [5, 10, 20];
  deleteVersionDialog: boolean = false;
  deleteVersionsDialog: boolean = false;
  versionToDelete: Version;
  selectedVersions: Version[] = [];
  projectId: number;

  constructor(
    private versionService: VersionService,
    private projectService: ProjectService,
    private messageService: MessageService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit() {
    this.projectId = +this.route.snapshot.paramMap.get('projectId')!;
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
    this.router.navigate(['/user/projects/version/', this.projectId, version.id]);
  }

  addNewVersion() {
    this.projectService.addVersion(this.projectId).subscribe((addedVersion: Version) => {
      this.versions.push(addedVersion);
      this.messageService.add({ severity: 'success', summary: 'Version added', detail: 'Version added successfully' });
    });
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