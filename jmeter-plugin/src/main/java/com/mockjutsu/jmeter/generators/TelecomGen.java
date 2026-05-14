package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** Telecom — IMEI (Luhn), ICCID, IMSI, MSISDN. Mirrors telecom.py. */
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
            case "imei"  -> imei(rng, locale);
            case "imei2" -> imei(rng, locale);  // second SIM slot
            case "iccid" -> iccid(rng, locale);
            case "imsi"  -> imsi(rng, locale);
            case "msisdn"-> msisdn(rng, locale);
            default      -> "ERROR: Unknown telecom type '" + type + "'";
        };
    }

    // ── IMEI — 15 digits, Luhn valid ─────────────────────────────────────────

    static String imei(ThreadLocalRandom rng, String locale) {
        String[] tacs = switch (locale) { case "US" -> TAC_US; case "DE" -> TAC_DE; default -> TAC_TR; };
        String tac    = tacs[rng.nextInt(tacs.length)];
        StringBuilder sb = new StringBuilder(tac);
        while (sb.length() < 14) sb.append(rng.nextInt(0, 10));
        sb.append(IdentityGen.luhnCheckDigit(sb.toString()));
        return sb.toString();
    }

    // ── ICCID — 19-20 digits, Luhn valid ─────────────────────────────────────

    static String iccid(ThreadLocalRandom rng, String locale) {
        // Format: MII(89) + CC + MNC + account(12) + Luhn
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

    // ── MSISDN — international phone number format ────────────────────────────

    private static String msisdn(ThreadLocalRandom rng, String locale) {
        String cc = switch (locale) { case "US" -> "+1"; case "DE" -> "+49"; case "FR" -> "+33"; case "UK" -> "+44"; case "RU" -> "+7"; default -> "+90"; };
        return cc + String.format("%010d", rng.nextLong(1000000000L, 9999999999L));
    }
}
