import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let auth: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    auth = TestBed.inject(AuthService);
  });

  it('is standaard niet ingelogd', () => {
    expect(auth.isLoggedIn()).toBe(false);
    expect(auth.role()).toBeNull();
  });

  it('logt in met naam en rol en bewaart dat in localStorage', () => {
    auth.login('  jan ', 'KLANT');
    expect(auth.user()).toEqual({ username: 'jan', role: 'KLANT' });
    expect(auth.hasRole('KLANT')).toBe(true);
    expect(auth.hasRole('CHAUFFEUR')).toBe(false);
    expect(localStorage.getItem('foodexpress.user')).toContain('jan');
  });

  it('logt uit', () => {
    auth.login('jan', 'KLANT');
    auth.logout();
    expect(auth.isLoggedIn()).toBe(false);
    expect(localStorage.getItem('foodexpress.user')).toBeNull();
  });
});
