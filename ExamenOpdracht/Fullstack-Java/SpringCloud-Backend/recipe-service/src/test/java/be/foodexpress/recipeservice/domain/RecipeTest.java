package be.foodexpress.recipeservice.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Unittest zonder Spring: de allergenen van een recept komen uit zijn ingrediënten. */
class RecipeTest {

    @Test
    void allergenenWordenAfgeleidUitDeIngredienten() {
        Recipe recipe = new Recipe("Pasta", "", 20, 2, "2026-W48", null);
        recipe.addIngredient(new Ingredient("penne", "g", Set.of(Allergen.GLUTEN)), 90);
        recipe.addIngredient(new Ingredient("pesto", "g", Set.of(Allergen.MELK, Allergen.NOTEN)), 30);
        recipe.addIngredient(new Ingredient("parmezaan", "g", Set.of(Allergen.MELK)), 20);
        recipe.addIngredient(new Ingredient("tomaat", "g", Set.of()), 100);

        assertThat(recipe.getAllergens())
                .containsExactlyInAnyOrder(Allergen.GLUTEN, Allergen.MELK, Allergen.NOTEN);
    }

    @Test
    void receptZonderIngredientenHeeftGeenAllergenen() {
        Recipe recipe = new Recipe("Water", "", 1, 1, "2026-W48", null);
        assertThat(recipe.getAllergens()).isEmpty();
    }

    @Test
    void allergeenLabelsWerkenInBeideRichtingen() {
        assertThat(Allergen.MELK.label()).isEqualTo("melk");
        assertThat(Allergen.fromLabel(" Melk ")).isEqualTo(Allergen.MELK);
    }
}
