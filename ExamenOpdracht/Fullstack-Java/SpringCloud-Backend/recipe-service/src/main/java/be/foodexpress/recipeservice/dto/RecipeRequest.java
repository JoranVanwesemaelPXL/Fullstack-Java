package be.foodexpress.recipeservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Body voor POST en PUT /api/recipes. */
public record RecipeRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @NotNull @Positive Integer prepTimeMinutes,
        @NotNull @Min(1) @Max(12) Integer servings,
        @NotBlank @Pattern(regexp = "\\d{4}-W\\d{2}", message = "moet de vorm 2026-W48 hebben") String week,
        @Size(max = 500) String imageUrl,
        @NotEmpty List<@Valid @NotNull RecipeIngredientRequest> ingredients
) {
}
