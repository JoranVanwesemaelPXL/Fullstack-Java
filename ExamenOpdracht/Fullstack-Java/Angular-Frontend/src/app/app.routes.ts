import { Routes } from '@angular/router';

import { WelcomePage } from './pages/welcome/welcome-page';
import { Login } from './pages/login/login';
import { Menu } from './pages/menu/menu';
import { NewOrder } from './pages/new-order/new-order';
import { Orders } from './pages/orders/orders';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'welcome',
    pathMatch: 'full'
  },
  {
    path: 'welcome',
    component: WelcomePage
  },
  {
    path: 'login',
    component: Login
  },
  {
    path: 'menu',
    component: Menu
  },
  {
    path: 'new-order',
    component: NewOrder
  },
  {
    path: 'orders',
    component: Orders
  },
  {
    path: '**',
    redirectTo: 'welcome'
  }
];
