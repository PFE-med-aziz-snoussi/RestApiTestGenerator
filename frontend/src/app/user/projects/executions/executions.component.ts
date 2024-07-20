import { Component, OnInit, Input } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { VersionService } from 'src/app/services/version.service';
import { ProjectService } from 'src/app/services/project.service';
import { PaginatorModule } from 'primeng/paginator';
import { MessageService } from 'primeng/api';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-executions',
  standalone: true,
  templateUrl: './executions.component.html',
  styleUrls: ['./executions.component.scss'],
  imports: [ButtonModule, TableModule, CommonModule,PaginatorModule,PaginatorModule,MessageModule]
})
export class ExecutionsComponent implements OnInit {
  @Input() projectId: number;
  @Input() versionId: number;
  Data: any;
  totalIterations: number= 0;
  totalIterationsArray: any[] = [];
  executions: any[] = [];
  showOnlyFailures: boolean = false;
  currentExecutions: any[] = [];
  totalAssertions: number = 0;
  totalFailedAssertions: number= 0;
  CollectionName: string;
  TotalRunDuration: number;
  TotalDataReceived: string;
  AverageResponseTime: number;
  RequestsTotal: number;
  RequestsFailedTotal: number;
  prerequestScripts: number;
  prerequestScriptsFailed: number;
  testScripts: number;
  testScriptsFailed: number;
  testsFailed: number;
  failedTests: number = 0;
  testsPending: number;
  testsSkipped: number = 0;
  loading: boolean = true;
  summaryData: any[];
  currentTab: string = 'summary';
  selectedIteration: number = 0;
  expanded: boolean = false;
  isCollapsed: boolean[] = [];
  isAllCollapsed: boolean = true;
  formattedDate: string;
  paginatedExecutions: any[] = [];
  rows: number = 10;


  constructor(private versionService: VersionService, private projectService: ProjectService,    private messageService: MessageService,
  ) { }

  ngOnInit(): void {
    this.initializeIterations();
  }

  initializeIterations() {
    this.versionService.getVersionById(this.versionId).subscribe(response => {
      this.executions = response.executions;
      this.totalIterations = this.executions.length;
      this.totalIterationsArray = Array(this.totalIterations).fill(0).map((x, i) => i);
      this.loadIterationData(this.selectedIteration);

    }, error => {
      console.error('Failed to fetch executions', error);
    });
  }


  loadIterationData(iteration: number) {
    const executionId = this.executions[iteration].id;
    this.projectService.getResultPostmanCollection(this.projectId, this.versionId, executionId).subscribe(response => {
      const reader = new FileReader();
      reader.onload = () => {
        const jsonObject = JSON.parse(reader.result as string);
        this.Data = jsonObject;
        this.executions = jsonObject.run.executions;
        this.paginate({ first: 0, rows: this.rows });

        //console.log(jsonObject)

        this.currentExecutions = jsonObject.run.executions.map(execution => ({
          id: execution.id,
          totalAssertions: execution.assertions?.length || 0,
          totalFailedAssertions: execution.assertions?.filter(a => !a.passed).length || 0,
          timings: execution.timings,
          requestsTotal: execution.requests?.length || 0,
          requestsFailedTotal: execution.requests?.filter(r => !r.passed).length || 0,
          prerequestScripts: execution.prerequestScripts?.length || 0,
          prerequestScriptsFailed: execution.prerequestScripts?.filter(p => !p.passed).length || 0,
          testScripts: execution.testScripts?.length || 0,
          testScriptsFailed: execution.testScripts?.filter(t => !t.passed).length || 0,
          testsFailed: execution.tests?.filter(t => !t.passed).length || 0,
          testsPending: execution.tests?.filter(t => t.pending).length || 0,
          testsSkipped: this.calculateSkippedTests([execution])
        }));
        this.isCollapsed = new Array(jsonObject.run.executions.length).fill(true);

        this.failedTests =  jsonObject.run.failures.length;
        this.CollectionName = jsonObject.collection.info.name;
        this.TotalRunDuration = (jsonObject.run.timings.completed - jsonObject.run.timings.started) / 1000;
        this.TotalDataReceived = this.calculateTotalDataReceived(jsonObject.run.executions);
        this.AverageResponseTime = Math.floor(jsonObject.run.timings.responseAverage);

        this.RequestsTotal = jsonObject.run.stats.requests.total;
        this.RequestsFailedTotal = jsonObject.run.stats.requests.failed;
        this.prerequestScripts = jsonObject.run.stats.prerequestScripts.total;
        this.prerequestScriptsFailed = jsonObject.run.stats.prerequestScripts.failed;
        this.testScripts = jsonObject.run.stats.testScripts.total;
        this.testScriptsFailed = jsonObject.run.stats.testScripts.failed;
        this.testsFailed = jsonObject.run.stats.tests.failed;
        this.testsPending = jsonObject.run.stats.tests.pending;
        this.testsSkipped = this.calculateSkippedTests(jsonObject.run.executions);

        
        this.formattedDate = this.convertTimestampToDate(jsonObject.run.timings.completed);

        this.totalAssertions = jsonObject.run.stats.assertions?.total || 0;
        this.totalFailedAssertions = jsonObject.run.stats.assertions?.failed || 0;

        this.loading = false;
        this.loadSummaryData();
      };
      reader.readAsText(response);
    }, error => {
      console.error('Failed to load iteration data', error);
    });
  }

  calculateTotalDataReceived(executions: any[]): string {
    let totalSize = 0;
    executions.forEach(execution => {
      if (execution.response && execution.response.responseSize) {
        totalSize += execution.response.responseSize;
      }
    });

    if (totalSize > 1048576) {
      return (totalSize / 1048576).toFixed(2) + ' MB';
    } else if (totalSize > 1024) {
      return (totalSize / 1024).toFixed(2) + ' KB';
    } else {
      return totalSize + ' B';
    }
  }

  formatBytes(bytes: number): string {
    if (bytes === 0) return '0 O';
    const k = 1024;
    const sizes = ['O', 'KO', 'MO', 'GO', 'TO', 'PO'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  loadSummaryData() {
    this.summaryData = [
      { item: 'Requêtes', total: this.RequestsTotal, failed: this.RequestsFailedTotal },
      { item: 'Scripts Prérequis', total: this.prerequestScripts, failed: this.prerequestScriptsFailed },
      { item: 'Scripts de Test', total: this.testScripts, failed: this.testScriptsFailed },
      { item: 'Assertions', total: this.totalAssertions, failed: this.totalFailedAssertions },
      { item: 'Tests Ignorés', total: this.testsSkipped, failed: '-' }
    ];
}

  calculateSkippedTests(executions: any[]): number {
    let skippedCount = 0;
    executions.forEach(execution => {
      if (execution.assertions) {
        execution.assertions.forEach(assertion => {
          if (assertion.skipped) {
            skippedCount++;
          }
        });
      }
    });
    return skippedCount;
  }

  loadContent(tab: string) {
    this.currentTab = tab;
  }

  selectIteration(iteration: number) {
    this.selectedIteration = iteration;
    this.loadIterationData(iteration);
  }

  constructCompletedPath(urlObject: any): string {
    const protocol = urlObject.protocol;
    const host = urlObject.host.join('.');
    const path = urlObject.path.join('/');
    let queryString = '';
    
    if (urlObject.query.length > 0) {
      const queryParameters = urlObject.query.map(q => `${q.key}=${q.value}`).join('&');
      queryString = `?${queryParameters}`;
    }
    
    return `${protocol}://${host}/${path}${queryString}`;
  }

  calculatePassPercentage(assertions: any[]): number {
    const totalAssertions = assertions.length;
    const passedAssertions = assertions.filter(assertion => !assertion.error).length;
    return (passedAssertions / totalAssertions) * 100;
  }

  parseJsonFromStream(jsonStream: any): any {
    if (!jsonStream || !jsonStream.response) {
      return null; 
    }
    if (!jsonStream.response.stream) {
      return null; 
    }
    const byteArray = jsonStream.response.stream.data;
    const decoder = new TextDecoder('utf-8');
    const responseBody = decoder.decode(new Uint8Array(byteArray));
    const parsedJson = JSON.parse(responseBody);
    return parsedJson;
  }
  
  getTotal(assertions, type: string): number {
    return assertions.reduce((total, assert) => {
      if (type === 'passed' && !assert.skipped && !assert.error) {
        return total + 1;
      } else if (type === 'failed' && assert.error) {
        return total + 1;
      } else if (type === 'skipped' && assert.skipped) {
        return total + 1;
      }
      return total;
    }, 0);
  }

  getPrettyJson(json: string): string {
    if (!json) {
      return ""; 
    }
    const jsonObj = JSON.parse(json);
    return JSON.stringify(jsonObj, null, 2);
  }

  convertTimestampToDate(timestamp: number): string {
    const date = new Date(timestamp);
  
    // Date format options
    const dateOptions: Intl.DateTimeFormatOptions = {
      weekday: 'long',   // Full name of the weekday
      day: '2-digit',    // Day of the month (e.g., 01)
      month: 'long',      // Full name of the month (e.g., juillet)
      year: 'numeric',    // Full year (e.g., 2024)
    };
  
    const timeOptions: Intl.DateTimeFormatOptions = {
      hour: '2-digit',   
      minute: '2-digit',
      second: '2-digit' 
    };

    const formattedDate = date.toLocaleDateString('fr-FR', dateOptions);
    const formattedTime = date.toLocaleTimeString('fr-FR', { ...timeOptions, hour12: false });
      return `${formattedDate} ${formattedTime}`;
  }
  
  toggleExpandCollapse(): void {
    if (this.isAllCollapsed) {
      this.openAll();
    } else {
      this.closeAll();
    }
    this.isAllCollapsed = !this.isAllCollapsed;
  }
  
  openAll(): void {
    this.isCollapsed.fill(false);
  }
  
  closeAll(): void {
    this.isCollapsed.fill(true);
  }
  
  toggleCollapse(index: number): void {
    this.isCollapsed[index] = !this.isCollapsed[index];
    this.isAllCollapsed = this.isCollapsed.every(c => c);
  }

  toggleShowOnlyFailures() {
    this.showOnlyFailures = !this.showOnlyFailures;
  }

  copyToClipboard(value: string): void {
    const tempInput = document.createElement('textarea');
    tempInput.style.position = 'absolute';
    tempInput.style.left = '-9999px';
    tempInput.value = value;
    document.body.appendChild(tempInput);
    tempInput.select();
    try {
      document.execCommand('copy');
      this.messageService.add({ severity: 'info', summary: '', detail: 'Copied.' });

    } catch (err) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to copy' });

    }
    document.body.removeChild(tempInput);
  }

  paginate(event) {
    this.paginatedExecutions = this.executions.slice(event.first, event.first + event.rows);
  }
  
}
