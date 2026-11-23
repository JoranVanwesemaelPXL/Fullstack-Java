package be.foodexpress.recipeservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecipeIngredientRequest(
        @NotNull Long ingredientId,
        @NotNull @Positive Double quantityPerPerson
) {
}
