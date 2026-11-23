import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { RecipeStore } from '../../core/stores/recipe.store';

/** US1 — Het weekmenu bekijken. */
@Component({
  selector: 'app-menu',
  imports: [RouterLink],
  templateUrl: './menu.html',
})
export class Menu implements OnInit {
  protected readonly store = inject(RecipeStore);
  protected readonly auth = inject(AuthService);

  ngOnInit(): void {
    this.store.loadWeekMenu();
  }
}
