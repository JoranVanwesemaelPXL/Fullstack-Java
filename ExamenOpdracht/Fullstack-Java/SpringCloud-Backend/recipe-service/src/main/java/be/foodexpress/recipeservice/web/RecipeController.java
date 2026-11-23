package be.foodexpress.recipeservice.web;

import be.foodexpress.recipeservice.dto.RecipeDto;
import be.foodexpress.recipeservice.dto.RecipeRequest;
import be.foodexpress.recipeservice.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Lezen mag iedereen (US1: de klant bekijkt het weekmenu).
 * Aanmaken, wijzigen en verwijderen mag enkel een MEDEWERKER.
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /** US1 — GET /api/recipes?week=2026-W48 (zonder week: deze week). */
    @GetMapping
    public List<RecipeDto> getMenu(@RequestParam(required = false) String week) {
        return recipeService.findByWeek(week);
    }

    @GetMapping("/{id}")
    public RecipeDto getRecipe(@PathVariable Long id) {
        return recipeService.findById(id);
    }

    @PostMapping
    public ResponseEntity<RecipeDto> create(
            @RequestHeader(value = UserHeaders.ROLE, required = false) String role,
            @Valid @RequestBody RecipeRequest request) {
        UserHeaders.requireRole(role, UserHeaders.MEDEWERKER);
        RecipeDto created = recipeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public RecipeDto update(
            @RequestHeader(value = UserHeaders.ROLE, required = false) String role,
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request) {
        UserHeaders.requireRole(role, UserHeaders.MEDEWERKER);
        return recipeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = UserHeaders.ROLE, required = false) String role,
            @PathVariable Long id) {
        UserHeaders.requireRole(role, UserHeaders.MEDEWERKER);
        recipeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
