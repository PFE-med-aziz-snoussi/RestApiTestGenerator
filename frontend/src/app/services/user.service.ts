import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User } from '../models/user.model';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.prod';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {
    }

    getUsers(): Observable<User[]> {
      return this.http.get<User[]>(`${this.apiUrl}/user/list`, { withCredentials: true });
  }

  getUserById(id: number): Observable<any> {
    return this.http.get<User>(`${this.apiUrl}/user/${id}`, { withCredentials: true });
  }

  getCurrentUser(): Observable<any> {
    return this.http.get<User>(`${this.apiUrl}/user/current`, { withCredentials: true });
  }

  createUser(user: User): Observable<any> {
    return this.http.post<User>(`${this.apiUrl}/user/create`, user, { withCredentials: true });
  }

  updateUser(id: number, user: User): Observable<any> {
    return this.http.put<User>(`${this.apiUrl}/user/update/${id}`, user, { withCredentials: true });
  }

  updateCurrentUser(user: User): Observable<any> {
    return this.http.put<User>(`${this.apiUrl}/user/current/update`, user, { withCredentials: true });
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/user/delete/${id}`, { withCredentials: true });
  }

  deleteUsers(ids: number[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/user/deleteMultiple`, ids, { withCredentials: true });
  }

  deleteCurrentUser(password: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/user/current/delete`, {
      withCredentials: true,
      body: password
    });
  }

  uploadFile(formData: FormData): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/user/upload`, formData, { withCredentials: true });
  }

  uploadCurrent(formData: FormData): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/user/upload/current`, formData, { withCredentials: true });
  }

  getCurrentUserPhoto(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/user/photo`, { responseType: 'blob', withCredentials: true });
  }

  deleteCurrentUserPhoto(): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/user/photo/current`, { withCredentials: true });
  }

  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/user/change-password`, { oldPassword, newPassword }, { withCredentials: true });
  }

}
