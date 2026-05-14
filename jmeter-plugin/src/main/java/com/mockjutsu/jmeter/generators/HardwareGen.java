package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** Hardware — Track1/2, chip_data (locale-aware), pin_block ISO 9564. Mirrors hardware.py. */
public final class HardwareGen {
    private HardwareGen() {}

    // ISO 4217 BCD currency codes per locale (tag 5F2A in chip TLV)
    private static final java.util.Map<String,String> CURRENCY_TLV = java.util.Map.of(
        "TR", "0949", "DE", "0978", "FR", "0978", "UK", "0826", "US", "0840", "RU", "0643"
    );

    private static final String[] TRACK1_NAMES = {
        "MOCKJUTSU/TESTCARD", "MOCKJUTSU/ALPHA", "MOCKJUTSU/BETA", "MOCKJUTSU/OMEGA"
    };

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "track1_data"   -> track1(rng, locale);
            case "track2_data"   -> track2(rng, locale);
            case "chip_data"     -> chipData(rng, locale);
            case "pin_block"     -> pinBlockFmt0(rng);
            case "pin_block_fmt3"-> pinBlockFmt3(rng);
            default -> "ERROR: Unknown hardware type '" + type + "'";
        };
    }

    // ── Track 1 — ISO/IEC 7813 ───────────────────────────────────────────────

    private static String track1(ThreadLocalRandom rng, String locale) {
        String pan    = FinancialGen.cardnum(rng, locale);
        String name   = TRACK1_NAMES[rng.nextInt(TRACK1_NAMES.length)];
        int    yy     = (java.time.LocalDate.now().getYear() % 100) + rng.nextInt(1, 6);
        int    mm     = rng.nextInt(1, 13);
        String sc     = String.format("%03d", rng.nextInt(0, 1000));
        return String.format("%%B%s^%s^%02d%02d%s000?", pan, name, yy, mm, sc);
    }

    // ── Track 2 — ISO/IEC 7813 ───────────────────────────────────────────────

    private static String track2(ThreadLocalRandom rng, String locale) {
        String pan    = FinancialGen.cardnum(rng, locale);
        int    yy     = (java.time.LocalDate.now().getYear() % 100) + rng.nextInt(1, 6);
        int    mm     = rng.nextInt(1, 13);
        String sc     = String.format("%03d", rng.nextInt(0, 1000));
        return String.format(";%s=%02d%02d%s0000000000000?", pan, yy, mm, sc);
    }

    // ── EMV Chip data TLV (locale-aware currency tag 5F2A) ───────────────────

    private static String chipData(ThreadLocalRandom rng, String locale) {
        String ccy = CURRENCY_TLV.getOrDefault(locale, "0949");
        // 5F2A = Transaction Currency Code (2 bytes BCD)
        // 9F36 = ATC (2 bytes)
        // 9F26 = ARQC (8 bytes)
        // 9F10 = IAD
        int atc    = rng.nextInt(1, 9999);
        String arqc = randomHexUpper(rng, 16);
        String iad  = "0A" + randomHexUpper(rng, 20);
        return String.format("5F2A02%s9F3602%04X9F2608%s9F10%02X%s",
            ccy, atc, arqc, iad.length()/2, iad);
    }

    // ── PIN Block Format 0 — ISO 9564-1 ──────────────────────────────────────

    static String pinBlockFmt0(ThreadLocalRandom rng) {
        int pinLen = rng.nextInt(4, 7);  // 4-6 digits
        int[] nibbles = new int[16];
        nibbles[0] = 0;         // format 0
        nibbles[1] = pinLen;    // PIN length
        for (int i = 0; i < pinLen; i++) nibbles[2 + i] = rng.nextInt(0, 10);
        for (int i = 2 + pinLen; i < 16; i++) nibbles[i] = 0xF;  // fill
        StringBuilder sb = new StringBuilder(16);
        for (int n : nibbles) sb.append(String.format("%X", n));
        return sb.toString();
    }

    // ── PIN Block Format 3 — ISO 9564-1 (random fill instead of F) ───────────

    static String pinBlockFmt3(ThreadLocalRandom rng) {
        int pinLen = rng.nextInt(4, 7);
        int[] nibbles = new int[16];
        nibbles[0] = 3;         // format 3
        nibbles[1] = pinLen;
        for (int i = 0; i < pinLen; i++) nibbles[2 + i] = rng.nextInt(0, 10);
        for (int i = 2 + pinLen; i < 16; i++) nibbles[i] = rng.nextInt(0, 10); // random fill
        StringBuilder sb = new StringBuilder(16);
        for (int n : nibbles) sb.append(String.format("%X", n));
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String randomHexUpper(ThreadLocalRandom rng, int chars) {
        StringBuilder sb = new StringBuilder(chars);
        String hex = "0123456789ABCDEF";
        for (int i = 0; i < chars; i++) sb.append(hex.charAt(rng.nextInt(16)));
        return sb.toString();
    }
}
