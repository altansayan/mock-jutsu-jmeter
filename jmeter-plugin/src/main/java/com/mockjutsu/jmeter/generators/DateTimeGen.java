package com.mockjutsu.jmeter.generators;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/** Date/time data generator. Mirrors datetime_gen.py. */
public final class DateTimeGen {

    private DateTimeGen() {}

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter TIME_FMT     = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String generate(String type, String locale) {
        return generate(type, locale, "");
    }

    public static String generate(String type, String locale, String qualifier) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "past_date"       -> pastDate(rng);
            case "future_date"     -> futureDate(rng);
            case "date_between"    -> dateBetween(rng, qualifier);
            case "date_this_year"  -> dateThisYear(rng);
            case "date_this_month" -> dateThisMonth(rng);
            case "time_only"       -> timeOnly(rng);
            case "past_datetime"   -> pastDatetime(rng);
            case "future_datetime" -> futureDatetime(rng);
            default -> "ERROR: Unknown datetime type '" + type + "'";
        };
    }

    private static String pastDate(ThreadLocalRandom rng) {
        return LocalDate.now().minusDays(rng.nextInt(1, 3650)).format(DATE_FMT);
    }

    private static String futureDate(ThreadLocalRandom rng) {
        return LocalDate.now().plusDays(rng.nextInt(1, 3650)).format(DATE_FMT);
    }

    private static String dateBetween(ThreadLocalRandom rng, String qualifier) {
        LocalDate start = LocalDate.now().minusYears(5);
        LocalDate end   = LocalDate.now();
        if (!qualifier.isEmpty() && qualifier.contains("|")) {
            String[] parts = qualifier.split("\\|", 2);
            try { start = LocalDate.parse(parts[0].trim()); } catch (Exception ignored) {}
            if (parts.length > 1) try { end = LocalDate.parse(parts[1].trim()); } catch (Exception ignored) {}
        }
        long range = end.toEpochDay() - start.toEpochDay();
        if (range <= 0) return start.format(DATE_FMT);
        return start.plusDays(rng.nextLong(0, range + 1)).format(DATE_FMT);
    }

    private static String dateThisYear(ThreadLocalRandom rng) {
        LocalDate start = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate end   = LocalDate.now();
        long range = end.toEpochDay() - start.toEpochDay();
        if (range <= 0) return start.format(DATE_FMT);
        return start.plusDays(rng.nextLong(0, range)).format(DATE_FMT);
    }

    private static String dateThisMonth(ThreadLocalRandom rng) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end   = LocalDate.now();
        long range = end.toEpochDay() - start.toEpochDay();
        if (range <= 0) return start.format(DATE_FMT);
        return start.plusDays(rng.nextLong(0, range)).format(DATE_FMT);
    }

    private static String timeOnly(ThreadLocalRandom rng) {
        return String.format("%02d:%02d:%02d", rng.nextInt(24), rng.nextInt(60), rng.nextInt(60));
    }

    private static String pastDatetime(ThreadLocalRandom rng) {
        LocalDateTime dt = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(rng.nextLong(1, 3650L * 24 * 3600));
        return dt.format(DATETIME_FMT);
    }

    private static String futureDatetime(ThreadLocalRandom rng) {
        LocalDateTime dt = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(rng.nextLong(1, 3650L * 24 * 3600));
        return dt.format(DATETIME_FMT);
    }
}
