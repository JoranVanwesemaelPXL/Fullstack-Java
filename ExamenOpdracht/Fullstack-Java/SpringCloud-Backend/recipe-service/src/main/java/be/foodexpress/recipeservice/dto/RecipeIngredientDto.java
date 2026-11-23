package be.foodexpress.recipeservice.dto;

/** Een ingrediënt binnen een recept; quantity is per persoon. */
public record RecipeIngredientDto(
        Long ingredientId,
        String name,
        double quantity,
        String unit
) {
}
