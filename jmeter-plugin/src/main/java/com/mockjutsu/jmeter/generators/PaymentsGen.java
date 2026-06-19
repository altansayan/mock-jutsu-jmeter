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
        // Full NACHA ACH file — mirrors payments.py (10 lines of 94 chars)
        String origRoutingRaw = BankingGen.routingNumber(rng).replace("-","");
        // File header (record type 1)
        String fileDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        String fileTime = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HHmm"));
        int    fileId   = rng.nextInt(10, 100);
        String destRouting = BankingGen.routingNumber(rng).replace("-","");
        String fh = String.format("101 %09d%010d%s%sA094101%-23s%-23s%-8s  ",
            Long.parseLong(destRouting), Long.parseLong(origRoutingRaw),
            fileDate, fileTime,
            "MOCKJ FEDERAL BANK", "MOCKJ CORP " + fileId, "");
        fh = padRight(fh, 94);

        // Batch header (record type 5)
        String batchDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        int batchNo = fileId * 1000 + rng.nextInt(1000);
        String bh = String.format("5200%-16s%-20s%010dCTX%-10s%6s   1%08d%08d",
            "MOCKJ CORP " + fileId, "", Long.parseLong(origRoutingRaw),
            "PAYMENT", batchDate, rng.nextInt(10000000,99999999), batchNo);
        bh = padRight(bh, 94);

        // Entry detail (record type 6)
        String acct    = FinancialGen.randomDigits(rng, 17);
        double amount  = rng.nextDouble(10, 9999);
        String payeeId = "MOCKJ-" + String.format("%06d", rng.nextInt(1000000));
        String trace   = origRoutingRaw.substring(0,8) + String.format("%07d", rng.nextInt(10000000));
        String ed = String.format("6220%09d%-17s%010d%-15s%-22s%01d%08d%07d",
            Long.parseLong(destRouting), acct, (long)(amount * 100),
            payeeId, "MOCKJ USER " + fileId, 0,
            rng.nextInt(10000000,99999999), rng.nextInt(1000000,9999999));
        ed = padRight(ed, 94);

        // Addenda (record type 7)
        String addRef = "MOCKJ PAYMENT REF " + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        String ad = String.format("705%-80s%04d%07d", padRight(addRef,80), 0, rng.nextInt(1000000,9999999));
        ad = padRight(ad, 94);

        // Batch control (record type 8)
        String bc = String.format("8%03d%06d%010d%012d%012d%010d%-39s%08d",
            200, 2, Long.parseLong(destRouting), (long)(amount*100), (long)(amount*100),
            Long.parseLong(origRoutingRaw), "", batchNo);
        bc = padRight(bc, 94);

        // File control (record type 9)
        String fc = String.format("9%06d%06d%08d%010d%012d%012d%-39s",
            1, 1, 2, Long.parseLong(destRouting), (long)(amount*100), (long)(amount*100), "");
        fc = padRight(fc, 94);

        // Padding lines (9s to fill block of 10)
        String pad = "9".repeat(94);
        return fh + "\n" + bh + "\n" + ed + "\n" + ad + "\n" + bc + "\n" + fc + "\n" + pad + "\n" + pad + "\n" + pad + "\n" + pad;
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }

    private static String sepaMandate(ThreadLocalRandom rng, String locale) {
        // Return XML string mirroring payments.py sepa_mandate (pain.008 XML)
        String mandateId = "MOCKJ-SDD-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        String pmtInfId  = "MOCKJ-SDD-PMT-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        String msgId     = "MOCKJ-SDD-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        String iban      = FinancialGen.iban(rng, locale.equals("TR") ? "DE" : locale);
        String bic       = BankingGen.bic(rng, "DE", true);
        double amount    = rng.nextDouble(10, 5000);
        String ccy       = locale.equals("TR") ? "EUR" : (locale.equals("UK") ? "GBP" : "EUR");
        String today     = java.time.LocalDate.now().toString();
        String ts        = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        int    corpNo    = rng.nextInt(10, 100);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.008.001.08\">\n" +
               "  <CstmrDrctDbtInitn>\n" +
               "    <GrpHdr>\n" +
               "      <MsgId>" + msgId + "</MsgId>\n" +
               "      <CreDtTm>" + ts + "</CreDtTm>\n" +
               "      <NbOfTxs>1</NbOfTxs>\n" +
               "      <InitgPty><Nm>MOCKJ CORP " + corpNo + "</Nm></InitgPty>\n" +
               "    </GrpHdr>\n" +
               "    <PmtInf>\n" +
               "      <PmtInfId>" + pmtInfId + "</PmtInfId>\n" +
               "      <PmtMtd>DD</PmtMtd>\n" +
               "      <ReqdColltnDt>" + today + "</ReqdColltnDt>\n" +
               "      <Cdtr><Nm>MOCKJ CORP " + corpNo + "</Nm></Cdtr>\n" +
               "      <CdtrAcct><Id><IBAN>" + iban + "</IBAN></Id></CdtrAcct>\n" +
               "      <CdtrAgt><FinInstnId><BIC>" + bic + "</BIC></FinInstnId></CdtrAgt>\n" +
               "      <DrctDbtTxInf>\n" +
               "        <PmtId><EndToEndId>" + mandateId + "</EndToEndId></PmtId>\n" +
               "        <InstdAmt Ccy=\"" + ccy + "\">" + String.format(java.util.Locale.US, "%.2f", amount) + "</InstdAmt>\n" +
               "        <DrctDbtTx><MndtRltdInf><MndtId>" + mandateId + "</MndtId><DtOfSgntr>" + today + "</DtOfSgntr></MndtRltdInf></DrctDbtTx>\n" +
               "        <Dbtr><Nm>MOCKJ DEBTOR</Nm></Dbtr>\n" +
               "        <DbtrAcct><Id><IBAN>" + FinancialGen.iban(rng, "DE") + "</IBAN></Id></DbtrAcct>\n" +
               "      </DrctDbtTxInf>\n" +
               "    </PmtInf>\n" +
               "  </CstmrDrctDbtInitn>\n" +
               "</Document>";
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
