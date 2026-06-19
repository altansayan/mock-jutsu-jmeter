package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class BankStatementGen {
    private BankStatementGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "mt940"  -> mt940(rng, locale);
            case "camt053"-> camt053(rng, locale);
            default -> "ERROR: Unknown bank statement type '" + type + "'";
        };
    }

    private static String mt940(ThreadLocalRandom rng, String locale) {
        String iban    = FinancialGen.iban(rng, locale);
        String bic     = BankingGen.bic(rng, locale, true);
        double open    = rng.nextDouble(1000, 50000);
        double close   = open + rng.nextDouble(-500, 5000);
        String ccy     = switch (locale) { case "DE","FR" -> "EUR"; case "UK" -> "GBP"; case "US" -> "USD"; case "RU" -> "RUB"; default -> "TRY"; };
        String date    = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        return ":20:MOCKJ" + String.format("%06d", rng.nextInt(100000,999999)) + "\n" +
               ":25:" + bic + "/" + iban + "\n" +
               ":28C:00001/001\n" +
               ":60F:C" + date + ccy + mt940Amount(open) + "\n" +
               ":61:" + date + String.format("%02d%02d", rng.nextInt(1,13), rng.nextInt(1,29)) + "C" + mt940Amount(rng.nextDouble(100,5000)) + "NMSC//MOCKREF\n" +
               ":86:Test transaction - MOCKJUTSU\n" +
               ":62F:C" + date + ccy + mt940Amount(close) + "\n-";
    }

    /** MT940 amount format: no thousands separator, comma as decimal mark. e.g. 1234,56 */
    private static String mt940Amount(double amount) {
        long whole = (long) amount;
        int cents = (int) Math.round((amount - whole) * 100);
        return whole + "," + String.format("%02d", cents);
    }

    private static String camt053(ThreadLocalRandom rng, String locale) {
        String iban  = FinancialGen.iban(rng, locale);
        double bal   = rng.nextDouble(1000, 50000);
        String ccy   = switch (locale) { case "DE","FR" -> "EUR"; case "UK" -> "GBP"; case "US" -> "USD"; case "RU" -> "RUB"; default -> "TRY"; };
        String date  = java.time.LocalDate.now().toString();
        String msgId = "MOCKJ-" + java.util.UUID.randomUUID().toString().substring(0,8).toUpperCase();
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.053.001.02\">" +
               "<BkToCstmrStmt><GrpHdr><MsgId>" + msgId + "</MsgId><CreDtTm>" + date + "T12:00:00</CreDtTm></GrpHdr>" +
               "<Stmt><Id>STMT001</Id><Acct><Id><IBAN>" + iban + "</IBAN></Id></Acct>" +
               "<Bal><Tp><CdOrPrtry><Cd>OPBD</Cd></CdOrPrtry></Tp>" +
               "<Amt Ccy=\"" + ccy + "\">" + String.format("%.2f", bal) + "</Amt>" +
               "<CdtDbtInd>CRDT</CdtDbtInd><Dt><Dt>" + date + "</Dt></Dt></Bal>" +
               "</Stmt></BkToCstmrStmt></Document>";
    }
}
