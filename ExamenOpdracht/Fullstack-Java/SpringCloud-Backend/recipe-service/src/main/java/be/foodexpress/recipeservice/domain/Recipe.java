package be.foodexpress.recipeservice.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Een recept op het weekmenu. */
@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int prepTimeMinutes;

    /** Standaard aantal personen. */
    @Column(nullable = false)
    private int servings;

    /** ISO-week waarin het recept op het menu staat, bv. "2026-W48". */
    @Column(nullable = false, length = 8)
    private String week;

    private String imageUrl;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    protected Recipe() {
        // voor JPA
    }

    public Recipe(String name, String description, int prepTimeMinutes, int servings, String week, String imageUrl) {
        this.name = name;
        this.description = description;
        this.prepTimeMinutes = prepTimeMinutes;
        this.servings = servings;
        this.week = week;
        this.imageUrl = imageUrl;
    }

    /** Voegt een ingrediënt toe; houdt beide kanten van de relatie in sync. */
    public void addIngredient(Ingredient ingredient, double quantityPerPerson) {
        ingredients.add(new RecipeIngredient(this, ingredient, quantityPerPerson));
    }

    public void clearIngredients() {
        ingredients.clear();
    }

    /** Allergenen van het recept = alle allergenen van zijn ingrediënten. */
    public Set<Allergen> getAllergens() {
        Set<Allergen> result = EnumSet.noneOf(Allergen.class);
        for (RecipeIngredient ri : ingredients) {
            result.addAll(ri.getIngredient().getAllergens());
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public void setPrepTimeMinutes(int prepTimeMinutes) {
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<RecipeIngredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }
}
