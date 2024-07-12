import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BlockService {
  private blockKey = 'isBlocked';

  setBlocked(isBlocked: boolean): void {
    localStorage.setItem(this.blockKey, JSON.stringify(isBlocked));
  }

  isBlocked(): boolean {
    return JSON.parse(localStorage.getItem(this.blockKey) || 'false');
  }
  
}
