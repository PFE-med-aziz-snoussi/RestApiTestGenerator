import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Project } from '../models/project.model';
import { Version } from '../models/version.model';

import { environment } from 'src/environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  createProject(project: Project): Observable<any> {
    return this.http.post<Project>(`${this.apiUrl}/project/create`, project, { withCredentials: true });
  }

  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/project/allByAdmin`, { withCredentials: true });
  }

  getProjectById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/project/${id}`, { withCredentials: true });
  }

  updateProject(id: number, project: Project): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/project/${id}`, project, { withCredentials: true });
  }

  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/project/${id}`, { withCredentials: true });
  }

  deleteMultipleProjects(projects: Project[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/project/deleteMultiple`, projects, { withCredentials: true });
  }

  getMyProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/project/all`, { withCredentials: true });
  }

  generatePostmanCollection(projectId: number, versionId: number, requestBody: any): Observable<any> {
    console.log(requestBody)  ;  
    return this.http.post<any>(`${this.apiUrl}/project/generatepostmancoll/${projectId}/${versionId}`, requestBody, { withCredentials: true });
  }
  
  runNewman(projectId: number, versionId: number): Observable<any> {
    return this.http.post<string>(`${this.apiUrl}/project/newman/${projectId}/${versionId}`, {}, { withCredentials: true });
  }

  downloadPostmanCollection(projectId: number, versionId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/project/download/postman-collection/${projectId}/${versionId}`, { responseType: 'blob', withCredentials: true });
  }

  downloadOpenAPIFile(projectId: number, versionId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/project/download/openapi-file/${projectId}/${versionId}`, { responseType: 'blob', withCredentials: true });
  }

  getResultPostmanCollection(projectId: number, versionId: number, executionId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/project/result-postman-collection/${projectId}/${versionId}/${executionId}`, { responseType: 'blob', withCredentials: true });
  }

  getResultPostmanCollectionByexecution(executionId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/project/result-postman-collection-by-execution/${executionId}`, { responseType: 'blob', withCredentials: true });
  }

  deleteExecution(executionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/project/executions/${executionId}`, { withCredentials: true });
  }

  addVersion(projectId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/project/addVersion/${projectId}`, {}, { withCredentials: true });
  }

  uploadOpenAPIFile(file: File, projectId: number, versionId: number): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    formData.append('projectId', projectId.toString());
    formData.append('versionId', versionId.toString());
    return this.http.post<any>(`${this.apiUrl}/project/uploadOpenApi`, formData, { withCredentials: true });
  }

  addVersionAndAffectProject(projectId: number, version: Version): Observable<Version> {
    return this.http.post<Version>(`${this.apiUrl}/project/${projectId}/versions`, version, { withCredentials: true });
  }

}
