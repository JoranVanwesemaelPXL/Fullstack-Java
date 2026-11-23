package be.foodexpress.recipeservice.web;

import be.foodexpress.recipeservice.exception.ApiException;

import java.util.Arrays;

/**
 * De frontend stuurt bij elk request X-User en X-Role mee (geen echte authenticatie, zie case).
 * Hier lezen we de rol uit en controleren we of die volstaat.
 */
public final class UserHeaders {

    public static final String USER = "X-User";
    public static final String ROLE = "X-Role";

    public static final String KLANT = "KLANT";
    public static final String MEDEWERKER = "MEDEWERKER";
    public static final String CHAUFFEUR = "CHAUFFEUR";

    private UserHeaders() {
    }

    /** Gooit 403 als de rol uit de header niet een van de toegelaten rollen is. */
    public static void requireRole(String role, String... allowed) {
        boolean ok = role != null && Arrays.stream(allowed).anyMatch(a -> a.equalsIgnoreCase(role.trim()));
        if (!ok) {
            throw ApiException.forbidden("Deze actie vereist rol " + String.join(" of ", allowed) + ".");
        }
    }
}
