package com.mockjutsu.jmeter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Algorithm compliance tests — known real-world test vectors.
 *
 * These tests verify that generator algorithms match published standards.
 * When adding a new generator: add test vectors here.
 *
 * Standard references:
 *  - CUSIP: ABA https://www.cusip.com/identifiers.html
 *  - ABA Routing: Federal Reserve https://www.federalreserve.gov/
 *  - TCKN: Türkiye Nüfus Müdürlüğü algorithm
 *  - EAN/GS1: ISO/IEC 15420, https://www.gs1.org/
 *  - ISIN: ISO 6166
 *  - Luhn: ISO/IEC 7812-1
 *  - NHS: https://www.datadictionary.nhs.uk/
 *  - IBAN: ISO 13616 / SWIFT
 *  - ABA Routing: 3*(d0+d3+d6) + 7*(d1+d4+d7) + (d2+d5+d8) ≡ 0 mod 10
 */
class KnownVectorTest {

    // ── Checksum helpers ──────────────────────────────────────────────────────

    private static int cusipCheckDigit(String payload) {
        int total = 0;
        for (int i = 0; i < payload.length(); i++) {
            char c = payload.charAt(i);
            int v = Character.isDigit(c) ? c - '0' : c - 'A' + 10;
            if (i % 2 == 1) v *= 2;
            total += v / 10 + v % 10;
        }
        return (10 - total % 10) % 10;
    }

    private static boolean abaValid(String rtn) {
        int[] d = new int[9];
        for (int i = 0; i < 9; i++) d[i] = rtn.charAt(i) - '0';
        return (3*(d[0]+d[3]+d[6]) + 7*(d[1]+d[4]+d[7]) + (d[2]+d[5]+d[8])) % 10 == 0;
    }

    private static boolean luhnValid(String number) {
        int total = 0;
        boolean alt = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alt) { n *= 2; if (n > 9) n -= 9; }
            total += n;
            alt = !alt;
        }
        return total % 10 == 0;
    }

    private static boolean eanValid(String number) {
        int total = 0;
        for (int i = 0; i < number.length() - 1; i++) {
            int d = number.charAt(i) - '0';
            total += (i % 2 == 0) ? d : d * 3;
        }
        int check = (10 - total % 10) % 10;
        return check == (number.charAt(number.length()-1) - '0');
    }

    private static boolean tckn_valid(String n) {
        if (n.length() != 11 || n.charAt(0) == '0') return false;
        int[] d = new int[11];
        for (int i = 0; i < 11; i++) d[i] = n.charAt(i) - '0';
        int odd = d[0]+d[2]+d[4]+d[6]+d[8];
        int even = d[1]+d[3]+d[5]+d[7];
        if ((7*odd - even + 100) % 10 != d[9]) return false;
        int sum = 0; for (int i = 0; i < 10; i++) sum += d[i];
        return sum % 10 == d[10];
    }

    private static boolean nhsValid(String n) {
        if (n.length() != 10) return false;
        int[] w = {10,9,8,7,6,5,4,3,2};
        int total = 0;
        for (int i = 0; i < 9; i++) total += (n.charAt(i)-'0') * w[i];
        int rem = total % 11;
        int check = (rem == 0) ? 0 : 11 - rem;
        return check != 10 && check == (n.charAt(9)-'0');
    }

    // ── Known vector tests ────────────────────────────────────────────────────

    @Test void cusip_apple_valid()   { assertEquals(0, cusipCheckDigit("03783310")); }
    @Test void cusip_oracle_valid()  { assertEquals(5, cusipCheckDigit("68389X10")); }
    @Test void cusip_ibm_valid()     { assertEquals(1, cusipCheckDigit("45920010")); }

    @ParameterizedTest
    @ValueSource(strings = {"021000021", "121042882", "011000138"})
    void aba_routing_valid(String rtn) { assertTrue(abaValid(rtn), "ABA routing failed: " + rtn); }

    @ParameterizedTest
    @ValueSource(strings = {"021000020", "121042883"})
    void aba_routing_invalid(String rtn) { assertFalse(abaValid(rtn), "Expected invalid ABA: " + rtn); }

    @ParameterizedTest
    @ValueSource(strings = {"4532015112830366", "5425233430109903", "374251018720955"})
    void luhn_valid(String card) { assertTrue(luhnValid(card), "Luhn failed: " + card); }

    @ParameterizedTest
    @ValueSource(strings = {"4532015112830367", "5425233430109904"})
    void luhn_invalid(String card) { assertFalse(luhnValid(card), "Expected invalid Luhn: " + card); }

    @ParameterizedTest
    @ValueSource(strings = {"5901234123457", "4006381333931", "9780306406157"})
    void ean13_valid(String ean) { assertTrue(eanValid(ean), "EAN-13 failed: " + ean); }

    @ParameterizedTest
    @ValueSource(strings = {"5901234123458", "4006381333932"})
    void ean13_invalid(String ean) { assertFalse(eanValid(ean), "Expected invalid EAN-13: " + ean); }

    @Test void tckn_known_valid()   { assertTrue(tckn_valid("12345678028")); }
    @Test void tckn_known_invalid() { assertFalse(tckn_valid("12345678021")); }

    @ParameterizedTest
    @ValueSource(strings = {"9434765870", "4505577104"})
    void nhs_valid(String nhs) { assertTrue(nhsValid(nhs), "NHS failed: " + nhs); }

    // ── Generator output format tests ─────────────────────────────────────────

    private static final int SAMPLES = 200;

    @Test void generated_cusip_passes_checksum() {
        for (int i = 0; i < SAMPLES; i++) {
            String v = MockJutsuRegistry.generate("cusip", "TR");
            assertEquals(9, v.length(), "CUSIP must be 9 chars: " + v);
            assertEquals(cusipCheckDigit(v.substring(0,8)), v.charAt(8)-'0',
                "Generated CUSIP failed checksum: " + v);
        }
    }

    @Test void generated_routing_number_passes_checksum() {
        for (int i = 0; i < SAMPLES; i++) {
            String v = MockJutsuRegistry.generate("routing_number", "US");
            assertTrue(abaValid(v), "Generated ABA routing failed: " + v);
        }
    }

    @Test void generated_ean13_passes_checksum() {
        for (int i = 0; i < SAMPLES; i++) {
            String v = MockJutsuRegistry.generate("ean13", "TR");
            assertTrue(eanValid(v), "Generated EAN-13 failed: " + v);
        }
    }

    @Test void generated_luhn_card_passes_checksum() {
        for (int i = 0; i < SAMPLES; i++) {
            String v = MockJutsuRegistry.generate("cardnum", "TR");
            assertTrue(luhnValid(v), "Generated card number failed Luhn: " + v);
        }
    }

    @Test void generated_tckn_passes_algorithm() {
        for (int i = 0; i < SAMPLES; i++) {
            String v = MockJutsuRegistry.generate("tckn", "TR");
            assertTrue(tckn_valid(v), "Generated TCKN failed: " + v);
        }
    }

    @Test void generated_nhs_passes_algorithm() {
        for (int i = 0; i < SAMPLES; i++) {
            String v = MockJutsuRegistry.generate("nhs_number", "UK");
            // NHS format from registry: "ddd ddd dddd" — strip spaces before validating
            String digits = v.replace(" ", "");
            assertTrue(nhsValid(digits), "Generated NHS failed: " + v);
        }
    }

    @Test void generated_ssn_area_valid() {
        Pattern p = Pattern.compile("^(?!000|666|9\\d\\d)\\d{3}-(?!00)\\d{2}-(?!0000)\\d{4}$");
        for (int i = 0; i < 500; i++) {
            String v = MockJutsuRegistry.generate("ssn", "US");
            assertTrue(p.matcher(v).matches(), "SSN area invalid: " + v);
        }
    }

    @Test void generated_uuid_v4_format() {
        Pattern p = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
        for (int i = 0; i < SAMPLES; i++) {
            String v = MockJutsuRegistry.generate("uuid", "TR");
            assertTrue(p.matcher(v).matches(), "UUID v4 format wrong: " + v);
        }
    }
}
