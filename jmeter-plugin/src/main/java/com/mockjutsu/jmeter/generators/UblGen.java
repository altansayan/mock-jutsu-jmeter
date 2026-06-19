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

        // Locale'e gore para birimi ve KDV orani
        String ccy    = switch (locale) { case "DE","FR" -> "EUR"; case "UK" -> "GBP"; case "US" -> "USD"; case "RU" -> "RUB"; default -> "TRY"; };
        double vatPct = switch (locale) { case "DE" -> 19.0; case "US" -> 8.0; default -> 20.0; };

        // Rastgele birim fiyat ve miktar
        int    qty        = rng.nextInt(1, 20);
        double unitPrice  = rng.nextDouble(10, 500);
        double lineExt    = Math.round(qty * unitPrice * 100.0) / 100.0;
        double taxAmt     = Math.round(lineExt * vatPct / 100.0 * 100.0) / 100.0;
        double taxInclusive = Math.round((lineExt + taxAmt) * 100.0) / 100.0;

        String iban  = FinancialGen.iban(rng, locale);

        // FIX 4 — InvoiceLine (en az 1 adet zorunlu)
        String invoiceLine =
               "<cac:InvoiceLine xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"" +
               " xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">" +
               "<cbc:ID>1</cbc:ID>" +
               "<cbc:InvoicedQuantity unitCode=\"EA\">" + qty + "</cbc:InvoicedQuantity>" +
               "<cbc:LineExtensionAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", lineExt) + "</cbc:LineExtensionAmount>" +
               "<cac:Item><cbc:Name>Test Urun</cbc:Name></cac:Item>" +
               "<cac:Price><cbc:PriceAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", unitPrice) + "</cbc:PriceAmount></cac:Price>" +
               "</cac:InvoiceLine>";

        // FIX 5 — TaxTotal (zorunlu)
        String taxTotal =
               "<cac:TaxTotal xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"" +
               " xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">" +
               "<cbc:TaxAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", taxAmt) + "</cbc:TaxAmount>" +
               "<cac:TaxSubtotal>" +
               "<cbc:TaxableAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", lineExt) + "</cbc:TaxableAmount>" +
               "<cbc:TaxAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", taxAmt) + "</cbc:TaxAmount>" +
               "<cac:TaxCategory>" +
               "<cbc:Percent>" + (int) vatPct + "</cbc:Percent>" +
               "<cac:TaxScheme><cbc:ID>VAT</cbc:ID></cac:TaxScheme>" +
               "</cac:TaxCategory>" +
               "</cac:TaxSubtotal>" +
               "</cac:TaxTotal>";

        // FIX 6 — LegalMonetaryTotal (TaxExclusiveAmount + TaxInclusiveAmount zorunlu, tutarlar tutarli)
        String legalMonetary =
               "<cac:LegalMonetaryTotal xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"" +
               " xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">" +
               "<cbc:LineExtensionAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", lineExt) + "</cbc:LineExtensionAmount>" +
               "<cbc:TaxExclusiveAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", lineExt) + "</cbc:TaxExclusiveAmount>" +
               "<cbc:TaxInclusiveAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", taxInclusive) + "</cbc:TaxInclusiveAmount>" +
               "<cbc:PayableAmount currencyID=\"" + ccy + "\">" + String.format("%.2f", taxInclusive) + "</cbc:PayableAmount>" +
               "</cac:LegalMonetaryTotal>";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"" +
               " xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"" +
               " xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">" +
               "<cbc:ID>" + invId + "</cbc:ID><cbc:IssueDate>" + date + "</cbc:IssueDate>" +
               "<cbc:DocumentCurrencyCode>" + ccy + "</cbc:DocumentCurrencyCode>" +
               "<cac:AccountingSupplierParty><cac:Party><cac:PartyName><cbc:Name>MOCK SUPPLIER</cbc:Name></cac:PartyName></cac:Party></cac:AccountingSupplierParty>" +
               "<cac:AccountingCustomerParty><cac:Party><cac:PartyName><cbc:Name>MOCK BUYER</cbc:Name></cac:PartyName></cac:Party></cac:AccountingCustomerParty>" +
               "<cac:PaymentMeans><cac:PayeeFinancialAccount><cbc:ID>" + iban + "</cbc:ID></cac:PayeeFinancialAccount></cac:PaymentMeans>" +
               taxTotal +
               legalMonetary +
               invoiceLine +
               "</Invoice>";
    }

    private static String xmldsig() {
        byte[] sig = new byte[128]; SEC.nextBytes(sig);
        byte[] cert = new byte[256]; SEC.nextBytes(cert);
        // Synthetic DER certificate: set valid ASN.1 SEQUENCE header (0x30 0x82)
        cert[0] = 0x30; cert[1] = (byte) 0x82; cert[2] = 0x00; cert[3] = (byte) (cert.length - 4);
        String sigVal  = Base64.getMimeEncoder(76, new byte[]{'\n'}).encodeToString(sig);
        String certVal = Base64.getEncoder().encodeToString(cert);
        return "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">" +
               "<SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>" +
               "<SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/></SignedInfo>" +
               "<SignatureValue>" + sigVal + "\n</SignatureValue>" +
               "<KeyInfo><X509Data><ds:X509Certificate>" + certVal + "</ds:X509Certificate></X509Data></KeyInfo>" +
               "</Signature>";
    }
}
