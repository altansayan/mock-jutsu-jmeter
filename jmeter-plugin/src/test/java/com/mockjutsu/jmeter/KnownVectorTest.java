package com.mockjutsu.jmeter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Algorithm compliance tests driven by compliance/algorithm_vectors.json.
 *
 * Single source of truth: mock-jutsu-api/compliance/algorithm_vectors.json
 * A copy lives at src/test/resources/compliance/algorithm_vectors.json in this repo.
 * When adding a new generator: update the JSON in BOTH repos (see GENERATOR_SOP.md).
 */
class KnownVectorTest {

    // ── JSON loading ──────────────────────────────────────────────────────────

    private static final ObjectMapper JSON = new ObjectMapper();

    private static JsonNode vectors() {
        try (InputStream in = KnownVectorTest.class.getResourceAsStream("/compliance/algorithm_vectors.json")) {
            if (in == null) throw new IllegalStateException(
                "compliance/algorithm_vectors.json not found on classpath — copy from mock-jutsu-api/compliance/");
            return JSON.readTree(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<String> arr(JsonNode node) {
        return StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText);
    }

    // ── Data providers (read from JSON) ──────────────────────────────────────

    static Stream<String> cusipValid()   { return arr(vectors().get("cusip").get("valid_complete")); }
    static Stream<String> cusipInvalid() { return arr(vectors().get("cusip").get("invalid_check")); }
    static Stream<String> abaValid()     { return arr(vectors().get("aba_routing").get("valid")); }
    static Stream<String> abaInvalid()   { return arr(vectors().get("aba_routing").get("invalid")); }
    static Stream<String> luhnValid()    { return arr(vectors().get("luhn").get("valid")); }
    static Stream<String> luhnInvalid()  { return arr(vectors().get("luhn").get("invalid")); }
    static Stream<String> ean13Valid()   { return arr(vectors().get("ean13").get("valid")); }
    static Stream<String> ean13Invalid() { return arr(vectors().get("ean13").get("invalid")); }
    static Stream<String> tcknValid()    { return arr(vectors().get("tckn").get("valid")); }
    static Stream<String> tcknInvalid()  { return arr(vectors().get("tckn").get("invalid")); }
    static Stream<String> nhsValid()     { return arr(vectors().get("nhs").get("valid")); }
    static Stream<String> nhsInvalid()   { return arr(vectors().get("nhs").get("invalid")); }

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
        int total = 0; boolean alt = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alt) { n *= 2; if (n > 9) n -= 9; }
            total += n; alt = !alt;
        }
        return total % 10 == 0;
    }

    private static boolean eanValid(String number) {
        int total = 0;
        for (int i = 0; i < number.length() - 1; i++) {
            int d = number.charAt(i) - '0';
            total += (i % 2 == 0) ? d : d * 3;
        }
        return (10 - total % 10) % 10 == (number.charAt(number.length()-1) - '0');
    }

    private static boolean tcknValid(String n) {
        if (n.length() != 11 || n.charAt(0) == '0') return false;
        int[] d = new int[11];
        for (int i = 0; i < 11; i++) d[i] = n.charAt(i) - '0';
        int odd = d[0]+d[2]+d[4]+d[6]+d[8], even = d[1]+d[3]+d[5]+d[7];
        if ((7*odd - even + 100) % 10 != d[9]) return false;
        int sum = 0; for (int i = 0; i < 10; i++) sum += d[i];
        return sum % 10 == d[10];
    }

    private static boolean nhsValid(String n) {
        if (n.length() != 10) return false;
        int[] w = {10,9,8,7,6,5,4,3,2};
        int total = 0;
        for (int i = 0; i < 9; i++) total += (n.charAt(i)-'0') * w[i];
        int rem = total % 11, check = (rem == 0) ? 0 : 11 - rem;
        return check != 10 && check == (n.charAt(9)-'0');
    }

    // ── Known vector tests (data-driven from JSON) ────────────────────────────

    @ParameterizedTest @MethodSource("cusipValid")
    void cusip_known_valid(String cusip) {
        assertEquals(cusipCheckDigit(cusip.substring(0,8)), cusip.charAt(8)-'0',
            "CUSIP vector failed: " + cusip);
    }

    @ParameterizedTest @MethodSource("cusipInvalid")
    void cusip_known_invalid(String cusip) {
        assertNotEquals(cusipCheckDigit(cusip.substring(0,8)), cusip.charAt(8)-'0',
            "Expected invalid CUSIP: " + cusip);
    }

    @ParameterizedTest @MethodSource("abaValid")
    void aba_routing_valid(String rtn) { assertTrue(abaValid(rtn), "ABA routing failed: " + rtn); }

    @ParameterizedTest @MethodSource("abaInvalid")
    void aba_routing_invalid(String rtn) { assertFalse(abaValid(rtn), "Expected invalid ABA: " + rtn); }

    @ParameterizedTest @MethodSource("luhnValid")
    void luhn_known_valid(String card) { assertTrue(luhnValid(card), "Luhn failed: " + card); }

    @ParameterizedTest @MethodSource("luhnInvalid")
    void luhn_known_invalid(String card) { assertFalse(luhnValid(card), "Expected invalid Luhn: " + card); }

    @ParameterizedTest @MethodSource("ean13Valid")
    void ean13_known_valid(String ean) { assertTrue(eanValid(ean), "EAN-13 failed: " + ean); }

    @ParameterizedTest @MethodSource("ean13Invalid")
    void ean13_known_invalid(String ean) { assertFalse(eanValid(ean), "Expected invalid EAN-13: " + ean); }

    @ParameterizedTest @MethodSource("tcknValid")
    void tckn_known_valid(String tckn) { assertTrue(tcknValid(tckn), "TCKN failed: " + tckn); }

    @ParameterizedTest @MethodSource("tcknInvalid")
    void tckn_known_invalid(String tckn) { assertFalse(tcknValid(tckn), "Expected invalid TCKN: " + tckn); }

    @ParameterizedTest @MethodSource("nhsValid")
    void nhs_known_valid(String nhs) { assertTrue(nhsValid(nhs), "NHS failed: " + nhs); }

    @ParameterizedTest @MethodSource("nhsInvalid")
    void nhs_known_invalid(String nhs) { assertFalse(nhsValid(nhs), "Expected invalid NHS: " + nhs); }

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
            assertTrue(tcknValid(v), "Generated TCKN failed: " + v);
        }
    }

    @Test void generated_nhs_passes_algorithm() {
        for (int i = 0; i < SAMPLES; i++) {
            String v = MockJutsuRegistry.generate("nhs_number", "UK");
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
