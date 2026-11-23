package be.foodexpress.recipeservice.domain;

import java.util.Locale;

/** De 14 allergenen die volgens de EU-regelgeving vermeld moeten worden. */
public enum Allergen {
    GLUTEN,
    SCHAALDIEREN,
    EI,
    VIS,
    PINDA,
    SOJA,
    MELK,
    NOTEN,
    SELDERIJ,
    MOSTERD,
    SESAM,
    SULFIET,
    LUPINE,
    WEEKDIEREN;

    /** Naam zoals de frontend ze ziet en doorstuurt, bv. "melk". */
    public String label() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Zet "melk" of "MELK" om naar MELK; gooit IllegalArgumentException bij een onbekende waarde. */
    public static Allergen fromLabel(String label) {
        return Allergen.valueOf(label.trim().toUpperCase(Locale.ROOT));
    }
}
