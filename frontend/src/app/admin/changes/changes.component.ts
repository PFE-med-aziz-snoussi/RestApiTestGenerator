import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { Change } from 'src/app/models/change.model';
import { Version } from 'src/app/models/version.model'; // Ensure Version model is imported
import { ChangeService } from 'src/app/services/change.service';
import { VersionService } from 'src/app/services/version.service'; // Ensure Version service is imported

@Component({
  selector: 'app-change',
  templateUrl: './changes.component.html',
  providers: [MessageService]
})
export class ChangesComponent implements OnInit {
  changes: Change[] = [];
  selectedChanges: Change[] = [];
  changeDialog: boolean = false;
  deleteChangeDialog: boolean = false;
  deleteChangesDialog: boolean = false;
  change: Change = new Change(); 
  submitted: boolean = false;
  versions: Version[] = []; 
  selectedVersion: Version | null = null; // Ensure this is properly typed

  httpMethods = ['GET', 'POST', 'PUT', 'DELETE', 'N/A'];

  constructor(
    private changeService: ChangeService, 
    private messageService: MessageService,
    private versionService: VersionService 
  ) {}

  ngOnInit(): void {
    this.loadChanges();
    this.fetchVersions(); 
  }

  loadChanges() {
    this.changeService.getAllChanges().subscribe(data => {
      this.changes = data;
    });
  }

  fetchVersions() {
    this.versionService.getAllVersions().subscribe(data => {
      this.versions = data;
    });
  }

  openNewChange() {
    this.change = new Change(); 
    this.selectedVersion = null; // Reset selected version
    this.submitted = false;
    this.changeDialog = true;
  }

  editChange(change: Change) {
    this.change = { ...change };
    console.log(change)
    this.selectedVersion = this.versions.find(version => 
      version.changes.some(c => c.id === this.change.id)
    ) || null;    
    this.changeDialog = true;
  }

  saveChange(form: any) {
    this.submitted = true;
  
    if (form.valid) {
      if (this.change.id) {
        if (this.selectedVersion) {
          this.change.version = this.selectedVersion;
        } else {
          this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Veuillez sélectionner une version', life: 3000 });
          return;
        }
        this.changeService.updateChange(this.change.id, this.change).subscribe(() => {
          this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Changement mis à jour', life: 3000 });
          this.loadChanges();
          this.changeDialog = false;
        });
      } else {
        if (this.selectedVersion) {
          this.change.version = this.selectedVersion;
        } else {
          this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Veuillez sélectionner une version', life: 3000 });
          return;
        }
  
        this.changeService.createChange(this.change).subscribe(() => {
          this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Changement créé', life: 3000 });
          this.loadChanges();
          this.changeDialog = false;
        });
      }
    }
  }
  
  deleteChange(change: Change) {
    this.change = { ...change };
    this.deleteChangeDialog = true;
  }
  
  confirmDeleteChange() {
    this.changeService.deleteChange(this.change.id).subscribe(() => {
      this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Changement supprimé', life: 3000 });
      this.loadChanges();
      this.deleteChangeDialog = false;
    });
  }
  
  deleteSelectedChanges() {
    this.deleteChangesDialog = true;
  }
  
  confirmDeleteSelected() {
    const ids = this.selectedChanges.map(change => change.id);
    this.changeService.deleteMultipleChanges(ids).subscribe(() => {
      this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Changements supprimés', life: 3000 });
      this.loadChanges();
      this.deleteChangesDialog = false;
      this.selectedChanges = [];
    });
  }
  

  hideDialog() {
    this.changeDialog = false;
    this.submitted = false;
  }
}
