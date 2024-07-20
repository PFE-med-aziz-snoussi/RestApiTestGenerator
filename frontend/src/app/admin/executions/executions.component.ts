import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Execution } from 'src/app/models/execution.model';
import { Project } from 'src/app/models/project.model';
import { Version } from 'src/app/models/version.model';
import { ExecutionService } from 'src/app/services/execution.service';
import { ProjectService } from 'src/app/services/project.service';
import { VersionService } from 'src/app/services/version.service'; // Assuming you have a service for versions

@Component({
  selector: 'app-admin-executions',
  templateUrl: './executions.component.html',
  providers: [MessageService]
})
export class ExecutionsComponent implements OnInit {
  executions: Execution[] = [];
  selectedExecutions: Execution[] = [];
  executionDialog: boolean = false;
  deleteExecutionDialog: boolean = false;
  deleteExecutionsDialog: boolean = false;
  execution: Execution = new Execution(); 
  submitted: boolean = false;
  projects: Project[] = [];
  versions: Version[] = []; // Array to store versions

  constructor(private executionService: ExecutionService, 
    private messageService: MessageService,
    private projectService: ProjectService,
    private versionService: VersionService, // Inject VersionService
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadExecutions();
    this.fetchProjects();
    this.fetchVersions(); // Call to load versions
  }

  loadExecutions() {
    this.executionService.getAllExecutions().subscribe(data => {
      this.executions = data;
    });
  }

  openNewExecution() {
    this.execution = new Execution(); 
    this.submitted = false;
    this.executionDialog = true;
  }

  editExecution(execution: Execution) {
    this.execution = { ...execution }; // Clone the selected execution
    this.executionDialog = true;
  }

  saveExecution(form: any) {
    this.submitted = true;

    if (form.valid) {
      if (this.execution.id) {
        this.executionService.updateExecution(this.execution.id, this.execution).subscribe(() => {
          this.messageService.add({ severity: 'success', summary: 'Réussi', detail: 'Exécution mise à jour', life: 3000 });
          this.loadExecutions();
          this.executionDialog = false;
        });
      } else {
        this.executionService.createExecution(this.execution).subscribe(() => {
          this.messageService.add({ severity: 'success', summary: 'Réussi', detail: 'Exécution créée', life: 3000 });
          this.loadExecutions();
          this.executionDialog = false;
        });
      }
    }
  }

  deleteExecution(execution: Execution) {
    this.execution = { ...execution };
    this.deleteExecutionDialog = true;
  }

  confirmDeleteExecution() {
    this.executionService.deleteExecution(this.execution.id).subscribe(() => {
      this.messageService.add({ severity: 'success', summary: 'Réussi', detail: 'Exécution supprimée', life: 3000 });
      this.loadExecutions();
      this.deleteExecutionDialog = false;
    });
  }

  deleteSelectedExecutions() {
    this.deleteExecutionsDialog = true;
  }

  confirmDeleteSelected() {
    const ids = this.selectedExecutions.map(execution => execution.id);
    this.executionService.deleteMultipleExecutions(ids).subscribe(() => {
      this.messageService.add({ severity: 'success', summary: 'Réussi', detail: 'Exécutions supprimées', life: 3000 });
      this.loadExecutions();
      this.deleteExecutionsDialog = false;
      this.selectedExecutions = [];
    });
  }

  hideDialog() {
    this.executionDialog = false;
    this.submitted = false;
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

  fetchVersions() {
    this.versionService.getAllVersions().subscribe(
      data => {
        this.versions = data;
      },
      error => {
        console.error('Erreur lors de la récupération des versions:', error);
      }
    );
  }

  navigateToExecutions(execution: Execution) {
    const version = this.versions.find(v => v.executions.some(e => e.id === execution.id));
    if (version) {
      const project = this.projects.find(p => p.versions.some(v => v.id === version.id));
      if (project) {
        this.router.navigate(['/admin/versions', project.id, version.id]);
      } else {
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Projet non trouvé pour cette version' });
      }
    } else {
      this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Version non trouvée pour cette exécution' });
    }
  }
}
