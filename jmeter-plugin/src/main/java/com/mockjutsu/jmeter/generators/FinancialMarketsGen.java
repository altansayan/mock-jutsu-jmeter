package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** Capital markets — ISIN, CUSIP, SEDOL, LEI, FIX, PSD2. Mirrors financial_markets.py. */
public final class FinancialMarketsGen {

    private FinancialMarketsGen() {}

    private static final String[] COUNTRY_CODES = {"US","GB","DE","FR","TR","JP","CA","AU","CH","NL"};
    private static final char[] ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "isin"         -> isin(rng, locale);
            case "cusip"        -> cusip(rng);
            case "sedol"        -> sedol(rng);
            case "lei"          -> lei(rng);
            case "fix_message"  -> fixMessage(rng);
            case "psd2_consent" -> psd2Consent(rng);
            default             -> "ERROR: Unknown markets type '" + type + "'";
        };
    }

    // ── ISIN — ISO 6166: CC + 9 alphanumeric + Luhn check digit ──────────────

    static String isin(ThreadLocalRandom rng, String locale) {
        String cc = switch (locale) {
            case "TR" -> "TR"; case "DE" -> "DE"; case "FR" -> "FR";
            case "UK" -> "GB"; case "RU" -> "RU";
            default   -> COUNTRY_CODES[rng.nextInt(COUNTRY_CODES.length)];
        };
        StringBuilder body = new StringBuilder(cc);
        for (int i = 0; i < 9; i++) body.append(ALPHA_NUM[rng.nextInt(ALPHA_NUM.length)]);
        String isin12 = body.toString();

        // Convert to numeric string for Luhn
        StringBuilder numeric = new StringBuilder();
        for (char c : isin12.toCharArray()) {
            if (Character.isLetter(c)) numeric.append(c - 'A' + 10);
            else numeric.append(c);
        }
        // Luhn on the numeric string
        String num = numeric.toString();
        int sum = 0;
        boolean alt = false;
        for (int i = num.length() - 1; i >= 0; i--) {
            int d = num.charAt(i) - '0';
            if (alt) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
            alt = !alt;
        }
        int check = (10 - (sum % 10)) % 10;
        return isin12 + check;
    }

    // ── CUSIP — 9 chars (6 alpha, 2 issue, 1 check) ──────────────────────────

    static String cusip(ThreadLocalRandom rng) {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(ALPHA_NUM[rng.nextInt(36)]);
        String body = sb.toString();
        // CUSIP check digit: even 1-indexed (odd 0-indexed) positions ×2
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            char c = body.charAt(i);
            int v = Character.isDigit(c) ? c - '0' : c - 'A' + 10;
            if (i % 2 == 1) v *= 2;
            sum += v / 10 + v % 10;
        }
        int check = (10 - (sum % 10)) % 10;
        return body + check;
    }

    // ── SEDOL — 7 chars: 6 alphanumeric + 1 check digit ─────────────────────

    static String sedol(ThreadLocalRandom rng) {
        // SEDOL: positions 0-5 are 0-9 or B-Z (no A, E, I, O, U)
        char[] valid = "0123456789BCDFGHJKLMNPQRSTVWXYZ".toCharArray();
        int[] weights = {1, 3, 1, 7, 3, 9};
        int[] d = new int[6];
        for (int i = 0; i < 6; i++) {
            char c = valid[rng.nextInt(valid.length)];
            d[i] = Character.isDigit(c) ? c - '0' : c - 'A' + 10;
        }
        int sum = 0;
        for (int i = 0; i < 6; i++) sum += d[i] * weights[i];
        int check = (10 - (sum % 10)) % 10;
        StringBuilder sb = new StringBuilder();
        for (int v : d) {
            if (v < 10) sb.append(v);
            else sb.append((char)('A' + v - 10));
        }
        sb.append(check);
        return sb.toString();
    }

    // ── LEI — ISO 17442: 20 chars, MOD-97 check ──────────────────────────────

    static String lei(ThreadLocalRandom rng) {
        // Format: 4 alpha (LOU) + 2 zeros + 13 alphanum + 2 check
        String lou   = randomAlphaNum(rng, 4).toUpperCase();
        String body  = "00" + randomAlphaNum(rng, 13).toUpperCase();
        String check = FinancialGen.ibanCheckDigits(lou, body);
        return lou + body + check;
    }

    // ── FIX Message (abbreviated) ─────────────────────────────────────────────

    private static String fixMessage(ThreadLocalRandom rng) {
        String[] sides    = {"1","2"};
        String[] ordTypes = {"1","2","3","4"};
        String ordId  = "ORD-" + rng.nextInt(100000, 999999);
        String clOrdId = "CL-" + rng.nextInt(100000, 999999);
        double price  = 10.0 + rng.nextDouble(0, 490);
        int    qty    = rng.nextInt(100, 10000);
        String symbol = "MOCK" + randomAlpha(rng, 3);
        return String.format("8=FIX.4.29=15535=D49=MOCKJUTSU56=EXCHANGE11=%s21=138=%d40=%s44=%.2f54=%s55=%s60=20240101-12:00:0010=000",
            clOrdId, qty, ordTypes[rng.nextInt(ordTypes.length)], price,
            sides[rng.nextInt(sides.length)], symbol);
    }

    // ── PSD2 Consent ─────────────────────────────────────────────────────────

    private static String psd2Consent(ThreadLocalRandom rng) {
        String consentId = "MOCKJ-CONSENT-" + java.util.UUID.randomUUID().toString().substring(0,13).toUpperCase();
        return "{\"consentId\":\"" + consentId + "\",\"access\":{\"accounts\":\"readAccount\"," +
               "\"balances\":\"readBalances\",\"transactions\":\"readTransactions\"}," +
               "\"recurringIndicator\":true,\"validUntil\":\"2025-12-31\",\"frequencyPerDay\":4," +
               "\"lastActionDate\":\"2024-01-01\",\"consentStatus\":\"received\"}";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String randomAlphaNum(ThreadLocalRandom rng, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(ALPHA_NUM[rng.nextInt(ALPHA_NUM.length)]);
        return sb.toString();
    }

    private static String randomAlpha(ThreadLocalRandom rng, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append((char)('A' + rng.nextInt(26)));
        return sb.toString();
    }
}
