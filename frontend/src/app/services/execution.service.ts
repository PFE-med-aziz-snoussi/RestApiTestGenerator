import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Execution } from '../models/execution.model';  // Import your Execution model
import { environment } from 'src/environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class ExecutionService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  createExecution(execution: Execution): Observable<Execution> {
    return this.http.post<Execution>(`${this.apiUrl}/executions/create`, execution, { withCredentials: true });
  }

  getAllExecutions(): Observable<Execution[]> {
    return this.http.get<Execution[]>(`${this.apiUrl}/executions/all`, { withCredentials: true });
  }

  getExecutionById(id: number): Observable<Execution> {
    return this.http.get<Execution>(`${this.apiUrl}/executions/${id}`, { withCredentials: true });
  }

  updateExecution(id: number, execution: Execution): Observable<Execution> {
    return this.http.put<Execution>(`${this.apiUrl}/executions/${id}`, execution, { withCredentials: true });
  }

  deleteExecution(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/executions/${id}`, { withCredentials: true });
  }

  deleteMultipleExecutions(ids: number[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/executions/deleteMultiple`, ids, { withCredentials: true });
  }
}
