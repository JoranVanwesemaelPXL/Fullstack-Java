package be.foodexpress.recipeservice.web;

import be.foodexpress.recipeservice.dto.IngredientDto;
import be.foodexpress.recipeservice.dto.IngredientRequest;
import be.foodexpress.recipeservice.service.IngredientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public List<IngredientDto> getAll() {
        return ingredientService.findAll();
    }

    @GetMapping("/{id}")
    public IngredientDto getOne(@PathVariable Long id) {
        return ingredientService.findById(id);
    }

    @PostMapping
    public ResponseEntity<IngredientDto> create(
            @RequestHeader(value = UserHeaders.ROLE, required = false) String role,
            @Valid @RequestBody IngredientRequest request) {
        UserHeaders.requireRole(role, UserHeaders.MEDEWERKER);
        IngredientDto created = ingredientService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = UserHeaders.ROLE, required = false) String role,
            @PathVariable Long id) {
        UserHeaders.requireRole(role, UserHeaders.MEDEWERKER);
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
