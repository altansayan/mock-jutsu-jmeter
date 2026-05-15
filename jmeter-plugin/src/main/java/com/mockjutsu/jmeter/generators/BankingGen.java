package com.mockjutsu.jmeter.generators;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/** Banking — SWIFT/BIC, sort code, routing, BIK. Mirrors banking.py. */
public final class BankingGen {

    private BankingGen() {}

    private static final String[] INSTITUTION_CODES = {
        "NWBK","LOYD","BARC","HSBC","CITI","DEUT","BNPA","CRES","RZBR","SBER",
        "ALFA","VTBR","GAZP","SOVK","RAIF","INGB","ABNA","BNPP","CRED","ZURN"
    };

    private static final String[] LOCATION_CODES = {
        "2L","3L","4L","5L","6L","7L","8L","9L",
        "AA","BB","CC","DD","EE","FF","GG","HH",
        "MM","NN","PP","QQ","RR","SS","TT","UU",
        "XX","YY","ZZ"
    };

    private static final String[] BANK_NAMES_TR = {
        "Novex Bank","Apex Finans","Zircon Kredi","Orbit Bank","Vertex Finans"
    };
    private static final String[] BANK_NAMES_DE = {
        "Rhine Kapital","Baltic Finance","Elbe Kredit","Spree Bank","Mosel Finanz"
    };
    private static final String[] BANK_NAMES_FR = {
        "Loire Capital","Seine Finance","Alsace Crédit","Rhône Banque","Garonne Crédit"
    };
    private static final String[] BANK_NAMES_UK = {
        "Thames Capital","Severn Finance","Tyne Credit","Mersey Bank","Avon Trust"
    };
    private static final String[] BANK_NAMES_US = {
        "Atlas Credit","Summit Financial","Crest Bank","Pinnacle Trust","Ridge Financial"
    };
    private static final String[] BANK_NAMES_RU = {
        "Volga Capital","Neva Finance","Ural Kredit","Ob Bank","Lena Trust"
    };

    private static final String[] TX_DESC_TR = {
        "Fatura ödemesi","Market alışverişi","FAST transferi","EFT havalesi",
        "Kredi kartı ödemesi","Kira ödemesi","Sigorta primi","Maaş ödemesi"
    };
    private static final String[] TX_DESC_US = {
        "Utility payment","Online purchase","Wire transfer","ACH payment",
        "Rent payment","Credit card payment","Payroll deposit","Insurance premium"
    };
    private static final String[] TX_DESC_UK = {
        "Utility bill payment","Online shopping","Faster Payment","Standing order",
        "Direct debit","Salary credit","Mortgage payment","Council tax"
    };
    private static final String[] TX_DESC_DE = {
        "Rechnung bezahlen","Online-Einkauf","SEPA-Überweisung","Dauerauftrag",
        "Lastschrift","Gehalt","Miete","Versicherungsprämie"
    };
    private static final String[] TX_DESC_FR = {
        "Paiement facture","Achat en ligne","Virement SEPA","Prélèvement automatique",
        "Loyer","Salaire","Assurance","Remboursement"
    };
    private static final String[] TX_DESC_RU = {
        "Оплата услуг ЖКХ","Покупка в интернете","Перевод СБП","Оплата кредита",
        "Аренда квартиры","Зарплата","Страховой взнос","Возврат средств"
    };

    private static final String[] TX_CHAN_TR = {"FAST","EFT","Havale","SWIFT","Kart"};
    private static final String[] TX_CHAN_US = {"ACH","Wire","Zelle","SWIFT","Check"};
    private static final String[] TX_CHAN_UK = {"Faster Payments","BACS","CHAPS","SWIFT"};
    private static final String[] TX_CHAN_DE = {"SEPA Credit Transfer","SEPA Direct Debit","SWIFT","Lastschrift"};
    private static final String[] TX_CHAN_FR = {"Virement SEPA","Prélèvement SEPA","SWIFT","TIP"};
    private static final String[] TX_CHAN_RU = {"СБП","Межбанк","SWIFT","Карточный перевод"};

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+00:00'");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Country codes by locale
    private static String countryCode(String locale) {
        return switch (locale) { case "DE" -> "DE"; case "FR" -> "FR"; case "UK" -> "GB"; case "US" -> "US"; case "RU" -> "RU"; default -> "TR"; };
    }

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "swift","bic"  -> bic(rng, locale, false);
            case "sort_code"    -> sortCode(rng);
            case "routing_number" -> routingNumber(rng);
            case "bik_code"     -> bikCode(rng);
            case "transaction"  -> transaction(rng, locale);
            case "bank_name"    -> bankName(rng, locale);
            case "sepa_ref"     -> sepaRef(rng);
            case "creditor_ref" -> creditorRef(rng);
            default             -> "ERROR: Unknown banking type '" + type + "'";
        };
    }

    // ── BIC / SWIFT (ISO 9362) ────────────────────────────────────────────────

    static String bic(ThreadLocalRandom rng, String locale, boolean strict) {
        String institution = pick(rng, INSTITUTION_CODES);
        String country     = countryCode(locale);
        String location    = pick(rng, LOCATION_CODES);
        // Strict ISO 9362: position 8 (branch, 0-indexed) must be '0' for primary office
        String branch      = strict ? "0" + randomAlpha(rng, 2) : (rng.nextBoolean() ? "" : randomAlpha(rng, 3));
        return institution + country + location + branch;
    }

    // ── UK Sort Code ──────────────────────────────────────────────────────────

    private static String sortCode(ThreadLocalRandom rng) {
        return String.format("%02d-%02d-%02d",
            rng.nextInt(10,99), rng.nextInt(10,99), rng.nextInt(10,99));
    }

    // ── US ABA Routing Number (9 digits with check digit) ────────────────────

    static String routingNumber(ThreadLocalRandom rng) {
        // ABA check digit: (3*(d0+d3+d6) + 7*(d1+d4+d7) + (d2+d5+d8)) % 10 == 0
        int[] d = new int[9];
        for (int i = 0; i < 8; i++) d[i] = rng.nextInt(0, 10);
        d[0] = Math.max(1, d[0]); // first digit 1-9 (valid ABA)
        // Compute check: (3*(d0+d3+d6) + 7*(d1+d4+d7) + (d2+d5)) % 10
        // we need d8 such that the whole is divisible by 10
        int partial = 3*(d[0]+d[3]+d[6]) + 7*(d[1]+d[4]+d[7]) + (d[2]+d[5]);
        d[8] = (10 - (partial % 10)) % 10;
        StringBuilder sb = new StringBuilder(9);
        for (int v : d) sb.append(v);
        return sb.toString();
    }

    // ── Russian BIK (9 digits) ────────────────────────────────────────────────

    private static String bikCode(ThreadLocalRandom rng) {
        return "04" + String.format("%07d", rng.nextInt(1000000, 9999999));
    }

    // ── Transaction — JSON dict matching banking.py output ───────────────────

    private static String transaction(ThreadLocalRandom rng, String locale) {
        String ccy = switch (locale) { case "DE","FR" -> "EUR"; case "UK" -> "GBP"; case "US" -> "USD"; case "RU" -> "RUB"; default -> "TRY"; };

        // Three-tier amount distribution: micro / normal / large
        int tier = rng.nextInt(10);
        double amount;
        if (tier == 0) {
            amount = Math.round((0.01 + rng.nextInt(100) / 100.0) * 100) / 100.0;
        } else if (tier <= 8) {
            amount = Math.round((5.0 + rng.nextDouble() * 9994.99) * 100) / 100.0;
        } else {
            amount = 100000.0 + rng.nextInt(900000);
        }

        // Timestamp: within last 7 days, UTC
        OffsetDateTime ts = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(rng.nextLong(0, 7L * 24 * 3600));
        String timestamp = ts.format(TS_FMT);

        // Reference: MOCKJ-TRN{YYYYMMDD}-{10000-99999}
        String ref = "MOCKJ-TRN" + LocalDate.now().format(DATE_FMT) + "-" + (rng.nextInt(90000) + 10000);

        // IBAN (non-US) or routing (US)
        String senderIban, receiverIban;
        if ("US".equals(locale)) {
            senderIban   = "RT:" + routingNumber(rng);
            receiverIban = "RT:" + routingNumber(rng);
        } else {
            senderIban   = FinancialGen.iban(rng, locale);
            receiverIban = FinancialGen.iban(rng, locale);
        }

        // Description and channel pools
        String[] descs = switch (locale) {
            case "US" -> TX_DESC_US; case "UK" -> TX_DESC_UK; case "DE" -> TX_DESC_DE;
            case "FR" -> TX_DESC_FR; case "RU" -> TX_DESC_RU; default   -> TX_DESC_TR;
        };
        String[] chans = switch (locale) {
            case "US" -> TX_CHAN_US; case "UK" -> TX_CHAN_UK; case "DE" -> TX_CHAN_DE;
            case "FR" -> TX_CHAN_FR; case "RU" -> TX_CHAN_RU; default   -> TX_CHAN_TR;
        };

        // Status: 80% COMPLETED, 15% PENDING, 5% FAILED
        int r = rng.nextInt(20);
        String status = r < 16 ? "COMPLETED" : r < 19 ? "PENDING" : "FAILED";

        return String.format(
            "{\"ref\":\"%s\",\"sender_iban\":\"%s\",\"receiver_iban\":\"%s\",\"amount\":%.2f," +
            "\"currency\":\"%s\",\"description\":\"%s\",\"channel\":\"%s\",\"timestamp\":\"%s\",\"status\":\"%s\"}",
            ref, senderIban, receiverIban, amount, ccy,
            pick(rng, descs).replace("\"", "\\\""),
            pick(rng, chans), timestamp, status
        );
    }

    // ── Bank name ─────────────────────────────────────────────────────────────

    private static String bankName(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> pick(rng, BANK_NAMES_TR);
            case "DE" -> pick(rng, BANK_NAMES_DE);
            case "FR" -> pick(rng, BANK_NAMES_FR);
            case "UK" -> pick(rng, BANK_NAMES_UK);
            case "US" -> pick(rng, BANK_NAMES_US);
            case "RU" -> pick(rng, BANK_NAMES_RU);
            default   -> pick(rng, BANK_NAMES_TR);
        };
    }

    // ── SEPA Reference (ISO 11649 creditor reference) ────────────────────────

    private static String sepaRef(ThreadLocalRandom rng) {
        int len = 10 + rng.nextInt(11);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("MOCKJ-E2E-");
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }

    // ── Creditor Reference (ISO 11649) ────────────────────────────────────────

    private static String creditorRef(ThreadLocalRandom rng) {
        String body = randomAlphaNum(rng, 21).toUpperCase();
        // RF + check (MOD-97) + body
        String check = FinancialGen.ibanCheckDigits("RF", body);
        return "RF" + check + body;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String randomAlpha(ThreadLocalRandom rng, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append((char)('A' + rng.nextInt(26)));
        return sb.toString();
    }

    private static String randomAlphaNum(ThreadLocalRandom rng, int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }
}
