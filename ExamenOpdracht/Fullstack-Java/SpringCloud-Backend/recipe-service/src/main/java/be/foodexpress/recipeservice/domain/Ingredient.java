package be.foodexpress.recipeservice.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Een ingrediënt uit de catalogus (bv. "arborio").
 * Het id is ook de sleutel waarmee inventory-service zijn voorraad bijhoudt.
 * Allergenen horen bij het ingrediënt; de allergenen van een recept worden daaruit afgeleid.
 */
@Entity
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** Eenheid waarin het ingrediënt gemeten wordt: g, ml, stuk, ... */
    @Column(nullable = false)
    private String unit;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ingredient_allergen", joinColumns = @JoinColumn(name = "ingredient_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen", nullable = false)
    private Set<Allergen> allergens = new HashSet<>();

    protected Ingredient() {
        // voor JPA
    }

    public Ingredient(String name, String unit, Set<Allergen> allergens) {
        this.name = name;
        this.unit = unit;
        setAllergens(allergens);
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Set<Allergen> getAllergens() {
        return allergens;
    }

    public void setAllergens(Set<Allergen> allergens) {
        this.allergens.clear();
        if (allergens != null && !allergens.isEmpty()) {
            this.allergens.addAll(EnumSet.copyOf(allergens));
        }
    }
}
