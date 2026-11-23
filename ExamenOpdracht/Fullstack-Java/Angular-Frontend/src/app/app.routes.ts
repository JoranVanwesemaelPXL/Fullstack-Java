import { Routes } from '@angular/router';

import { authGuard, roleGuard } from './core/auth/auth.guard';
import { Login } from './pages/login/login';
import { Menu } from './pages/menu/menu';
import { NewOrder } from './pages/new-order/new-order';
import { Orders } from './pages/orders/orders';
import { WelcomePage } from './pages/welcome/welcome-page';

export const routes: Routes = [
  { path: '', redirectTo: 'welcome', pathMatch: 'full' },
  { path: 'welcome', component: WelcomePage },
  { path: 'login', component: Login },

  // Klant-pagina's: enkel na inloggen
  { path: 'menu', component: Menu, canActivate: [authGuard] },
  { path: 'new-order', component: NewOrder, canActivate: [authGuard, roleGuard('KLANT')] },
  { path: 'orders', component: Orders, canActivate: [authGuard, roleGuard('KLANT')] },

  { path: '**', redirectTo: 'welcome' },
];
