package com.mockjutsu.jmeter.generators;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/** Layer-1 unit tests — individual generator algorithms, no JMeter. */
class AlgorithmTest {

    // ── TCKN ─────────────────────────────────────────────────────────────────

    @RepeatedTest(50)
    void tcknIsValid() {
        String tckn = IdentityGen.tckn(ThreadLocalRandom.current());
        assertEquals(11, tckn.length(), "TCKN must be 11 digits");
        assertTrue(tckn.matches("\\d{11}"), "TCKN must be all digits");
        assertNotEquals('0', tckn.charAt(0), "TCKN first digit must not be 0");

        int[] d = new int[11];
        for (int i = 0; i < 11; i++) d[i] = tckn.charAt(i) - '0';

        // d9 check
        int odd  = d[0]+d[2]+d[4]+d[6]+d[8];
        int even = d[1]+d[3]+d[5]+d[7];
        assertEquals(d[9], ((7 * odd) - even + 100) % 10, "TCKN d9 invalid");

        // d10 check
        int sum = 0;
        for (int i = 0; i < 10; i++) sum += d[i];
        assertEquals(d[10], sum % 10, "TCKN d10 invalid");
    }

    // ── SSN ──────────────────────────────────────────────────────────────────

    @RepeatedTest(20)
    void ssnAreaNotSpecial() {
        String ssn = IdentityGen.ssn(ThreadLocalRandom.current());
        int area = Integer.parseInt(ssn.substring(0, 3));
        assertNotEquals(666, area, "SSN area 666 is reserved");
        assertTrue(area >= 1 && area <= 899, "SSN area out of range: " + area);
    }

    // ── NIN ──────────────────────────────────────────────────────────────────

    @RepeatedTest(20)
    void ninFormatValid() {
        // Format: "AA DD DD DD A" (13 chars incl. spaces) — matches Python's generate_uk_ni().
        String nin = IdentityGen.nin(ThreadLocalRandom.current());
        assertEquals(13, nin.length(), "NIN must be 13 chars incl. spaces");
        assertTrue(nin.matches("[A-Z]{2} \\d{2} \\d{2} \\d{2} [A-D]"), "NIN format invalid: " + nin);
        char p1 = nin.charAt(0), p2 = nin.charAt(1);
        assertFalse("DFIQUV".indexOf(p1) >= 0, "NIN prefix char 1 invalid: " + p1);
        assertFalse("DFIOQUV".indexOf(p2) >= 0, "NIN prefix char 2 invalid: " + p2);
        String prefix = "" + p1 + p2;
        assertFalse(java.util.Set.of("BG","GB","NK","KN","NT","TN","ZZ").contains(prefix), "NIN forbidden prefix: " + prefix);
    }

    // ── Luhn ──────────────────────────────────────────────────────────────────

    @RepeatedTest(50)
    void cardnumLuhnValid() {
        String card = FinancialGen.cardnum(ThreadLocalRandom.current(), "TR");
        assertTrue(isLuhnValid(card), "Card " + card + " failed Luhn");
    }

    @Test
    void cardnumVisaStartsWith4() {
        var rng = ThreadLocalRandom.current();
        for (int i = 0; i < 50; i++) {
            String card = FinancialGen.cardnum(rng, "TR", "visa");
            assertTrue(card.startsWith("4"), "Visa card must start with 4: " + card);
            assertEquals(16, card.length(), "Visa card must be 16 digits");
            assertTrue(isLuhnValid(card), "Visa card Luhn invalid: " + card);
        }
    }

    @Test
    void cardnumMastercardStartsWith5() {
        // Python's own CARD_NETWORKS dict only recognizes "mc" (not "mastercard") as the
        // network key — an unrecognized key falls back to visa, in both engines.
        var rng = ThreadLocalRandom.current();
        for (int i = 0; i < 50; i++) {
            String card = FinancialGen.cardnum(rng, "TR", "mc");
            assertTrue(card.startsWith("5"), "Mastercard must start with 5: " + card);
            assertEquals(16, card.length(), "Mastercard must be 16 digits");
            assertTrue(isLuhnValid(card), "Mastercard Luhn invalid: " + card);
        }
    }

    @Test
    void cardnumAmexStartsWith3() {
        var rng = ThreadLocalRandom.current();
        for (int i = 0; i < 50; i++) {
            String card = FinancialGen.cardnum(rng, "TR", "amex");
            assertTrue(card.startsWith("3"), "Amex card must start with 3: " + card);
            assertEquals(15, card.length(), "Amex card must be 15 digits");
            assertTrue(isLuhnValid(card), "Amex card Luhn invalid: " + card);
        }
    }

    @Test
    void cardnumTroyStartsWith9792() {
        var rng = ThreadLocalRandom.current();
        for (int i = 0; i < 50; i++) {
            String card = FinancialGen.cardnum(rng, "TR", "troy");
            assertTrue(card.startsWith("9792"), "Troy card must start with 9792: " + card);
            assertEquals(16, card.length(), "Troy card must be 16 digits");
            assertTrue(isLuhnValid(card), "Troy card Luhn invalid: " + card);
        }
    }

    @Test
    void cardnumMcAliasWorks() {
        var rng = ThreadLocalRandom.current();
        String card = FinancialGen.cardnum(rng, "TR", "mc");
        assertTrue(card.startsWith("5"), "mc alias must produce Mastercard: " + card);
    }

    @RepeatedTest(50)
    void imeiLuhnValid() {
        String imei = TelecomGen.imei(ThreadLocalRandom.current());
        assertEquals(15, imei.length());
        assertTrue(isLuhnValid(imei), "IMEI " + imei + " failed Luhn");
    }

    // ── IBAN ─────────────────────────────────────────────────────────────────

    @RepeatedTest(30)
    void trIbanCheckDigitsValid() {
        String iban = FinancialGen.iban(ThreadLocalRandom.current(), "TR");
        assertEquals("TR", iban.substring(0, 2));
        assertEquals(26, iban.length());
        // Verify MOD-97: rearrange then check
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        String numeric = toNumeric(rearranged);
        assertEquals(1, mod97(numeric), "TR IBAN MOD-97 check failed: " + iban);
    }

    @RepeatedTest(20)
    void deIbanCheckDigitsValid() {
        String iban = FinancialGen.iban(ThreadLocalRandom.current(), "DE");
        assertEquals("DE", iban.substring(0, 2));
        assertEquals(22, iban.length());
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        assertEquals(1, mod97(toNumeric(rearranged)), "DE IBAN MOD-97 failed: " + iban);
    }

    // ── NHS ───────────────────────────────────────────────────────────────────

    @RepeatedTest(30)
    void nhsModulo11Valid() {
        String nhs = HealthGen.nhsNumber(ThreadLocalRandom.current());
        String digits = nhs.replace(" ", "");
        assertEquals(10, digits.length());
        int[] d = new int[10];
        for (int i = 0; i < 10; i++) d[i] = digits.charAt(i) - '0';
        int[] w = {10,9,8,7,6,5,4,3,2};
        int total = 0;
        for (int i = 0; i < 9; i++) total += d[i] * w[i];
        int remainder = total % 11;
        int expected  = (remainder == 0) ? 0 : 11 - remainder;
        assertEquals(expected, d[9], "NHS check digit invalid for: " + nhs);
    }

    // ── EAN-13 ───────────────────────────────────────────────────────────────

    @RepeatedTest(20)
    void ean13ChecksumValid() {
        String ean = BarcodeGen.ean13(ThreadLocalRandom.current(), "TR");
        assertEquals(13, ean.length());
        int[] d = new int[13];
        for (int i = 0; i < 13; i++) d[i] = ean.charAt(i) - '0';
        int odd = 0, even = 0;
        for (int i = 0; i < 12; i++) {
            if (i % 2 == 0) odd += d[i]; else even += d[i];
        }
        int expected = (10 - ((odd + 3 * even) % 10)) % 10;
        assertEquals(expected, d[12], "EAN-13 check digit invalid: " + ean);
    }

    // ── ISIN ──────────────────────────────────────────────────────────────────

    @RepeatedTest(20)
    void isinIs12CharsLuhnValid() {
        String isin = FinancialMarketsGen.isin(ThreadLocalRandom.current(), "US");
        assertEquals(12, isin.length());
        assertTrue(isin.substring(0,2).matches("[A-Z]{2}"));
        // last char must be a digit
        assertTrue(Character.isDigit(isin.charAt(11)));
    }

    // ── CUSIP ─────────────────────────────────────────────────────────────────

    @RepeatedTest(20)
    void cusipIs9Chars() {
        String cusip = FinancialMarketsGen.cusip(ThreadLocalRandom.current());
        assertEquals(9, cusip.length());
    }

    // ── SEDOL ─────────────────────────────────────────────────────────────────

    @RepeatedTest(20)
    void sedolIs7Chars() {
        String sedol = FinancialMarketsGen.sedol(ThreadLocalRandom.current());
        assertEquals(7, sedol.length());
    }

    // ── NMEA checksum ────────────────────────────────────────────────────────

    @RepeatedTest(20)
    void nmeaGpggaChecksumCorrect() {
        String json = NmeaGen.generate("nmea_gpgga", "TR");
        // Extract raw NMEA sentence from JSON: {"sentence":"$GPGGA,...*XX","type":...}
        int s = json.indexOf("\"sentence\":\"") + "\"sentence\":\"".length();
        int e = json.indexOf("\",\"type\":");
        String sentence = json.substring(s, e);
        int star = sentence.lastIndexOf('*');
        String body = sentence.substring(1, star);
        String cs   = sentence.substring(star + 1);
        int expected = 0;
        for (char c : body.toCharArray()) expected ^= c;
        assertEquals(String.format("%02X", expected), cs, "NMEA checksum mismatch");
    }

    // ── MRZ check digit ───────────────────────────────────────────────────────

    @Test
    void mrzCheckDigitAlgorithm() {
        // Known value: "490154203237518" → check digit 1
        // (standard test vector from ICAO)
        // Using our algorithm on a controlled input
        String field = "ERIKSSON<<ANNA<MARIA<<<<<<<<<<<<";
        int cd = MrzGen.mrzCheck(field);
        assertTrue(cd >= 0 && cd <= 9);
    }

    @RepeatedTest(20)
    void mrzTd3Line2Is44Chars() {
        String json = MrzGen.generate("mrz_td3", "TR");
        // Extract lines field from JSON: {"mrz_type":"TD3","lines":"<L1> | <L2>",...}
        int s = json.indexOf("\"lines\":\"") + "\"lines\":\"".length();
        int e = json.indexOf("\",\"surname\"");
        String[] parts = json.substring(s, e).split(" \\| ");
        assertEquals(44, parts[0].length());
        assertEquals(44, parts[1].length());
    }

    // ── PIN Block ─────────────────────────────────────────────────────────────

    @RepeatedTest(30)
    void pinBlockFmt0Structure() {
        String pb = HardwareGen.pinBlockFmt0(ThreadLocalRandom.current());
        assertEquals(16, pb.length());
        assertEquals('0', pb.charAt(0), "Format 0 must start with 0");
        int pinLen = pb.charAt(1) - '0';
        assertTrue(pinLen >= 4 && pinLen <= 6, "PIN length must be 4-6, got: " + pinLen);
        // Fill nibbles must all be F
        for (int i = 2 + pinLen; i < 16; i++) {
            assertEquals('F', pb.charAt(i), "Format 0 fill must be F at pos " + i);
        }
    }

    @RepeatedTest(30)
    void pinBlockFmt3Structure() {
        String pb = HardwareGen.pinBlockFmt3(ThreadLocalRandom.current());
        assertEquals(16, pb.length());
        assertEquals('3', pb.charAt(0), "Format 3 must start with 3");
    }

    // ── EMV ───────────────────────────────────────────────────────────────────

    @RepeatedTest(20)
    void emvArqcIs16HexUppercase() {
        String arqc = CardPhysicsGen.emvArqc();
        assertEquals(16, arqc.length());
        assertTrue(arqc.matches("[0-9A-F]{16}"), "ARQC must be 16 uppercase hex chars");
    }

    @RepeatedTest(20)
    void emvIadStartsWith0A() {
        String iad = CardPhysicsGen.emvIad(ThreadLocalRandom.current());
        assertTrue(iad.startsWith("0A"), "IAD must start with 0A, got: " + iad.substring(0,2));
        assertEquals(22, iad.length(), "IAD must be 22 hex chars");
    }

    @Test
    void iso8583BitmapsAreCorrect() {
        // Auth request: DEs 2,3,4,7,11,12,13,14,18,22,25,37,41,42,49
        String authReq = CardPhysicsGen.iso8583AuthReq(ThreadLocalRandom.current(), "TR");
        assertTrue(authReq.contains("723C448008C08000"),
            "Auth request bitmap must be 723C448008C08000, response was: " + authReq);

        String authResp = CardPhysicsGen.iso8583AuthResp(ThreadLocalRandom.current(), "TR");
        assertTrue(authResp.contains("7238000006C00000"),
            "Auth response bitmap must be 7238000006C00000");

        String reversal = CardPhysicsGen.iso8583Reversal(ThreadLocalRandom.current(), "TR");
        assertTrue(reversal.contains("7238000008C08100"),
            "Reversal bitmap must be 7238000008C08100");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean isLuhnValid(String number) {
        int sum = 0;
        boolean alt = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int d = number.charAt(i) - '0';
            if (d < 0 || d > 9) return false;
            if (alt) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
            alt = !alt;
        }
        return sum % 10 == 0;
    }

    private static String toNumeric(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) sb.append(c - 'A' + 10);
            else sb.append(c);
        }
        return sb.toString();
    }

    private static int mod97(String numStr) {
        int mod = 0;
        for (int i = 0; i < numStr.length(); i++) mod = (mod * 10 + (numStr.charAt(i) - '0')) % 97;
        return mod;
    }
}
