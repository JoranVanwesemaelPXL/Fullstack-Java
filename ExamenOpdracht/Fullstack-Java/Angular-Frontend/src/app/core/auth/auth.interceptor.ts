import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

/**
 * Voegt aan elk request naar onze eigen API de headers X-User en X-Role toe.
 * De backend leest daaruit wie de gebruiker is (zie case: "Authenticatie").
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const user = inject(AuthService).user();

  if (!user || !req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        'X-User': user.username,
        'X-Role': user.role,
      },
    }),
  );
};
