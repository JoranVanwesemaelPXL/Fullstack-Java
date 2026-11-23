package be.foodexpress.recipeservice.dto;

import be.foodexpress.recipeservice.domain.Allergen;
import be.foodexpress.recipeservice.domain.Ingredient;
import be.foodexpress.recipeservice.domain.Recipe;
import be.foodexpress.recipeservice.domain.RecipeIngredient;

import java.util.Collection;
import java.util.List;

/** Zet entities om naar DTO's. Enkel hier hangt de DTO-vorm af van het databasemodel. */
public final class RecipeMapper {

    private RecipeMapper() {
    }

    public static RecipeDto toDto(Recipe recipe) {
        return new RecipeDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getPrepTimeMinutes(),
                recipe.getServings(),
                toLabels(recipe.getAllergens()),
                recipe.getIngredients().stream().map(RecipeMapper::toDto).toList(),
                recipe.getImageUrl(),
                recipe.getWeek()
        );
    }

    public static RecipeIngredientDto toDto(RecipeIngredient ri) {
        Ingredient ingredient = ri.getIngredient();
        return new RecipeIngredientDto(
                ingredient.getId(),
                ingredient.getName(),
                ri.getQuantityPerPerson(),
                ingredient.getUnit()
        );
    }

    public static IngredientDto toDto(Ingredient ingredient) {
        return new IngredientDto(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getUnit(),
                toLabels(ingredient.getAllergens())
        );
    }

    /** Allergenen als gesorteerde labels: ["gluten", "melk"]. */
    static List<String> toLabels(Collection<Allergen> allergens) {
        return allergens.stream().map(Allergen::label).sorted().toList();
    }
}
