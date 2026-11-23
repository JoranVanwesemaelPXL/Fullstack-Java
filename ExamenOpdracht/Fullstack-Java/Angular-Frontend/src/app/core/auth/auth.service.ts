import { Injectable, computed, signal } from '@angular/core';
import { Role, User } from '../models/user.model';

const STORAGE_KEY = 'foodexpress.user';

/**
 * Houdt bij wie er ingelogd is en met welke rol.
 * Volgens de case is er geen echte authenticatie: de gebruiker kiest een naam
 * en een rol op het loginscherm. De authInterceptor stuurt die mee als headers.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _user = signal<User | null>(this.readFromStorage());

  readonly user = this._user.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null);
  readonly role = computed(() => this._user()?.role ?? null);

  login(username: string, role: Role): void {
    const user: User = { username: username.trim(), role };
    this._user.set(user);
    this.writeToStorage(user);
  }

  logout(): void {
    this._user.set(null);
    this.writeToStorage(null);
  }

  hasRole(...roles: Role[]): boolean {
    const role = this.role();
    return role !== null && roles.includes(role);
  }

  private readFromStorage(): User | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as User) : null;
    } catch {
      return null;
    }
  }

  private writeToStorage(user: User | null): void {
    try {
      if (user) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
    } catch {
      // localStorage niet beschikbaar: de login blijft dan enkel in het geheugen.
    }
  }
}
