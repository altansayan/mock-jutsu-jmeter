package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/** Financial generator — mirrors financial.py algorithms (Luhn, MOD-97, IBAN). */
public final class FinancialGen {

    private FinancialGen() {}

    private static final SecureRandom SEC = new SecureRandom();

    // ── Card networks & BINs ──────────────────────────────────────────────────

    private static final String[][] VISA_BINS    = {{"4111","4539","4916","4532"}, {"16"}};
    private static final String[][] MC_BINS      = {{"5100","5200","5300","5400","5500"}, {"16"}};
    private static final String[][] AMEX_BINS    = {{"3400","3700","3701","3741","3742"}, {"15"}};
    private static final String[][] DISCOVER_BINS = {{"6011","6500","6501"}, {"16"}};
    private static final String[][] TROY_BINS    = {{"9792"}, {"16"}};

    private static final String[] CARD_TYPES   = {"credit","debit","prepaid"};
    private static final String[] CARD_STATUS  = {"active","inactive","blocked","expired"};
    private static final String[] CARD_CATS    = {"classic","gold","platinum","business","infinite"};
    private static final String[] NETWORKS     = {"Visa","Mastercard","AmericanExpress","Discover","Troy"};

    // ── IBAN country specs: {countryCode, checkLen, bbanLen} ─────────────────
    //    bbanLen excludes the 2-char country code and 2-char check digits

    private static final String[] TR_BANKS = {"00001","00004","00006","00010","00012","00046"};
    private static final String[] DE_BANKS = {"10010010","20041133","37040044","50010517","70020270"};
    private static final String[] FR_BANKS = {"30004","30006","14508","18306","17569"};
    private static final String[] UK_BANKS = {"NWBK","LOYD","BARC","HSBC","MIDL"};
    private static final String[] RU_BANKS = {"044525225","044525600","044030653"};

    // ── Issuers (fictional) ───────────────────────────────────────────────────

    private static final String[] ISSUERS_TR = {"Novex Bank TR","Apex Finans","Zircon Kredi","Orbit Bank"};
    private static final String[] ISSUERS_US = {"Novex Bank US","Atlas Credit","Summit Financial","Crest Bank"};
    private static final String[] ISSUERS_DE = {"Novex Bank DE","Rhine Capital","Baltic Finance","Elbe Kredit"};
    private static final String[] ISSUERS_FR = {"Novex Banque","Loire Capital","Seine Finance","Alsace Crédit"};
    private static final String[] ISSUERS_UK = {"Novex Bank UK","Thames Capital","Severn Finance","Tyne Credit"};
    private static final String[] ISSUERS_RU = {"Novex Bank RU","Volga Capital","Neva Finance","Ural Kredit"};

    // ── Public API ────────────────────────────────────────────────────────────

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "cardnum"      -> cardnum(rng, locale);
            case "cardnetwork"  -> cardnetwork(rng, locale);
            case "cardtype"     -> pick(rng, CARD_TYPES);
            case "cardstatus"   -> pick(rng, CARD_STATUS);
            case "cardcategory" -> pick(rng, CARD_CATS);
            case "cvv3"         -> String.format("%03d", rng.nextInt(0, 1000));
            case "cvv4"         -> String.format("%04d", rng.nextInt(0, 10000));
            case "issuer"       -> issuer(rng, locale);
            case "expiry"       -> expiry(rng);
            case "expirymonth"  -> String.format("%02d", rng.nextInt(1, 13));
            case "expiryyear"   -> String.valueOf(2025 + rng.nextInt(1, 8));
            case "pin"          -> String.format("%04d", rng.nextInt(0, 10000));
            case "balance"      -> balance(rng, locale);
            case "iban"         -> iban(rng, locale);
            case "credit_score" -> creditScore(rng, locale);
            case "sepa_qr"      -> sepaQr(rng, locale);
            case "emv_qr_p2p"   -> emvQr(rng, locale, "P2P");
            case "emv_qr_atm"   -> emvQr(rng, locale, "ATM");
            case "emv_qr_pos"   -> emvQr(rng, locale, "POS");
            case "3ds_cavv"     -> cavv();
            case "3ds_eci"      -> eci(rng);
            default             -> "ERROR: Unknown financial type '" + type + "'";
        };
    }

    // ── Card number (Luhn-valid) ──────────────────────────────────────────────

    static String cardnum(ThreadLocalRandom rng, String locale) {
        String[][] bins = cardBins(rng, locale);
        String bin    = bins[0][rng.nextInt(bins[0].length)];
        int    length = Integer.parseInt(bins[1][0]);
        // fill digits after BIN, leave last for Luhn check
        StringBuilder sb = new StringBuilder(bin);
        while (sb.length() < length - 1) sb.append(rng.nextInt(0, 10));
        sb.append(IdentityGen.luhnCheckDigit(sb.toString()));
        return sb.toString();
    }

    private static String[][] cardBins(ThreadLocalRandom rng, String locale) {
        // Troy for TR, otherwise random Visa/MC/Amex/Discover
        if ("TR".equals(locale) && rng.nextInt(3) == 0) return TROY_BINS;
        String[][][] all = {VISA_BINS, MC_BINS, AMEX_BINS, DISCOVER_BINS};
        return all[rng.nextInt(all.length)];
    }

    private static String cardnetwork(ThreadLocalRandom rng, String locale) {
        if ("TR".equals(locale) && rng.nextInt(3) == 0) return "Troy";
        return pick(rng, NETWORKS);
    }

    // ── IBAN — ISO 13616 with MOD-97 check digits ────────────────────────────

    static String iban(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> ibanTR(rng);
            case "DE" -> ibanDE(rng);
            case "FR" -> ibanFR(rng);
            case "UK" -> ibanUK(rng);
            case "RU" -> ibanRU(rng);
            default   -> ibanTR(rng);
        };
    }

    private static String ibanTR(ThreadLocalRandom rng) {
        // TR: CC(2) + check(2) + reserved(1=0) + bank(5) + account(16) = 26 total
        String bank    = pick(rng, TR_BANKS);
        String account = randomDigits(rng, 16);
        String bban    = "0" + bank + account;
        String check   = ibanCheckDigits("TR", bban);
        return "TR" + check + bban;
    }

    private static String ibanDE(ThreadLocalRandom rng) {
        // DE: CC(2) + check(2) + bank(8) + account(10) = 22 total
        String bank    = pick(rng, DE_BANKS);
        String account = randomDigits(rng, 10);
        String bban    = bank + account;
        String check   = ibanCheckDigits("DE", bban);
        return "DE" + check + bban;
    }

    private static String ibanFR(ThreadLocalRandom rng) {
        // FR: CC(2) + check(2) + bank(5) + branch(5) + account(11) + key(2) = 27 total
        String bank    = pick(rng, FR_BANKS);
        String branch  = randomDigits(rng, 5);
        String account = randomDigits(rng, 11);
        String key     = randomDigits(rng, 2);
        String bban    = bank + branch + account + key;
        String check   = ibanCheckDigits("FR", bban);
        return "FR" + check + bban;
    }

    private static String ibanUK(ThreadLocalRandom rng) {
        // UK: CC(2) + check(2) + bank(4) + sortcode(6) + account(8) = 22 total
        String bank    = pick(rng, UK_BANKS);
        String sort    = randomDigits(rng, 6);
        String account = randomDigits(rng, 8);
        String bban    = bank + sort + account;
        String check   = ibanCheckDigits("GB", bban);
        return "GB" + check + bban;
    }

    private static String ibanRU(ThreadLocalRandom rng) {
        // Russia doesn't officially use IBAN but has informal format
        // Simulate as RU + check + bank(9) + account(15)
        String bank    = pick(rng, RU_BANKS);
        String account = randomDigits(rng, 15);
        String bban    = bank + account;
        String check   = ibanCheckDigits("RU", bban);
        return "RU" + check + bban;
    }

    static String ibanCheckDigits(String country, String bban) {
        // Rearrange: BBAN + country + "00", then convert to numeric, compute 98 - mod97
        String rearranged = bban + country + "00";
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) numeric.append(c - 'A' + 10);
            else numeric.append(c);
        }
        // Big number MOD 97 via chunked computation
        String numStr = numeric.toString();
        int mod = 0;
        for (int i = 0; i < numStr.length(); i++) {
            mod = (mod * 10 + (numStr.charAt(i) - '0')) % 97;
        }
        int check = 98 - mod;
        return String.format("%02d", check);
    }

    // ── Expiry ────────────────────────────────────────────────────────────────

    private static String expiry(ThreadLocalRandom rng) {
        int month = rng.nextInt(1, 13);
        int year  = (java.time.LocalDate.now().getYear() % 100) + rng.nextInt(1, 7);
        return String.format("%02d/%02d", month, year);
    }

    // ── Issuer ────────────────────────────────────────────────────────────────

    private static String issuer(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> pick(rng, ISSUERS_TR);
            case "US" -> pick(rng, ISSUERS_US);
            case "DE" -> pick(rng, ISSUERS_DE);
            case "FR" -> pick(rng, ISSUERS_FR);
            case "UK" -> pick(rng, ISSUERS_UK);
            case "RU" -> pick(rng, ISSUERS_RU);
            default   -> pick(rng, ISSUERS_TR);
        };
    }

    // ── Balance ───────────────────────────────────────────────────────────────

    private static String balance(ThreadLocalRandom rng, String locale) {
        double amount = rng.nextDouble(10.0, 50000.00);
        return String.format("%.2f", amount);
    }

    // ── Credit score ──────────────────────────────────────────────────────────

    private static String creditScore(ThreadLocalRandom rng, String locale) {
        return String.valueOf(rng.nextInt(300, 851));
    }

    // ── SEPA QR ───────────────────────────────────────────────────────────────

    private static String sepaQr(ThreadLocalRandom rng, String locale) {
        String iban   = iban(rng, locale.equals("TR") ? "DE" : locale);
        double amount = rng.nextDouble(1.00, 1000.00);
        return String.format("BCD\n001\n1\nSCT\nNOVEXDEFT\nNovex Corp\n%s\nEUR%.2f\n\nTest payment\n", iban, amount);
    }

    // ── EMV QR ────────────────────────────────────────────────────────────────

    private static String emvQr(ThreadLocalRandom rng, String locale, String txType) {
        String merchant = String.format("MOCKJM%09d", rng.nextInt(100000000, 999999999));
        String ccy = switch (locale) { case "DE","FR" -> "978"; case "UK" -> "826"; case "US" -> "840"; case "RU" -> "643"; default -> "949"; };
        return String.format("000201010211520400005303%s5402%.2f5802TR5910MOCK%s6304ABCD",
            ccy, rng.nextDouble(1, 500), txType);
    }

    // ── 3DS ───────────────────────────────────────────────────────────────────

    private static String cavv() {
        byte[] bytes = new byte[20];
        SEC.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, 28);
    }

    private static String eci(ThreadLocalRandom rng) {
        String[] ecis = {"05","06","07"};
        return pick(rng, ecis);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    static String randomDigits(ThreadLocalRandom rng, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(rng.nextInt(0, 10));
        return sb.toString();
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }
}
