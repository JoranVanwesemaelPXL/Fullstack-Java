package be.foodexpress.recipeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Body voor POST /api/ingredients. Allergenen als labels, bv. ["melk", "gluten"]. */
public record IngredientRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 20) String unit,
        List<@NotBlank String> allergens
) {
}
