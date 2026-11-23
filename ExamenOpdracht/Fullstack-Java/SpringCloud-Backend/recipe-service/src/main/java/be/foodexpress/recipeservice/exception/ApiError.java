package be.foodexpress.recipeservice.exception;

import java.time.Instant;
import java.util.List;

/**
 * Het vaste foutobject dat elke service teruggeeft, bv.
 * { "status": 404, "code": "RECIPE_NOT_FOUND", "message": "Recept 99 bestaat niet.", "details": [], "timestamp": "..." }
 */
public record ApiError(
        int status,
        String code,
        String message,
        List<String> details,
        String timestamp
) {
    public static ApiError of(int status, String code, String message) {
        return of(status, code, message, List.of());
    }

    public static ApiError of(int status, String code, String message, List<String> details) {
        return new ApiError(status, code, message, details, Instant.now().toString());
    }
}
