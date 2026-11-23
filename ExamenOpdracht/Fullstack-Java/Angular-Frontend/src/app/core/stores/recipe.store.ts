import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { firstValueFrom } from 'rxjs';
import { toApiError } from '../models/api-error.model';
import { Recipe } from '../models/recipe.model';
import { RecipeService } from '../services/recipe.service';

interface RecipeState {
  recipes: Recipe[];
  loading: boolean;
  error: string | null;
  excludedAllergens: string[]; // recepten met deze allergenen worden verborgen
}

const initialState: RecipeState = {
  recipes: [],
  loading: false,
  error: null,
  excludedAllergens: [],
};

/**
 * Bewaart de toestand van het weekmenu (US1).
 * De store gebruikt RecipeService om met de backend te communiceren.
 */
export const RecipeStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withComputed(({ recipes, excludedAllergens }) => ({
    /** Alle allergenen die in het menu voorkomen, alfabetisch. */
    allAllergens: computed(() =>
      [...new Set(recipes().flatMap((r) => r.allergens))].sort((a, b) => a.localeCompare(b)),
    ),
    /** Het menu zonder de recepten die een uitgesloten allergeen bevatten. */
    visibleRecipes: computed(() =>
      recipes().filter((r) => !r.allergens.some((a) => excludedAllergens().includes(a))),
    ),
  })),

  withMethods((store, recipeService = inject(RecipeService)) => ({
    async loadWeekMenu(): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        const recipes = await firstValueFrom(recipeService.getWeekMenu());
        patchState(store, { recipes, loading: false });
      } catch (e) {
        patchState(store, { loading: false, error: toApiError(e).message });
      }
    },

    toggleAllergen(allergen: string): void {
      const current = store.excludedAllergens();
      patchState(store, {
        excludedAllergens: current.includes(allergen)
          ? current.filter((a) => a !== allergen)
          : [...current, allergen],
      });
    },

    clearAllergenFilter(): void {
      patchState(store, { excludedAllergens: [] });
    },
  })),
);
