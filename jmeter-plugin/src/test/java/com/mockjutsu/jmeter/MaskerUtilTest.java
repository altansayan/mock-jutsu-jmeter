package com.mockjutsu.jmeter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for MaskerUtil.mask() — regulation compliance per PCI DSS, KVKK, GDPR, E.164. */
class MaskerUtilTest {

    // ── PCI DSS SAD — all alphanumeric must be redacted ──────────────────────

    @Test void cvv3_three_stars()  { assertEquals("***",  MaskerUtil.mask("cvv3",  "123")); }
    @Test void cvv4_four_stars()   { assertEquals("****", MaskerUtil.mask("cvv4",  "1234")); }
    @Test void pin_four_stars()    { assertEquals("****", MaskerUtil.mask("pin",   "4321")); }

    @ParameterizedTest
    @ValueSource(strings = {"track1_data", "track2_data", "3ds_cavv", "password", "password_hash", "pin_block", "chip_data"})
    void sad_all_alphanumeric_replaced(String type) {
        String masked = MaskerUtil.mask(type, "A1B2C3D4E5");
        assertTrue(masked.chars().allMatch(c -> c == '*' || !Character.isLetterOrDigit(c)),
            type + ": non-* alphanumeric found in '" + masked + "'");
    }

    // ── PCI DSS PAN §3.4.1 — BIN(6) + masked middle + last4 ─────────────────

    @Test void cardnum_bin6_visible() {
        String m = MaskerUtil.mask("cardnum", "4532015112830366").replace(" ", "");
        assertTrue(m.startsWith("453201"), "BIN not visible: " + m);
    }

    @Test void cardnum_last4_visible() {
        String m = MaskerUtil.mask("cardnum", "4111111111111111").replace(" ", "");
        assertTrue(m.endsWith("1111"), "Last 4 not visible: " + m);
    }

    @Test void cardnum_middle_six_stars() {
        String m = MaskerUtil.mask("cardnum", "4111111111111111").replace(" ", "");
        assertEquals("******", m.substring(6, 12), "Middle positions [6,12) must be *: " + m);
    }

    // ── KVKK Rehber 2.4 — Turkish IDs ────────────────────────────────────────

    @Test void tckn_first2_seven_stars_last2() {
        String m = MaskerUtil.mask("tckn", "25807694128");
        assertEquals("25",      m.substring(0, 2),  "TCKN first 2 must be visible");
        assertEquals("*******", m.substring(2, 9),  "TCKN middle 7 must be *");
        assertEquals("28",      m.substring(9, 11), "TCKN last 2 must be visible");
    }

    @Test void tckn_length_stays_11() {
        assertEquals(11, MaskerUtil.mask("tckn", "25807694128").length());
    }

    @Test void ykn_same_rule_as_tckn() {
        String m = MaskerUtil.mask("ykn", "98765432100");
        assertEquals("98", m.substring(0, 2));
        assertEquals("00", m.substring(9, 11));
        assertTrue(m.contains("*******"));
    }

    // ── US IDs ───────────────────────────────────────────────────────────────

    @Test void ssn_irs_exact_format() {
        assertEquals("***-**-6789", MaskerUtil.mask("ssn", "123-45-6789"));
    }

    @Test void ssn_shows_last4() {
        assertTrue(MaskerUtil.mask("ssn", "001-23-4567").endsWith("4567"));
    }

    // ── UK IDs ───────────────────────────────────────────────────────────────

    @Test void nin_masked_exact_format() {
        assertEquals("AB ** ** ** C", MaskerUtil.mask("nin", "AB123456C"));
    }

    // ── Banking / SEPA PSD2 ──────────────────────────────────────────────────

    @Test void iban_tr_prefix_and_last4() {
        String m = MaskerUtil.mask("iban", "TR330006100519786457841326").replace(" ", "");
        assertTrue(m.startsWith("TR33"),  "IBAN prefix not visible: " + m);
        assertTrue(m.endsWith("1326"),    "IBAN last 4 not visible: " + m);
        assertTrue(m.contains("****"),    "IBAN middle not masked: " + m);
    }

    @Test void iban_gb_prefix_and_last4() {
        String m = MaskerUtil.mask("iban", "GB29NWBK60161331926819").replace(" ", "");
        assertTrue(m.startsWith("GB29"));
        assertTrue(m.endsWith("6819"));
    }

    // ── Contact — GDPR Art.5 + E.164 ─────────────────────────────────────────

    @Test void email_first2_and_domain() {
        assertEquals("al***@gmail.com", MaskerUtil.mask("email", "altan@gmail.com"));
    }

    @Test void email_domain_untouched() {
        assertTrue(MaskerUtil.mask("email", "test@example.org").endsWith("@example.org"));
    }

    @Test void phone_e164_tr_prefix_last2() {
        String m = MaskerUtil.mask("phone", "+905325551234");
        assertTrue(m.startsWith("+90"), "Phone TR prefix: " + m);
        assertTrue(m.endsWith("34"),    "Phone last 2: " + m);
        assertTrue(m.contains("***"),   "Phone middle: " + m);
    }

    @Test void phone_e164_us_single_digit_cc() {
        String m = MaskerUtil.mask("phone", "+12125551234");
        assertTrue(m.startsWith("+1") && m.contains("***"));
    }

    // ── Demographics — GDPR ───────────────────────────────────────────────────

    @Test void birthdate_hides_month_and_day() {
        assertEquals("1990-**-**", MaskerUtil.mask("birthdate", "1990-05-14"));
    }

    @Test void birthdate_slash_separator() {
        assertEquals("1985/**/**", MaskerUtil.mask("birthdate", "1985/12/25"));
    }

    @Test void age_always_two_stars() {
        assertEquals("**", MaskerUtil.mask("age", "35"));
        assertEquals("**", MaskerUtil.mask("age", "7"));
    }

    // ── Names — GDPR ─────────────────────────────────────────────────────────

    @Test void firstname_first_char_plus_stars() { assertEquals("A***", MaskerUtil.mask("firstname", "Altan")); }
    @Test void lastname_first_char_plus_stars()  { assertEquals("S***", MaskerUtil.mask("lastname",  "Sezer")); }
    @Test void fullname_each_word_masked()        { assertEquals("E*** K***", MaskerUtil.mask("fullname", "Emre Kaya")); }

    @Test void fullname_three_words_all_masked() {
        String[] parts = MaskerUtil.mask("fullname", "Altan Sezer Ayan").split(" ");
        assertEquals(3, parts.length);
        for (String p : parts) assertTrue(p.endsWith("***"), "Word not masked: " + p);
    }

    // ── Documents ────────────────────────────────────────────────────────────

    @Test void passport_first2_last2() {
        String m = MaskerUtil.mask("passport", "P1234567");
        assertTrue(m.startsWith("P1") && m.endsWith("67") && m.contains("*"));
    }

    // ── Telecom ──────────────────────────────────────────────────────────────

    @Test void imei_tac8_visible() {
        String m = MaskerUtil.mask("imei", "356938035643809").replace("-", "");
        assertEquals("35693803", m.substring(0, 8), "TAC8 not visible: " + m);
        assertTrue(m.contains("*"));
    }

    // ── Network — GDPR ───────────────────────────────────────────────────────

    @Test void ipv4_last_two_octets_masked() {
        assertEquals("192.168.*.*", MaskerUtil.mask("ipv4", "192.168.1.42"));
    }

    @Test void mac_last_three_groups_masked() {
        assertEquals("A4:C3:F0:**:**:**", MaskerUtil.mask("mac_address", "A4:C3:F0:3D:8E:21"));
    }

    // ── Pass-through — non-maskable types ────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"uuid", "color", "ean13", "cardnetwork", "gender", "blood_type"})
    void non_maskable_passthrough(String type) {
        String val = "original_value_xyz";
        assertEquals(val, MaskerUtil.mask(type, val), type + " must pass through unchanged");
    }

    @Test void unknown_type_passthrough() {
        String val = "some_value_123";
        assertEquals(val, MaskerUtil.mask("zzz_nonexistent_type", val));
    }

    // ── Already-masked variants — no double masking ──────────────────────────

    @Test void tckn_masked_not_double_masked() {
        String already = "25*******28";
        assertEquals(already, MaskerUtil.mask("tckn_masked", already));
    }

    @Test void ssn_masked_not_double_masked() {
        String already = "***-**-6789";
        assertEquals(already, MaskerUtil.mask("ssn_masked", already));
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

    @Test void empty_string_no_crash() {
        assertDoesNotThrow(() -> MaskerUtil.mask("tckn", ""));
    }

    @Test void iban_too_short_no_crash() {
        String m = MaskerUtil.mask("iban", "TR");
        assertNotNull(m);
        assertTrue(m.contains("*"));
    }

    @Test void phone_without_plus_no_crash() {
        String m = MaskerUtil.mask("phone", "05325551234");
        assertNotNull(m);
        assertTrue(m.contains("***"));
    }

    @Test void single_char_name_masked() {
        assertEquals("A***", MaskerUtil.mask("firstname", "A"));
    }
}
