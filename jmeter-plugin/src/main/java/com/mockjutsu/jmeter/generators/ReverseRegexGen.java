package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** ReverseRegex — returns a plausible string matching common regex patterns. Mirrors reverse_regex.py. */
public final class ReverseRegexGen {
    private ReverseRegexGen() {}

    // A handful of common regex patterns with sample generators
    private static final String[][] PATTERNS = {
        {"[A-Z]{2}\\d{6}[A-Z]",     "PASSPORT"},
        {"\\d{4}-\\d{4}-\\d{4}-\\d{4}", "CARD_NUM"},
        {"[A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12}", "UUID"},
        {"\\+?[1-9]\\d{1,14}",        "PHONE"},
        {"[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}", "EMAIL"},
    };

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int idx = rng.nextInt(PATTERNS.length);
        String label = PATTERNS[idx][1];
        return switch (label) {
            case "PASSPORT" -> randomAlpha(rng, 2) + String.format("%06d", rng.nextInt(100000,999999)) + randomAlpha(rng, 1);
            case "CARD_NUM" -> String.format("%04d-%04d-%04d-%04d", rng.nextInt(1000,9999), rng.nextInt(1000,9999), rng.nextInt(1000,9999), rng.nextInt(1000,9999));
            case "UUID"     -> java.util.UUID.randomUUID().toString().toUpperCase();
            case "PHONE"    -> "+1" + String.format("%010d", rng.nextLong(1000000000L, 9999999999L));
            case "EMAIL"    -> CommunicationGen.email(rng, locale);
            default         -> java.util.UUID.randomUUID().toString();
        };
    }

    private static String randomAlpha(ThreadLocalRandom rng, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append((char)('A' + rng.nextInt(26)));
        return sb.toString();
    }
}
