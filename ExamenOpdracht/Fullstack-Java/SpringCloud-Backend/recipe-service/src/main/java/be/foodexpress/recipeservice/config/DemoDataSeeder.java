package be.foodexpress.recipeservice.config;

import be.foodexpress.recipeservice.domain.Allergen;
import be.foodexpress.recipeservice.domain.Ingredient;
import be.foodexpress.recipeservice.domain.Recipe;
import be.foodexpress.recipeservice.repository.IngredientRepository;
import be.foodexpress.recipeservice.repository.RecipeRepository;
import be.foodexpress.recipeservice.service.Weeks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static be.foodexpress.recipeservice.domain.Allergen.GLUTEN;
import static be.foodexpress.recipeservice.domain.Allergen.MELK;
import static be.foodexpress.recipeservice.domain.Allergen.MOSTERD;
import static be.foodexpress.recipeservice.domain.Allergen.NOTEN;
import static be.foodexpress.recipeservice.domain.Allergen.SELDERIJ;
import static be.foodexpress.recipeservice.domain.Allergen.SESAM;
import static be.foodexpress.recipeservice.domain.Allergen.SOJA;
import static be.foodexpress.recipeservice.domain.Allergen.VIS;

/**
 * Vult de database met demodata bij het opstarten:
 * - de ingrediëntencatalogus (als die nog leeg is);
 * - een weekmenu voor de HUIDIGE week (als die week nog geen recepten heeft).
 * Zo is het menu nooit leeg, ook niet als je het project een week later opnieuw start.
 * De ingrediënt-id's (1..21) gebruikt inventory-service als sleutel voor zijn voorraad.
 */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    public DemoDataSeeder(IngredientRepository ingredientRepository, RecipeRepository recipeRepository) {
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (ingredientRepository.count() == 0) {
            ingredientRepository.saveAll(demoIngredients());
            log.info("Ingrediëntencatalogus aangemaakt");
        }

        String week = Weeks.current();
        if (!recipeRepository.existsByWeek(week)) {
            Map<String, Ingredient> byName = new HashMap<>();
            ingredientRepository.findAll().forEach(i -> byName.put(i.getName(), i));
            recipeRepository.saveAll(demoRecipes(week, byName));
            log.info("Weekmenu voor {} aangemaakt", week);
        }
    }

    private static List<Ingredient> demoIngredients() {
        return List.of(
                ing("arborio", "g"),
                ing("champignons", "g"),
                ing("parmezaan", "g", MELK),
                ing("groentebouillon", "ml", SELDERIJ),
                ing("kipfilet", "g"),
                ing("kokosmelk", "ml"),
                ing("groene currypasta", "g", VIS),
                ing("sojasaus", "ml", SOJA, GLUTEN),
                ing("jasmijnrijst", "g"),
                ing("volkoren penne", "g", GLUTEN),
                ing("basilicumpesto", "g", MELK, NOTEN),
                ing("kerstomaatjes", "g"),
                ing("zalmfilet", "g", VIS),
                ing("zoete aardappel", "g"),
                ing("griekse yoghurt", "g", MELK),
                ing("rode linzen", "g"),
                ing("naan", "stuk", GLUTEN),
                ing("mosterdzaad", "g", MOSTERD),
                ing("quinoa", "g"),
                ing("falafel", "stuk"),
                ing("hummus", "g", SESAM)
        );
    }

    private static List<Recipe> demoRecipes(String week, Map<String, Ingredient> i) {
        Recipe risotto = new Recipe("Romige champignonrisotto",
                "Arborio met kastanjechampignons, parmezaan en verse tijm.", 35, 2, week, null);
        risotto.addIngredient(i.get("arborio"), 90);
        risotto.addIngredient(i.get("champignons"), 125);
        risotto.addIngredient(i.get("parmezaan"), 20);
        risotto.addIngredient(i.get("groentebouillon"), 250);

        Recipe curry = new Recipe("Thaise groene curry met kip",
                "Kip, kokosmelk, sperziebonen en jasmijnrijst.", 25, 2, week, null);
        curry.addIngredient(i.get("kipfilet"), 125);
        curry.addIngredient(i.get("kokosmelk"), 100);
        curry.addIngredient(i.get("groene currypasta"), 15);
        curry.addIngredient(i.get("sojasaus"), 10);
        curry.addIngredient(i.get("jasmijnrijst"), 75);

        Recipe pasta = new Recipe("Volkoren pasta pesto",
                "Met kerstomaatjes, rucola en geroosterde pijnboompitten.", 20, 2, week, null);
        pasta.addIngredient(i.get("volkoren penne"), 90);
        pasta.addIngredient(i.get("basilicumpesto"), 30);
        pasta.addIngredient(i.get("kerstomaatjes"), 100);

        Recipe zalm = new Recipe("Zalm met zoete aardappel",
                "Ovenzalm, zoete-aardappelpartjes en citroen-yoghurtsaus.", 30, 2, week, null);
        zalm.addIngredient(i.get("zalmfilet"), 120);
        zalm.addIngredient(i.get("zoete aardappel"), 200);
        zalm.addIngredient(i.get("griekse yoghurt"), 50);

        Recipe dahl = new Recipe("Linzendahl met naan",
                "Rode linzen, tomaat, spinazie en warme kruiden.", 30, 4, week, null);
        dahl.addIngredient(i.get("rode linzen"), 70);
        dahl.addIngredient(i.get("naan"), 1);
        dahl.addIngredient(i.get("mosterdzaad"), 2);
        dahl.addIngredient(i.get("kerstomaatjes"), 80);

        Recipe bowl = new Recipe("Buddha bowl met falafel",
                "Quinoa, hummus, geroosterde groenten en granaatappel.", 25, 2, week, null);
        bowl.addIngredient(i.get("quinoa"), 70);
        bowl.addIngredient(i.get("falafel"), 4);
        bowl.addIngredient(i.get("hummus"), 40);

        return List.of(risotto, curry, pasta, zalm, dahl, bowl);
    }

    private static Ingredient ing(String name, String unit, Allergen... allergens) {
        return new Ingredient(name, unit, Set.of(allergens));
    }
}
