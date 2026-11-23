package be.foodexpress.recipeservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Koppeling recept ↔ ingrediënt, met de hoeveelheid per persoon. */
@Entity
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /** Hoeveelheid per persoon, in de eenheid van het ingrediënt. */
    @Column(nullable = false)
    private double quantityPerPerson;

    protected RecipeIngredient() {
        // voor JPA
    }

    RecipeIngredient(Recipe recipe, Ingredient ingredient, double quantityPerPerson) {
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.quantityPerPerson = quantityPerPerson;
    }

    public Long getId() {
        return id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public double getQuantityPerPerson() {
        return quantityPerPerson;
    }
}
