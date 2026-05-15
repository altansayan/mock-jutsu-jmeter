package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** Telecom — IMEI (Luhn), ICCID, IMSI, MSISDN, IMEI2. Mirrors telecom.py. */
public final class TelecomGen {

    private TelecomGen() {}

    // TAC prefixes by locale (first 8 digits of IMEI)
    private static final String[] TAC_TR = {"86840404","86393104","35601809","35847909"};
    private static final String[] TAC_US = {"35256209","35913908","35632008","35430209"};
    private static final String[] TAC_DE = {"35430109","86840304","35601709","35913808"};

    // MCC-MNC by locale
    private static final String[] MCCMNC_TR = {"28601","28603","28606"};
    private static final String[] MCCMNC_US = {"310260","310410","311480"};
    private static final String[] MCCMNC_DE = {"26201","26202","26203"};
    private static final String[] MCCMNC_FR = {"20801","20810","20815"};
    private static final String[] MCCMNC_UK = {"23410","23420","23430"};
    private static final String[] MCCMNC_RU = {"25001","25002","25020"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "imei"   -> imei(rng, locale);
            case "imei2"  -> imei2(rng, locale);
            case "iccid"  -> iccid(rng, locale);
            case "imsi"   -> imsi(rng, locale);
            case "msisdn" -> msisdn(rng, locale);
            default       -> "ERROR: Unknown telecom type '" + type + "'";
        };
    }

    // ── IMEI — 15 digits, Luhn valid ─────────────────────────────────────────

    static String imei(ThreadLocalRandom rng, String locale) {
        String[] tacs = switch (locale) { case "US" -> TAC_US; case "DE" -> TAC_DE; default -> TAC_TR; };
        String tac = tacs[rng.nextInt(tacs.length)];
        StringBuilder sb = new StringBuilder(tac);
        while (sb.length() < 14) sb.append(rng.nextInt(0, 10));
        sb.append(IdentityGen.luhnCheckDigit(sb.toString()));
        return sb.toString();
    }

    // ── IMEI2 — hyphenated format AA-BBBBBB-CCCCCC-D (ISO 3GPP) ─────────────

    private static String imei2(ThreadLocalRandom rng, String locale) {
        String full = imei(rng, locale);
        // AA-BBBBBB-CCCCCC-D
        return full.substring(0,2) + "-" + full.substring(2,8) + "-" + full.substring(8,14) + "-" + full.charAt(14);
    }

    // ── ICCID — 19-20 digits, Luhn valid ─────────────────────────────────────

    static String iccid(ThreadLocalRandom rng, String locale) {
        String cc = switch (locale) { case "US" -> "1"; case "DE" -> "49"; case "FR" -> "33"; case "UK" -> "44"; case "RU" -> "7"; default -> "90"; };
        StringBuilder sb = new StringBuilder("89").append(cc);
        while (sb.length() < 18) sb.append(rng.nextInt(0, 10));
        sb.append(IdentityGen.luhnCheckDigit(sb.toString()));
        return sb.toString();
    }

    // ── IMSI — 15 digits: MCC(3) + MNC(2) + MSIN(10) ────────────────────────

    static String imsi(ThreadLocalRandom rng, String locale) {
        String[] mccmncs = switch (locale) {
            case "TR" -> MCCMNC_TR; case "US" -> MCCMNC_US; case "DE" -> MCCMNC_DE;
            case "FR" -> MCCMNC_FR; case "UK" -> MCCMNC_UK; case "RU" -> MCCMNC_RU;
            default   -> MCCMNC_TR;
        };
        String mccmnc = mccmncs[rng.nextInt(mccmncs.length)];
        StringBuilder sb = new StringBuilder(mccmnc);
        while (sb.length() < 15) sb.append(rng.nextInt(0, 10));
        return sb.toString();
    }

    // ── MSISDN — locale-specific prefix + digit count ─────────────────────────
    // TR: +905 + 9 digits, US: +1 + 10 digits, UK: +447 + 9 digits,
    // DE: +491 + 9 digits, FR: +336 + 8 digits, RU: +79 + 9 digits

    private static String msisdn(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> "+905"  + digits(rng, 9);
            case "US" -> "+1"    + digits(rng, 10);
            case "UK" -> "+447"  + digits(rng, 9);
            case "DE" -> "+491"  + digits(rng, 9);
            case "FR" -> "+336"  + digits(rng, 8);
            case "RU" -> "+79"   + digits(rng, 9);
            default   -> "+905"  + digits(rng, 9);
        };
    }

    private static String digits(ThreadLocalRandom rng, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(rng.nextInt(10));
        return sb.toString();
    }
}
