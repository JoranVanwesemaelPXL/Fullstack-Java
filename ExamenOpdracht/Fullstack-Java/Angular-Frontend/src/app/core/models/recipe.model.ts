// Contract met recipe-service (RecipeDto). Moet overeenkomen met openapi.yaml.

export interface RecipeIngredient {
  ingredientId: number;
  name: string;
  quantity: number; // hoeveelheid per persoon
  unit: string; // bv. 'g', 'ml', 'stuk'
}

export interface Recipe {
  id: number;
  name: string;
  description: string;
  prepTimeMinutes: number;
  servings: number; // standaard aantal personen
  allergens: string[];
  ingredients: RecipeIngredient[];
  imageUrl: string | null;
  week: string; // ISO-week, bv. '2026-W48'
}
