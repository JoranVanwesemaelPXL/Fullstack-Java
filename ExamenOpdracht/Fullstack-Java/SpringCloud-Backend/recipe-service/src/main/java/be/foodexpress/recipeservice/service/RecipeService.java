package be.foodexpress.recipeservice.service;

import be.foodexpress.recipeservice.domain.Ingredient;
import be.foodexpress.recipeservice.domain.Recipe;
import be.foodexpress.recipeservice.dto.RecipeDto;
import be.foodexpress.recipeservice.dto.RecipeIngredientRequest;
import be.foodexpress.recipeservice.dto.RecipeMapper;
import be.foodexpress.recipeservice.dto.RecipeRequest;
import be.foodexpress.recipeservice.exception.ApiException;
import be.foodexpress.recipeservice.repository.IngredientRepository;
import be.foodexpress.recipeservice.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    /** US1: het menu van een week; zonder week → de huidige week. */
    public List<RecipeDto> findByWeek(String week) {
        String w = (week == null || week.isBlank()) ? Weeks.current() : week.trim();
        if (!Weeks.isValid(w)) {
            throw ApiException.badRequest("INVALID_WEEK", "Week moet de vorm 2026-W48 hebben.");
        }
        return recipeRepository.findByWeekOrderByNameAsc(w).stream().map(RecipeMapper::toDto).toList();
    }

    public RecipeDto findById(Long id) {
        return recipeRepository.findWithIngredientsById(id)
                .map(RecipeMapper::toDto)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional
    public RecipeDto create(RecipeRequest request) {
        Recipe recipe = new Recipe(
                request.name().trim(),
                request.description(),
                request.prepTimeMinutes(),
                request.servings(),
                request.week(),
                request.imageUrl());
        addIngredients(recipe, request.ingredients());
        return RecipeMapper.toDto(recipeRepository.save(recipe));
    }

    @Transactional
    public RecipeDto update(Long id, RecipeRequest request) {
        Recipe recipe = recipeRepository.findWithIngredientsById(id).orElseThrow(() -> notFound(id));
        recipe.setName(request.name().trim());
        recipe.setDescription(request.description());
        recipe.setPrepTimeMinutes(request.prepTimeMinutes());
        recipe.setServings(request.servings());
        recipe.setWeek(request.week());
        recipe.setImageUrl(request.imageUrl());
        recipe.clearIngredients();
        recipeRepository.flush(); // eerst de oude koppelingen weg, dan de nieuwe erin
        addIngredients(recipe, request.ingredients());
        return RecipeMapper.toDto(recipe);
    }

    @Transactional
    public void delete(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw notFound(id);
        }
        recipeRepository.deleteById(id);
    }

    /** Zoekt alle ingrediënten in één query op; een onbekend id geeft 400. */
    private void addIngredients(Recipe recipe, List<RecipeIngredientRequest> lines) {
        List<Long> ids = lines.stream().map(RecipeIngredientRequest::ingredientId).distinct().toList();
        Map<Long, Ingredient> found = ingredientRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));

        for (RecipeIngredientRequest line : lines) {
            Ingredient ingredient = found.get(line.ingredientId());
            if (ingredient == null) {
                throw ApiException.badRequest("UNKNOWN_INGREDIENT",
                        "Ingrediënt " + line.ingredientId() + " bestaat niet.");
            }
            recipe.addIngredient(ingredient, line.quantityPerPerson());
        }
    }

    private static ApiException notFound(Long id) {
        return ApiException.notFound("RECIPE_NOT_FOUND", "Recept " + id + " bestaat niet.");
    }
}
