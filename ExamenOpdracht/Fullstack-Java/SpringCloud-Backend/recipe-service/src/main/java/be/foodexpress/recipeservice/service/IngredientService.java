package be.foodexpress.recipeservice.service;

import be.foodexpress.recipeservice.domain.Allergen;
import be.foodexpress.recipeservice.domain.Ingredient;
import be.foodexpress.recipeservice.dto.IngredientDto;
import be.foodexpress.recipeservice.dto.IngredientRequest;
import be.foodexpress.recipeservice.dto.RecipeMapper;
import be.foodexpress.recipeservice.exception.ApiException;
import be.foodexpress.recipeservice.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<IngredientDto> findAll() {
        return ingredientRepository.findAllByOrderByNameAsc().stream().map(RecipeMapper::toDto).toList();
    }

    public IngredientDto findById(Long id) {
        return ingredientRepository.findById(id)
                .map(RecipeMapper::toDto)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional
    public IngredientDto create(IngredientRequest request) {
        String name = request.name().trim();
        if (ingredientRepository.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("INGREDIENT_EXISTS", "Ingrediënt '" + name + "' bestaat al.");
        }
        Ingredient ingredient = new Ingredient(name, request.unit().trim(), parseAllergens(request.allergens()));
        return RecipeMapper.toDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void delete(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw notFound(id);
        }
        // Wordt het nog gebruikt in een recept, dan geeft de database een fout → 409 via de handler.
        ingredientRepository.deleteById(id);
        ingredientRepository.flush();
    }

    static Set<Allergen> parseAllergens(List<String> labels) {
        Set<Allergen> result = EnumSet.noneOf(Allergen.class);
        if (labels == null) {
            return result;
        }
        for (String label : labels) {
            try {
                result.add(Allergen.fromLabel(label));
            } catch (IllegalArgumentException e) {
                throw ApiException.badRequest("UNKNOWN_ALLERGEN", "Onbekend allergeen: '" + label + "'.");
            }
        }
        return result;
    }

    private static ApiException notFound(Long id) {
        return ApiException.notFound("INGREDIENT_NOT_FOUND", "Ingrediënt " + id + " bestaat niet.");
    }
}
