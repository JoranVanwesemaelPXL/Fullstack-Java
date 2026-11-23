package be.foodexpress.recipeservice.service;

import java.time.LocalDate;
import java.time.temporal.IsoFields;

/** Hulpfuncties voor ISO-weken in de vorm "2026-W48". */
public final class Weeks {

    public static final String PATTERN = "\\d{4}-W\\d{2}";

    private Weeks() {
    }

    public static String of(LocalDate date) {
        int year = date.get(IsoFields.WEEK_BASED_YEAR);
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return String.format("%d-W%02d", year, week);
    }

    public static String current() {
        return of(LocalDate.now());
    }

    public static boolean isValid(String week) {
        return week != null && week.matches(PATTERN);
    }
}
