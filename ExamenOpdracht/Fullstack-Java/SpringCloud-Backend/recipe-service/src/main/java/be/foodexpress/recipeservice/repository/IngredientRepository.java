package be.foodexpress.recipeservice.repository;

import be.foodexpress.recipeservice.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
