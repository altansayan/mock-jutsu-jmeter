package com.mockjutsu.jmeter.generators;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Payments — SWIFT MT103, PAIN.001, NACHA ACH, SEPA Mandate, Fedwire. Mirrors payments.py. */
public final class PaymentsGen {
    private PaymentsGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "swift_mt103"  -> mt103(rng, locale);
            case "pain001"      -> pain001(rng, locale);
            case "nacha_ach"    -> nacha(rng);
            case "sepa_mandate" -> sepaMandate(rng, locale);
            case "fedwire"      -> fedwire(rng);
            default -> "ERROR: Unknown payments type '" + type + "'";
        };
    }

    private static String mt103(ThreadLocalRandom rng, String locale) {
        String ref    = "MOCKJ" + String.format("%015d", rng.nextLong(100000000000000L, 999999999999999L));
        String bic    = BankingGen.bic(rng, locale, true);
        String iban   = FinancialGen.iban(rng, locale);
        double amount = rng.nextDouble(100, 100000);
        String ccy    = switch (locale) { case "DE","FR" -> "EUR"; case "UK" -> "GBP"; case "US" -> "USD"; case "RU" -> "RUB"; default -> "TRY"; };
        String date   = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        return "{20:" + ref + "}{23B:CRED}{32A:" + date + ccy + String.format("%.2f", amount) + "}" +
               "{50K:" + IdentityGen.fullname(rng, locale) + "}{52A:" + bic + "}" +
               "{57A:" + bic + "}{59:" + iban + "\n" + IdentityGen.fullname(rng, locale) + "}" +
               "{70:MOCKJUTSU TEST PAYMENT}{71A:OUR}";
    }

    private static String pain001(ThreadLocalRandom rng, String locale) {
        String msgId  = "MOCKJ-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        String iban   = FinancialGen.iban(rng, locale);
        double amount = rng.nextDouble(10, 5000);
        String ccy    = switch (locale) { case "DE","FR" -> "EUR"; case "UK" -> "GBP"; case "US" -> "USD"; case "RU" -> "RUB"; default -> "TRY"; };
        String date   = java.time.LocalDate.now().plusDays(1).toString();
        return "<?xml version=\"1.0\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.09\">" +
               "<CstmrCdtTrfInitn><GrpHdr><MsgId>" + msgId + "</MsgId><NbOfTxs>1</NbOfTxs>" +
               "<CtrlSum>" + String.format("%.2f", amount) + "</CtrlSum></GrpHdr>" +
               "<PmtInf><PmtMtd>TRF</PmtMtd><ReqdExctnDt><Dt>" + date + "</Dt></ReqdExctnDt>" +
               "<Dbtr><Nm>MOCK DEBTOR</Nm></Dbtr><DbtrAcct><Id><IBAN>" + iban + "</IBAN></Id></DbtrAcct>" +
               "<CdtTrfTxInf><Amt><InstdAmt Ccy=\"" + ccy + "\">" + String.format("%.2f", amount) + "</InstdAmt></Amt>" +
               "<CdtrAcct><Id><IBAN>" + FinancialGen.iban(rng, locale) + "</IBAN></Id></CdtrAcct>" +
               "<RmtInf><Ustrd>MOCKJUTSU TEST</Ustrd></RmtInf>" +
               "</CdtTrfTxInf></PmtInf></CstmrCdtTrfInitn></Document>";
    }

    private static String nacha(ThreadLocalRandom rng) {
        String routing = BankingGen.routingNumber(rng);
        String acct    = FinancialGen.randomDigits(rng, 10);
        double amount  = rng.nextDouble(10, 9999);
        String trace   = "09100" + String.format("%011d", rng.nextLong(10000000000L, 99999999999L));
        return "5220MOCK COMPANY      MOCKJUTSU         CCD" + String.format("%010d", rng.nextInt()) + "240101\n" +
               "6220" + routing + acct + String.format("%010d", (long)(amount * 100)) + "MOCK PAYEE      " + trace;
    }

    private static String sepaMandate(ThreadLocalRandom rng, String locale) {
        String id   = "MOCKJ-MANDATE-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        String iban = FinancialGen.iban(rng, locale.equals("TR") ? "DE" : locale);
        String bic  = BankingGen.bic(rng, locale, true);
        return "{\"mandateId\":\"" + id + "\",\"sequenceType\":\"RCUR\",\"iban\":\"" + iban + "\"," +
               "\"bic\":\"" + bic + "\",\"signatureDate\":\"" + java.time.LocalDate.now() + "\"," +
               "\"creditorId\":\"MOCKJDE00ZZZ12345\",\"status\":\"ACTIVE\"}";
    }

    private static String fedwire(ThreadLocalRandom rng) {
        String ref    = String.format("%06d", rng.nextInt(100000,999999));
        String date   = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        double amount = rng.nextDouble(1000, 1000000);
        String routing = BankingGen.routingNumber(rng);
        return String.format("{1500}%s%s%012.0f{2000}%s{3400}%s{4000}%s{6000}MOCKJUTSU TEST WIRE",
            date, ref, amount * 100, routing, routing, BankingGen.routingNumber(rng));
    }
}
