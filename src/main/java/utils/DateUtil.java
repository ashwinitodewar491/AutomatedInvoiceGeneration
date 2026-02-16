package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class DateUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String[] getCurrentMonthRange() {

        LocalDate now = LocalDate.now();

        LocalDate from = now.withDayOfMonth(1);
        LocalDate to   = now.with(TemporalAdjusters.lastDayOfMonth());

        return new String[]{
                from.format(FORMATTER),
                to.format(FORMATTER)
        };
    }
}
