import { AuthService } from './../../services/auth.service';
import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { Table } from 'primeng/table';
import { ProjectService } from '../../services/project.service';
import { Project } from '../../models/project.model';
import { Router } from '@angular/router';
import { User } from 'src/app/models/user.model';

@Component({
    templateUrl: './projects.component.html',
    providers: [MessageService]
})
export class ProjectsComponent implements OnInit {

    projectDialog: boolean = false;

    deleteProjectDialog: boolean = false;

    deleteProjectsDialog: boolean = false;

    projects: Project[] = [];

    project: Project = { nomDuProjet: '',description: ''};

    selectedProjects: Project[] = [];

    submitted: boolean = false;

    cols: any[] = [];

    rowsPerPageOptions = [5, 10, 20];

    isUpdating: boolean = false;

    currentUser: User | undefined;


    constructor(private projectService: ProjectService, private messageService: MessageService,private router: Router,private authService:AuthService) { }

    ngOnInit() {
        this.authService.getCurrentUser().subscribe(user => {
            this.currentUser = user;
        });
        this.projectService.getMyProjects().subscribe(data => this.projects = data);
        this.cols = [
            { field: 'id', header: 'ID' },
            { field: 'nomDuProjet', header: 'nomDuProjet' },
            { field: 'description', header: 'Description' },
            { field: 'version', header: 'Version' },

        ];

    }

    openNew() {
        this.project = { nomDuProjet: '',
        description: ''};
        this.submitted = false;
        this.projectDialog = true;
    }

    deleteSelectedProjects() {
        this.deleteProjectsDialog = true;
    }

    editProject(project: Project) {
        this.project = { ...project };
        this.projectDialog = true;
        this.isUpdating = true; 
    }
    
    deleteProject(project: Project) {
        this.project = { ...project };
        this.deleteProjectDialog = true;
    }

    confirmDeleteSelected() {
        this.deleteProjectsDialog = false;
        this.projectService.deleteMultipleProjects(this.selectedProjects).subscribe(
            () => {
                this.messageService.add({ severity: 'success', summary: 'Successful', detail: 'Projects Deleted', life: 3000 });
                this.projects = this.projects.filter(val => !this.selectedProjects.includes(val));
                this.selectedProjects = [];
            },
            (error) => {
                this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Error deleting projects: ' + error.message, life: 3000 });
            }
        );
    }


    confirmDelete() {
        this.deleteProjectDialog = false;
        this.projectService.deleteProject(this.project.id).subscribe(
            () => {
                this.messageService.add({ severity: 'success', summary: 'Successful', detail: 'Project Deleted', life: 3000 });
                this.projects = this.projects.filter(val => val.id !== this.project.id);
                this.project = { nomDuProjet: '', description: '' };
            },
            (error) => {
                this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Error deleting project: ' + error.message, life: 3000 });
            }
        );
    }
    

    hideDialog() {
        this.projectDialog = false;
        this.submitted = false;
    }

    saveProject() {
        this.submitted = true;
    
        if (this.project.nomDuProjet?.trim()) {
            if (this.project.id) {
                this.projectService.updateProject(this.project.id, this.project).subscribe(
                    () => {
                        this.projects[this.findIndexById(this.project.id)] = this.project;
                        this.messageService.add({ severity: 'success', summary: 'Successful', detail: 'Project Updated', life: 3000 });
                        this.projectService.getMyProjects().subscribe(data => this.projects = data);

                    },
                    (error) => {
                        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Error updating project: ' + error.message, life: 3000 });
                    }
                );
            } else {
                this.projectService.createProject(this.project).subscribe(
                    (newProject) => {
                        this.projects.push(newProject);
                        this.messageService.add({ severity: 'success', summary: 'Successful', detail: 'Project Created', life: 3000 });
                    },
                    (error) => {
                        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Error creating project: ' + error.message, life: 3000 });
                    }
                );
            }
    
            this.projectDialog = false;
            this.project = { nomDuProjet: '', description: '' };
        }
    }
    

    findIndexById(id: number): number {
        let index = -1;
        for (let i = 0; i < this.projects.length; i++) {
            if (this.projects[i].id === id) {
                index = i;
                break;
            }
        }

        return index;
    }

    onGlobalFilter(table: Table, event: Event) {
        table.filterGlobal((event.target as HTMLInputElement).value, 'contains');
    }

    navigateToVersions(projectId: number) {
        this.router.navigate([`/user/projects/${projectId}`]);
      }
}
