package be.foodexpress.recipeservice.dto;

import java.util.List;

/**
 * Het contract naar buiten (zie openapi.yaml en de Angular-interface Recipe).
 * Wijzigt de entity, dan blijft dit record hetzelfde.
 */
public record RecipeDto(
        Long id,
        String name,
        String description,
        int prepTimeMinutes,
        int servings,
        List<String> allergens,
        List<RecipeIngredientDto> ingredients,
        String imageUrl,
        String week
) {
}
