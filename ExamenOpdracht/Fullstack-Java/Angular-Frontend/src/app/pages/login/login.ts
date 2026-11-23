import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ROLES, Role } from '../../core/models/user.model';

@Component({
  selector: 'app-login',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly roles = ROLES;
  protected readonly roleLabels: Record<Role, string> = {
    KLANT: 'Klant',
    MEDEWERKER: 'Medewerker',
    CHAUFFEUR: 'Chauffeur',
  };

  protected readonly form = inject(FormBuilder).nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(2)]],
    role: ['KLANT' as Role, Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { username, role } = this.form.getRawValue();
    this.auth.login(username, role);

    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/welcome';
    this.router.navigateByUrl(returnUrl);
  }
}
