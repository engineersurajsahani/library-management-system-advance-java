package com.lms.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Shared fine calculation: ₹5 per day after due date. */
public final class FineUtil {

    public static final double PER_DAY = 5.0;

    private FineUtil() { }

    /** Current running fine for an active (unreturned) issue. */
    public static double currentFine(String dueDate) {
        try {
            if (dueDate == null) return 0;
            long late = ChronoUnit.DAYS.between(LocalDate.parse(dueDate), LocalDate.now());
            return Math.max(0, late) * PER_DAY;
        } catch (Exception e) {
            return 0;
        }
    }
}
