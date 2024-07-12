import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Version } from '../models/version.model';
import { environment } from 'src/environments/environment.prod';
import { Project } from '../models/project.model';

@Injectable({
  providedIn: 'root'
})
export class VersionService {
  private apiUrl = environment.apiUrl + '/versions';

  constructor(private http: HttpClient) { }

  createVersion(version: Version): Observable<Version> {
    return this.http.post<Version>(`${this.apiUrl}/create`, version,{ withCredentials: true });
  }

  getVersionById(id: number): Observable<Version> {
    return this.http.get<Version>(`${this.apiUrl}/${id}`,{ withCredentials: true });
  }

  getAllVersions(): Observable<Version[]> {
    return this.http.get<Version[]>(`${this.apiUrl}/`,{ withCredentials: true });
  }

  updateVersion(id: number, versionDetails: Version): Observable<Version> {
    return this.http.put<Version>(`${this.apiUrl}/${id}`, versionDetails,{ withCredentials: true });
  }

  deleteVersion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`,{ withCredentials: true });
  }

  getVersionsByProjectId(projectId: number): Observable<Version[]> {
    return this.http.get<Version[]>(`${this.apiUrl}/project/${projectId}`,{ withCredentials: true });
  }

  deleteMultipleVersions(versions: Version[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/deleteMultipleVersions`, versions, { withCredentials: true });
  }

  getProjectByVersionId(versionId: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/project/version/${versionId}`, { withCredentials: true });
  }
}