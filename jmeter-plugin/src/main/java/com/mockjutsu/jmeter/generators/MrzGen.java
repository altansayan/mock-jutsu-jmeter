package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** MRZ — TD3 (passport) and TD1 (ID card) with correct check digits. Mirrors mrz.py. */
public final class MrzGen {
    private MrzGen() {}

    private static final int[] MRZ_VALUES = new int[128];
    static {
        for (char c = '0'; c <= '9'; c++) MRZ_VALUES[c] = c - '0';
        for (char c = 'A'; c <= 'Z'; c++) MRZ_VALUES[c] = c - 'A' + 10;
        MRZ_VALUES['<'] = 0;
    }
    private static final int[] WEIGHTS = {7, 3, 1};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "mrz_td3" -> td3(rng, locale);
            case "mrz_td1" -> td1(rng, locale);
            default -> "ERROR: Unknown MRZ type '" + type + "'";
        };
    }

    // ── TD3 — 2 lines × 44 chars (passport) ──────────────────────────────────

    static String td3(ThreadLocalRandom rng, String locale) {
        String cc     = localeToCC(locale);
        String fn     = IdentityGen.firstname(rng, locale, "").toUpperCase().replaceAll("[^A-Z]","");
        String ln     = IdentityGen.lastname(rng, locale, "").toUpperCase().replaceAll("[^A-Z]","");
        String number = randomAlphaNum(rng, 9);
        String dob    = mrz_date(rng, true);
        String expiry = mrz_date(rng, false);
        char   sex    = rng.nextBoolean() ? 'M' : 'F';

        // Line 1: P<CC<LN<<FN (padded to 44)
        String line1raw = "P<" + cc + "<" + ln + "<<" + fn;
        String line1    = pad(line1raw, 44);

        // Line 2: ICAO Doc 9303 TD3 — 44 chars exactly
        // number(9)+check(1)+cc(3)+dob(6)+check(1)+sex(1)+expiry(6)+check(1)+optional(14)+optionalChk(1)+composite(1)
        String numChk     = String.valueOf(mrzCheck(number));
        String dobChk     = String.valueOf(mrzCheck(dob));
        String expiryChk  = String.valueOf(mrzCheck(expiry));
        String personal   = pad("<", 14);
        String personalChk = "<";  // optional data — no numeric check
        String composite  = number + numChk + dob + dobChk + expiry + expiryChk + personal + personalChk;
        String compChk    = String.valueOf(mrzCheck(composite));
        String line2      = number + numChk + cc + dob + dobChk + sex + expiry + expiryChk + personal + personalChk + compChk;

        return line1 + "\n" + line2;
    }

    // ── TD1 — 3 lines × 30 chars (ID card) ───────────────────────────────────

    static String td1(ThreadLocalRandom rng, String locale) {
        String cc     = localeToCC(locale);
        String fn     = IdentityGen.firstname(rng, locale, "").toUpperCase().replaceAll("[^A-Z]","");
        String ln     = IdentityGen.lastname(rng, locale, "").toUpperCase().replaceAll("[^A-Z]","");
        String number = randomAlphaNum(rng, 9);
        String dob    = mrz_date(rng, true);
        String expiry = mrz_date(rng, false);
        char   sex    = rng.nextBoolean() ? 'M' : 'F';

        String numChk    = String.valueOf(mrzCheck(number));
        String dobChk    = String.valueOf(mrzCheck(dob));
        String expiryChk = String.valueOf(mrzCheck(expiry));

        String line1  = pad("I<" + cc + number + numChk, 30);
        String line2  = pad(dob + dobChk + sex + expiry + expiryChk + cc, 30);
        String line3  = pad(ln + "<<" + fn, 30);

        return line1 + "\n" + line2 + "\n" + line3;
    }

    // ── MRZ check digit ───────────────────────────────────────────────────────

    static int mrzCheck(String field) {
        int sum = 0;
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            int v = (c >= 0 && c < MRZ_VALUES.length) ? MRZ_VALUES[c] : 0;
            sum += v * WEIGHTS[i % 3];
        }
        return sum % 10;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String mrz_date(ThreadLocalRandom rng, boolean past) {
        int year  = past ? (50 + rng.nextInt(48)) : (25 + rng.nextInt(10));
        int month = rng.nextInt(1, 13);
        int day   = rng.nextInt(1, 29);
        return String.format("%02d%02d%02d", year, month, day);
    }

    private static String pad(String s, int len) {
        if (s.length() >= len) return s.substring(0, len);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append('<');
        return sb.toString();
    }

    private static String localeToCC(String locale) {
        return switch (locale) { case "TR" -> "TUR"; case "US" -> "USA"; case "DE" -> "DEU"; case "FR" -> "FRA"; case "UK" -> "GBR"; case "RU" -> "RUS"; default -> "TUR"; };
    }

    private static String randomAlphaNum(ThreadLocalRandom rng, int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }
}
