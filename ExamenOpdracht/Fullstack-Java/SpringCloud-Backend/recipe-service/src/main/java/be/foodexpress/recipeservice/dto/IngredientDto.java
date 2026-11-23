package be.foodexpress.recipeservice.dto;

import java.util.List;

public record IngredientDto(
        Long id,
        String name,
        String unit,
        List<String> allergens
) {
}
