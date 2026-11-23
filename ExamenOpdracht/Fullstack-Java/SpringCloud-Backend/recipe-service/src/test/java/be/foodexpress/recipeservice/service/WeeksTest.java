package be.foodexpress.recipeservice.service;

import be.foodexpress.recipeservice.domain.Allergen;
import be.foodexpress.recipeservice.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeksTest {

    @Test
    void geeftDeIsoWeek() {
        assertThat(Weeks.of(LocalDate.of(2026, 11, 23))).isEqualTo("2026-W48");
        assertThat(Weeks.of(LocalDate.of(2026, 1, 1))).isEqualTo("2026-W01");
    }

    @Test
    void nieuwjaarKanNogInDeLaatsteWeekVanVorigJaarVallen() {
        assertThat(Weeks.of(LocalDate.of(2027, 1, 1))).isEqualTo("2026-W53");
    }

    @Test
    void valideertHetFormaat() {
        assertThat(Weeks.isValid("2026-W48")).isTrue();
        assertThat(Weeks.isValid("2026-48")).isFalse();
        assertThat(Weeks.isValid(null)).isFalse();
    }

    @Test
    void parseAllergensAanvaardtLabelsEnWeigertOnbekende() {
        assertThat(IngredientService.parseAllergens(List.of("melk", "GLUTEN")))
                .containsExactlyInAnyOrder(Allergen.MELK, Allergen.GLUTEN);
        assertThat(IngredientService.parseAllergens(null)).isEmpty();

        assertThatThrownBy(() -> IngredientService.parseAllergens(List.of("chocolade")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("chocolade");
    }
}
