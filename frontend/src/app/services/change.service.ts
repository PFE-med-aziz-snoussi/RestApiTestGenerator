import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Change } from '../models/change.model';  
import { environment } from 'src/environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class ChangeService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  createChange(change: Change): Observable<any> {
    return this.http.post<Change>(`${this.apiUrl}/changes/create`, change, { withCredentials: true });
  }

  getAllChanges(): Observable<Change[]> {
    return this.http.get<Change[]>(`${this.apiUrl}/changes/all`, { withCredentials: true });
  }

  getChangeById(id: number): Observable<Change> {
    return this.http.get<Change>(`${this.apiUrl}/changes/${id}`, { withCredentials: true });
  }

  updateChange(id: number, change: Change): Observable<any> {
    return this.http.put<Change>(`${this.apiUrl}/changes/${id}`, change, { withCredentials: true });
  }

  deleteChange(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/changes/${id}`, { withCredentials: true });
  }

  deleteMultipleChanges(ids: number[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/changes/deleteMultiple`, ids, { withCredentials: true });
  }
}
