package be.foodexpress.recipeservice.repository;

import be.foodexpress.recipeservice.domain.Recipe;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /** Haalt de recepten van een week op, mét ingrediënten in één query (geen N+1 per recept). */
    @EntityGraph(attributePaths = {"ingredients", "ingredients.ingredient"})
    List<Recipe> findByWeekOrderByNameAsc(String week);

    @EntityGraph(attributePaths = {"ingredients", "ingredients.ingredient"})
    Optional<Recipe> findWithIngredientsById(Long id);

    boolean existsByWeek(String week);
}
