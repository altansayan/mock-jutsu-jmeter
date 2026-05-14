package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** Barcode — EAN-13, EAN-8, UPC-A, ISBN-13, ISBN-10, GS1-128. Mirrors barcode.py. */
public final class BarcodeGen {

    private BarcodeGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "ean13"   -> ean13(rng);
            case "ean8"    -> ean8(rng);
            case "upca"    -> upcA(rng);
            case "isbn13"  -> isbn13(rng);
            case "isbn10"  -> isbn10(rng);
            case "gs1_128" -> gs1128(rng);
            default        -> "ERROR: Unknown barcode type '" + type + "'";
        };
    }

    // EAN-13 checksum: alternating weights 1,3
    static String ean13(ThreadLocalRandom rng) {
        int[] d = new int[13];
        for (int i = 0; i < 12; i++) d[i] = rng.nextInt(0, 10);
        d[12] = ean13Check(d, 12);
        return digitsToString(d);
    }

    // EAN-8
    static String ean8(ThreadLocalRandom rng) {
        int[] d = new int[8];
        for (int i = 0; i < 7; i++) d[i] = rng.nextInt(0, 10);
        d[7] = ean13Check(d, 7);
        return digitsToString(d);
    }

    // UPC-A — same as EAN-13 check, starts with country 0
    static String upcA(ThreadLocalRandom rng) {
        int[] d = new int[12];
        d[0] = 0;
        for (int i = 1; i < 11; i++) d[i] = rng.nextInt(0, 10);
        d[11] = ean13Check(d, 11);
        return digitsToString(d);
    }

    // ISBN-13 = EAN-13 with prefix 978 or 979
    private static String isbn13(ThreadLocalRandom rng) {
        int[] d = new int[13];
        int[] prefix = rng.nextBoolean() ? new int[]{9,7,8} : new int[]{9,7,9};
        System.arraycopy(prefix, 0, d, 0, 3);
        for (int i = 3; i < 12; i++) d[i] = rng.nextInt(0, 10);
        d[12] = ean13Check(d, 12);
        return digitsToString(d);
    }

    // ISBN-10: 9 digits + check (mod 11, X=10)
    private static String isbn10(ThreadLocalRandom rng) {
        int[] d = new int[9];
        for (int i = 0; i < 9; i++) d[i] = rng.nextInt(0, 10);
        int sum = 0;
        for (int i = 0; i < 9; i++) sum += d[i] * (10 - i);
        int check = (11 - (sum % 11)) % 11;
        StringBuilder sb = new StringBuilder();
        for (int v : d) sb.append(v);
        sb.append(check == 10 ? "X" : String.valueOf(check));
        return sb.toString();
    }

    // GS1-128: Application Identifier (01) + GTIN-14 + (10) + lot + (17) + expiry
    private static String gs1128(ThreadLocalRandom rng) {
        int[] gtin = new int[14];
        for (int i = 0; i < 13; i++) gtin[i] = rng.nextInt(0, 10);
        gtin[13] = ean13Check(gtin, 13);
        String lot    = "LOT" + rng.nextInt(100, 9999);
        String expiry = String.format("%02d%02d%02d", rng.nextInt(24,30), rng.nextInt(1,13), rng.nextInt(1,29));
        return "(01)" + digitsToString(gtin) + "(10)" + lot + "(17)" + expiry;
    }

    // ── EAN check digit algorithm ─────────────────────────────────────────────

    private static int ean13Check(int[] d, int len) {
        int odd = 0, even = 0;
        for (int i = 0; i < len; i++) {
            if (i % 2 == 0) odd  += d[i];
            else              even += d[i];
        }
        return (10 - ((odd + 3 * even) % 10)) % 10;
    }

    private static String digitsToString(int[] d) {
        StringBuilder sb = new StringBuilder(d.length);
        for (int v : d) sb.append(v);
        return sb.toString();
    }
}
