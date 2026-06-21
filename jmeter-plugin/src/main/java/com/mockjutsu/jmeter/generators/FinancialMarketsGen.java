package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** Capital markets — ISIN, CUSIP, SEDOL, LEI, FIX, PSD2. Mirrors financial_markets.py. */
public final class FinancialMarketsGen {

    private FinancialMarketsGen() {}

    private static final String[] COUNTRY_CODES   = {"US","GB","DE","FR","TR","JP","CA","AU","CH","NL"};
    private static final char[]   ALPHA_NUM        = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final String[] EXCHANGE_NAMES   = {"NYSE","NASDAQ","LSE","Euronext","XETRA","Borsa Istanbul","TSE","ASX","SIX","Euronext Amsterdam"};
    private static final String[] MIC_CODES        = {"XNYS","XNAS","XLON","XPAR","XETR","XIST","XTKS","XASX","XSWX","XAMS","XHKG","XMIL","XBUD","XBRA","XKRX"};
    private static final String[] FOREX_PAIRS      = {"EURUSD","GBPUSD","USDJPY","USDCHF","AUDUSD","USDCAD","NZDUSD","EURGBP","EURJPY","GBPJPY","USDTRY","EUРТRY"};
    private static final String[] OPTION_TYPES     = {"CALL","PUT"};
    private static final String[] BOND_TYPES       = {"Government","Corporate","Municipal","Agency","Treasury","Sovereign"};
    private static final String[] TICKER_PREFIXES  = {"MCK","NOV","APX","ZRC","ORB","VTX","AXM","TRX","PLN","FXM","LBL","ZNT"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "isin"              -> isin(rng, locale);
            case "cusip"             -> cusip(rng);
            case "sedol"             -> sedol(rng);
            case "lei"               -> lei(rng);
            case "fix_message"       -> fixMessage(rng);
            case "psd2_consent"      -> psd2Consent(rng);
            case "figi"              -> figi(rng);
            case "nsin"              -> nsin(rng);
            case "stock_ticker"      -> stockTicker(rng);
            case "forex_pair"        -> pick(rng, FOREX_PAIRS);
            case "forex_rate"        -> forexRate(rng);
            case "ric"               -> ric(rng, locale);
            case "mic"               -> pick(rng, MIC_CODES);
            case "stock_exchange"    -> pick(rng, EXCHANGE_NAMES);
            case "option_contract"   -> optionContract(rng);
            case "bond_yield"        -> String.format(java.util.Locale.US, "%.4f", rng.nextDouble(0.5, 8.0));
            case "coupon_rate"       -> String.format(java.util.Locale.US, "%.2f", rng.nextDouble(1.0, 12.0));
            case "settlement_date"   -> settlementDate();
            case "portfolio_id"      -> portfolioId(rng);
            case "portfolio_id_masked" -> maskPortfolio(portfolioId(rng));
            default                  -> "ERROR: Unknown markets type '" + type + "'";
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
        // JWS compact serialization (mirrors financial_markets.py)
        String kid = randomAlphaNum(rng, 16).toUpperCase();
        String consentId = "MOCKJ-" + kid.substring(0, 8);
        long now = System.currentTimeMillis() / 1000;
        String iban1 = "TR" + String.format("%02d", rng.nextInt(10, 99)) + String.format("%016d", rng.nextLong(1000000000000000L, 9999999999999999L));
        String iban2 = "TR" + String.format("%02d", rng.nextInt(10, 99)) + String.format("%016d", rng.nextLong(1000000000000000L, 9999999999999999L));
        String expDate = java.time.LocalDate.now().plusDays(90).toString() + "T00:00:00Z";
        String fromDate = java.time.LocalDate.now().minusDays(90).toString() + "T00:00:00Z";
        String toDate   = java.time.LocalDate.now().plusDays(90).toString() + "T00:00:00Z";
        String header = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
            ("{\"alg\":\"PS256\",\"kid\":\"" + kid + "\",\"b64\":false,\"crit\":[\"b64\"]}").getBytes());
        String payloadJson =
            "{\"iss\":\"MOCKJUTSU\",\"iat\":" + now + ",\"exp\":" + (now + 3600) +
            ",\"consentId\":\"" + consentId + "\"" +
            ",\"permissions\":[\"ReadAccountsDetail\",\"ReadBalances\",\"ReadTransactionsCredits\"" +
            ",\"ReadTransactionsDebits\",\"ReadTransactionsDetail\"]" +
            ",\"accounts\":[{\"schemeName\":\"IBAN\",\"identification\":\"" + iban1 + "\"}]" +
            ",\"balances\":[{\"schemeName\":\"IBAN\",\"identification\":\"" + iban1 + "\"}]" +
            ",\"transactions\":[{\"schemeName\":\"IBAN\",\"identification\":\"" + iban2 + "\"}]" +
            ",\"expirationDateTime\":\"" + expDate + "\"" +
            ",\"transactionFromDateTime\":\"" + fromDate + "\"" +
            ",\"transactionToDateTime\":\"" + toDate + "\"}";
        String payload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
        byte[] sigBytes = new byte[64];
        new java.security.SecureRandom().nextBytes(sigBytes);
        String sig = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes);
        return header + "." + payload + "." + sig;
    }

    // ── FIGI (12 chars: 2 alpha + 9 alphanumeric + 1 CUSIP-style check) ────────

    static String figi(ThreadLocalRandom rng) {
        // Avoid reserved prefixes BS, BM, GG, GB, GH, KY, VG
        String[] invalidPfx = {"BS","BM","GG","GB","GH","KY","VG"};
        String prefix;
        do {
            prefix = "" + (char)('A' + rng.nextInt(26)) + (char)('A' + rng.nextInt(26));
        } while (java.util.Arrays.asList(invalidPfx).contains(prefix));
        prefix += "G"; // FIGI registrar code
        StringBuilder body = new StringBuilder(prefix);
        for (int i = 0; i < 8; i++) body.append(ALPHA_NUM[rng.nextInt(ALPHA_NUM.length)]);
        String s = body.toString();
        // CUSIP-style check digit
        int sum = 0;
        for (int i = 0; i < 11; i++) {
            char c = s.charAt(i);
            int v = Character.isDigit(c) ? c - '0' : c - 'A' + 10;
            if (i % 2 == 1) v *= 2;
            sum += v / 10 + v % 10;
        }
        int check = (10 - (sum % 10)) % 10;
        return s + check;
    }

    // ── NSIN (9-char National Securities ID) ─────────────────────────────────

    static String nsin(ThreadLocalRandom rng) {
        return randomAlphaNum(rng, 9).toUpperCase();
    }

    // ── Stock Ticker ──────────────────────────────────────────────────────────

    private static String stockTicker(ThreadLocalRandom rng) {
        String base = TICKER_PREFIXES[rng.nextInt(TICKER_PREFIXES.length)];
        return rng.nextBoolean() ? base : base + (char)('A' + rng.nextInt(26));
    }

    // ── Forex Rate ────────────────────────────────────────────────────────────

    private static String forexRate(ThreadLocalRandom rng) {
        double rate = 0.5 + rng.nextDouble(0, 9.5);
        return String.format(java.util.Locale.US, "%.4f", rate);
    }

    // ── RIC (Reuters Instrument Code) ────────────────────────────────────────

    private static String ric(ThreadLocalRandom rng, String locale) {
        String sfx = switch (locale) { case "UK" -> ".L"; case "DE" -> ".DE"; case "FR" -> ".PA"; default -> ""; };
        return stockTicker(rng) + sfx;
    }

    // ── Option Contract ───────────────────────────────────────────────────────

    private static String optionContract(ThreadLocalRandom rng) {
        java.time.LocalDate exp = java.time.LocalDate.now().plusMonths(1 + rng.nextInt(12));
        double strike = 50.0 + rng.nextDouble(0, 950.0);
        String type = OPTION_TYPES[rng.nextInt(2)];
        return String.format(java.util.Locale.US, "%s %s %.1f %s", stockTicker(rng), exp, strike, type);
    }

    // ── Settlement Date ───────────────────────────────────────────────────────

    private static String settlementDate() {
        return java.time.LocalDate.now().plusDays(2).toString();
    }

    // ── Portfolio ID ──────────────────────────────────────────────────────────

    private static String portfolioId(ThreadLocalRandom rng) {
        return "PORT-" + randomAlphaNum(rng, 8).toUpperCase();
    }

    private static String maskPortfolio(String id) {
        return id.substring(0, 7) + "****";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }

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
