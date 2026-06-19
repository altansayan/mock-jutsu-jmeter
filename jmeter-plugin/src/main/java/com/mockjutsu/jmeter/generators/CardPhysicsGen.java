package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

/** CardPhysics — ISO 8583 bitmap, EMV ARQC/ATC/IAD, ATM session, POS receipt. Mirrors cardphysics.py. */
public final class CardPhysicsGen {
    private CardPhysicsGen() {}

    private static final SecureRandom SEC = new SecureRandom();

    // Pre-computed ISO 8583 bitmaps (verified bit positions)
    private static final String BITMAP_AUTH_REQ  = makeBitmap(2,3,4,7,11,12,13,14,18,22,25,37,41,42,49);
    private static final String BITMAP_AUTH_RESP = makeBitmap(2,3,4,7,11,12,13,38,39,41,42);
    private static final String BITMAP_REVERSAL  = makeBitmap(2,3,4,7,11,12,13,37,41,42,49,56);

    private static String makeBitmap(int... des) {
        long bits = 0L;
        for (int de : des) bits |= (1L << (64 - de));
        return String.format("%016X", bits);
    }

    // ISO 4217 numeric n3 per ISO 8583 DE049 — 3 digits, no leading zero
    // EMV tag 5F2A uses 2-byte BCD (0x09 0x49 = TRY) — separate context, not here
    private static String currency(String locale) {
        return switch (locale) { case "DE","FR" -> "978"; case "UK" -> "826"; case "US" -> "840"; case "RU" -> "643"; default -> "949"; };
    }

    private static final String[] MCC_POOL   = {"5411","5999","4111","5912","5691","7011","5812","6011"};
    private static final String[] ENTRY_MODES = {"051","071","002"};

    private static String currencyAlpha(String locale) {
        return switch (locale) { case "DE","FR" -> "EUR"; case "UK" -> "GBP"; case "US" -> "USD"; case "RU" -> "RUB"; default -> "TRY"; };
    }

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "emv_arqc"               -> emvArqc();
            case "emv_atc"                -> emvAtc(rng);
            case "emv_iad"                -> emvIad(rng);
            case "iso8583_auth_request"   -> iso8583AuthReq(rng, locale);
            case "iso8583_auth_response"  -> iso8583AuthResp(rng, locale);
            case "iso8583_reversal"       -> iso8583Reversal(rng, locale);
            case "atm_session"            -> atmSession(rng, locale);
            case "pos_receipt"            -> posReceipt(rng, locale);
            default -> "ERROR: Unknown cardphysics type '" + type + "'";
        };
    }

    // ── EMV ARQC (tag 9F26) — 8 bytes = 16 hex uppercase ────────────────────

    static String emvArqc() {
        byte[] b = new byte[8];
        SEC.nextBytes(b);
        StringBuilder sb = new StringBuilder(16);
        for (byte v : b) sb.append(String.format("%02X", v));
        return sb.toString();
    }

    // ── EMV ATC (tag 9F36) — 2 bytes = 4 hex ────────────────────────────────

    static String emvAtc(ThreadLocalRandom rng) {
        return String.format("%04X", rng.nextInt(1, 10000));
    }

    // ── EMV IAD (tag 9F10) — starts with 0A, 11 bytes = 22 hex ──────────────

    static String emvIad(ThreadLocalRandom rng) {
        int dki = rng.nextInt(0, 16);
        int cvn = rng.nextInt(0, 256);
        int cvr = rng.nextInt(0, 65536);
        int add = rng.nextInt();
        int pad = rng.nextInt(0, 65536);
        return String.format("0A%02X%02X%04X%08X%04X", dki, cvn, cvr, add & 0xFFFFFFFFL, pad);
    }

    // ── ISO 8583 Auth Request (MTI 0100) — plain string mirroring cardphysics.py ─

    static String iso8583AuthReq(ThreadLocalRandom rng, String locale) {
        String pan    = FinancialGen.cardnum(rng, locale);
        String amount = String.format("%012d", rng.nextInt(100, 9999999));
        String trace  = String.format("%06d", rng.nextInt(1, 1000000));
        String dt     = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMddHHmmss"));
        String expiry = String.format("%02d%02d", (java.time.LocalDate.now().getYear()%100)+rng.nextInt(1,6), rng.nextInt(1,13));
        String rrn    = "MOCKJ" + hexRng(rng, 7);
        String tid    = "MOCKJT" + String.format("%02d", rng.nextInt(10, 100));
        String mid    = "MOCKJM" + String.format("%09d", rng.nextInt(100000000, 999999999));
        String ccy    = currency(locale);
        return "MTI:0100\\nBITMAP:" + BITMAP_AUTH_REQ +
               "\\nDE002:" + pan + "\\nDE003:000000\\nDE004:" + amount +
               "\\nDE007:" + dt + "\\nDE011:" + trace + "\\nDE012:" + dt.substring(4) +
               "\\nDE013:" + dt.substring(0,4) + "\\nDE014:" + expiry +
               "\\nDE018:" + MCC_POOL[rng.nextInt(MCC_POOL.length)] +
               "\\nDE022:" + ENTRY_MODES[rng.nextInt(ENTRY_MODES.length)] + "\\nDE025:00" +
               "\\nDE037:" + rrn + "\\nDE041:" + tid + "\\nDE042:" + mid + "\\nDE049:" + ccy;
    }

    // ── ISO 8583 Auth Response (MTI 0110) ────────────────────────────────────

    static String iso8583AuthResp(ThreadLocalRandom rng, String locale) {
        String pan      = FinancialGen.cardnum(rng, locale);
        String amount   = String.format("%012d", rng.nextInt(100, 9999999));
        String trace    = String.format("%06d", rng.nextInt(1, 1000000));
        String dt       = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMddHHmmss"));
        String authCode = "MOCKJ" + rng.nextInt(1, 10);
        String respCode = new String[]{"00","05","12","14","51","54","57","91"}[rng.nextInt(8)];
        String tid      = "MOCKJT" + String.format("%02d", rng.nextInt(10, 100));
        String mid      = "MOCKJM" + String.format("%09d", rng.nextInt(100000000, 999999999));
        return "MTI:0110\\nBITMAP:" + BITMAP_AUTH_RESP +
               "\\nDE002:" + pan + "\\nDE003:000000\\nDE004:" + amount +
               "\\nDE007:" + dt + "\\nDE011:" + trace + "\\nDE012:" + dt.substring(4) +
               "\\nDE013:" + dt.substring(0,4) + "\\nDE038:" + authCode +
               "\\nDE039:" + respCode + "\\nDE041:" + tid + "\\nDE042:" + mid;
    }

    // ── ISO 8583 Reversal (MTI 0400) ─────────────────────────────────────────

    static String iso8583Reversal(ThreadLocalRandom rng, String locale) {
        String pan    = FinancialGen.cardnum(rng, locale);
        String amount = String.format("%012d", rng.nextInt(100, 9999999));
        String rrn    = "MOCKJ" + hexRng(rng, 7);
        String tid    = "MOCKJT" + String.format("%02d", rng.nextInt(10, 100));
        String mid    = "MOCKJM" + String.format("%09d", rng.nextInt(100000000, 999999999));
        String ccy    = currency(locale);
        String trace  = String.format("%06d", rng.nextInt(1, 1000000));
        String dt     = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMddHHmmss"));
        String acqId  = String.format("%011d", rng.nextLong(10000000000L, 99999999999L));
        String de056  = "0100" + trace + dt + acqId;
        return "MTI:0400\\nBITMAP:" + BITMAP_REVERSAL +
               "\\nDE002:" + pan + "\\nDE003:000000\\nDE004:" + amount +
               "\\nDE037:" + rrn + "\\nDE041:" + tid + "\\nDE042:" + mid +
               "\\nDE049:" + ccy + "\\nDE056:" + de056;
    }

    // ── ATM Session JSON — mirrors cardphysics.py (16 fields) ────────────────

    static String atmSession(ThreadLocalRandom rng, String locale) {
        String sessionId = "MOCKJ-ATM-" + String.format("%08X", rng.nextInt()).toUpperCase();
        int    termNo    = rng.nextInt(10, 100);
        double amount    = rng.nextInt(1,20) * 50.0;
        String ccy       = currencyAlpha(locale);
        String maskedPan = String.format("**** **** **** %04d", rng.nextInt(1000,9999));
        String[] schemes  = {"VISA","MASTERCARD","TROY","AMEX"};
        String[] txTypes  = {"CASH_WITHDRAWAL","BALANCE_INQUIRY","MINI_STATEMENT","DEPOSIT"};
        String[] respMsgs = {"APPROVED","DECLINED","REFER TO BANK","INVALID PIN"};
        String[] respCodes= {"00","05","51","55"};
        int rc = rng.nextInt(4);
        String expiry  = String.format("%02d/%02d", rng.nextInt(1,13), (java.time.LocalDate.now().getYear()%100)+rng.nextInt(1,6));
        String arqc    = CardPhysicsGen.emvArqc();
        String atc     = CardPhysicsGen.emvAtc(rng);
        String stan    = String.format("%06d", rng.nextInt(1, 1000000));
        String ts      = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        return "{\"session_id\":\"" + sessionId + "\",\"terminal_id\":\"MOCKJT" + String.format("%02d",termNo) + "\"," +
               "\"terminal_location\":\"MOCKJ Bank Branch " + termNo + "\"," +
               "\"amount\":\"" + String.format(java.util.Locale.US,"%.2f",amount) + "\"," +
               "\"currency\":\"" + ccy + "\",\"masked_pan\":\"" + maskedPan + "\"," +
               "\"card_scheme\":\"" + schemes[rng.nextInt(schemes.length)] + "\"," +
               "\"expiry\":\"" + expiry + "\",\"stan\":\"" + stan + "\"," +
               "\"auth_code\":\"MOCKJ" + rng.nextInt(1,10) + "\"," +
               "\"response_code\":\"" + respCodes[rc] + "\",\"response_message\":\"" + respMsgs[rc] + "\"," +
               "\"arqc\":\"" + arqc + "\",\"atc\":\"" + atc + "\"," +
               "\"transaction_type\":\"" + txTypes[rng.nextInt(txTypes.length)] + "\"," +
               "\"timestamp\":\"" + ts + "\"}";
    }

    // ── POS Receipt (40-char width text) ─────────────────────────────────────

    static String posReceipt(ThreadLocalRandom rng, String locale) {
        double amount = rng.nextInt(1,500) + rng.nextDouble();
        String ccy    = currencyAlpha(locale);
        String pan    = "**** **** **** " + String.format("%04d", rng.nextInt(1000,9999));
        String ts     = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String auth   = "MOCKJ" + rng.nextInt(1,10);
        return "========================================\n" +
               "         MOCK JUTSU TEST POS            \n" +
               "----------------------------------------\n" +
               String.format("DATE: %-33s\n", ts) +
               String.format("CARD: %-33s\n", pan) +
               String.format("AUTH: %-33s\n", auth) +
               "----------------------------------------\n" +
               String.format("AMOUNT: %30s\n", String.format("%.2f %s", amount, ccy)) +
               "========================================\n" +
               "      *** TEST DATA — NOT REAL ***      \n";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String hexRng(ThreadLocalRandom rng, int chars) {
        StringBuilder sb = new StringBuilder(chars);
        String hex = "0123456789ABCDEF";
        for (int i = 0; i < chars; i++) sb.append(hex.charAt(rng.nextInt(16)));
        return sb.toString();
    }
}
