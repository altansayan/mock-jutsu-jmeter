package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class UblGen {
    private UblGen() {}
    private static final SecureRandom SEC = new SecureRandom();

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "ubl_invoice" -> ublInvoice(rng, locale);
            case "xmldsig"     -> xmldsig();
            default -> "ERROR: Unknown UBL type '" + type + "'";
        };
    }

    private static String ublInvoice(ThreadLocalRandom rng, String locale) {
        String invId = "INV-" + String.format("%08d", rng.nextInt(10000000,99999999));
        String date  = java.time.LocalDate.now().toString();
        double amount = rng.nextDouble(100, 10000);
        String ccy   = switch (locale) { case "DE","FR" -> "EUR"; case "UK" -> "GBP"; case "US" -> "USD"; case "RU" -> "RUB"; default -> "TRY"; };
        String iban  = FinancialGen.iban(rng, locale);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\">" +
               "<ID>" + invId + "</ID><IssueDate>" + date + "</IssueDate>" +
               "<DocumentCurrencyCode>" + ccy + "</DocumentCurrencyCode>" +
               "<AccountingSupplierParty><Party><PartyName><Name>MOCK SUPPLIER</Name></PartyName></Party></AccountingSupplierParty>" +
               "<AccountingCustomerParty><Party><PartyName><Name>MOCK BUYER</Name></PartyName></Party></AccountingCustomerParty>" +
               "<PaymentMeans><PayeeFinancialAccount><ID>" + iban + "</ID></PayeeFinancialAccount></PaymentMeans>" +
               "<LegalMonetaryTotal><PayableAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", amount) + "</PayableAmount></LegalMonetaryTotal>" +
               "</Invoice>";
    }

    private static String xmldsig() {
        byte[] sig = new byte[128]; SEC.nextBytes(sig);
        String sigVal = Base64.getMimeEncoder(76, new byte[]{'\n'}).encodeToString(sig);
        return "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">" +
               "<SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>" +
               "<SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/></SignedInfo>" +
               "<SignatureValue>" + sigVal + "\n</SignatureValue></Signature>";
    }
}
