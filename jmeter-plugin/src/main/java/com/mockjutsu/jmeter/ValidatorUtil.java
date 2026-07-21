package com.mockjutsu.jmeter;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Regulation-compliant input validators — mirrors algorithms.py + validators.py.
 *
 * validate(type, value) returns:
 *   Boolean.TRUE   — value is valid for this type
 *   Boolean.FALSE  — value is invalid (checksum/format failed)
 *   null           — no validator registered for this type (not an error)
 */
public final class ValidatorUtil {

    private ValidatorUtil() {}

    // ══════════════════════════════════════════════════════════════════════════
    // ALGORITHM IMPLEMENTATIONS
    // ══════════════════════════════════════════════════════════════════════════

    /** Strip non-digit characters. */
    private static String digitsOnly(String s) {
        return s.replaceAll("[^0-9]", "");
    }

    /** Strip spaces. */
    private static String stripSpaces(String s) {
        return s.replaceAll("\\s", "");
    }

    /**
     * Luhn algorithm — validates full number including check digit.
     * ISO/IEC 7812-1.  Strips spaces and dashes before checking.
     */
    public static boolean luhnValid(String numberStr) {
        String digits = numberStr.replaceAll("[\\s\\-]", "");
        if (!digits.chars().allMatch(Character::isDigit)) return false;
        if (digits.length() < 2) return false;
        int total = 0;
        for (int i = 0; i < digits.length(); i++) {
            int d = digits.charAt(digits.length() - 1 - i) - '0';
            if (i % 2 == 1) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            total += d;
        }
        return total % 10 == 0;
    }

    /**
     * IBAN validation — ISO 13616, MOD-97.
     * Accepts spaces and lowercase.
     */
    public static boolean ibanValid(String ibanStr) {
        String s = stripSpaces(ibanStr).toUpperCase();
        if (s.length() < 5) return false;
        if (!Character.isLetter(s.charAt(0)) || !Character.isLetter(s.charAt(1))) return false;
        if (!Character.isDigit(s.charAt(2)) || !Character.isDigit(s.charAt(3))) return false;
        String rearranged = s.substring(4) + s.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) numeric.append(c - 'A' + 10);
            else numeric.append(c);
        }
        if (!numeric.chars().allMatch(Character::isDigit)) return false;
        return new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    /**
     * Turkish Republic ID (TC Kimlik Numarası) — TCKN.
     * 11 digits, d[0]≠0, dual MOD-10 checksum.
     */
    public static boolean tcknValid(String tcknStr) {
        String s = stripSpaces(tcknStr);
        if (s.length() != 11 || !s.chars().allMatch(Character::isDigit) || s.charAt(0) == '0') return false;
        int[] d = s.chars().map(c -> c - '0').toArray();
        int odd  = d[0] + d[2] + d[4] + d[6] + d[8];
        int even = d[1] + d[3] + d[5] + d[7];
        if ((odd * 7 - even) % 10 != d[9]) return false;
        int sum10 = 0;
        for (int i = 0; i < 10; i++) sum10 += d[i];
        return sum10 % 10 == d[10];
    }

    /**
     * Turkish Foreigner ID (Yabancı Kimlik Numarası) — YKN.
     * 11 digits, starts with "99", Luhn check on full number.
     */
    public static boolean yknValid(String yknStr) {
        String s = stripSpaces(yknStr);
        if (s.length() != 11 || !s.chars().allMatch(Character::isDigit) || !s.startsWith("99")) return false;
        return luhnValid(s);
    }

    /**
     * Turkish Tax ID (Vergi Kimlik Numarası) — VKN.
     * 10 digits, proprietary GİB checksum.
     */
    public static boolean vknValid(String vknStr) {
        String s = stripSpaces(vknStr);
        if (s.length() != 10 || !s.chars().allMatch(Character::isDigit)) return false;
        int[] d = s.chars().map(c -> c - '0').toArray();
        int total = 0;
        for (int i = 0; i < 9; i++) {
            int v = (d[i] + (9 - i)) % 10;
            int c;
            if (v != 0) {
                c = (v * (1 << (9 - i))) % 9;
                if (c == 0) c = 9;
            } else {
                c = 0;
            }
            total += c;
        }
        return (10 - total % 10) % 10 == d[9];
    }

    /**
     * US Social Security Number — SSN.
     * Format: AAA-GG-SSSS; area 001-899 excl. 000 and 666; group 01-99; serial 0001-9999.
     */
    public static boolean ssnValid(String ssnStr) {
        var m = Pattern.compile("(\\d{3})-(\\d{2})-(\\d{4})").matcher(ssnStr.strip());
        if (!m.matches()) return false;
        int area   = Integer.parseInt(m.group(1));
        int group  = Integer.parseInt(m.group(2));
        int serial = Integer.parseInt(m.group(3));
        return area != 0 && area != 666 && area <= 899 && group != 0 && serial != 0;
    }

    /**
     * UK National Insurance Number — NIN.
     * Format: XX 99 99 99 X (spaces optional).
     * Forbidden first chars: D F I Q U V
     * Forbidden second chars: D F I O Q U V
     * Forbidden prefixes: BG GB KN NK NT TN ZZ
     */
    public static boolean ninValid(String ninStr) {
        String s = stripSpaces(ninStr).toUpperCase();
        if (!s.matches("[A-Z]{2}\\d{6}[A-D]")) return false;
        String forbidden1 = "DFIQUV";
        String forbidden2 = "DFIOQUV";
        java.util.Set<String> forbiddenPfx = java.util.Set.of(
            "BG","GB","KN","NK","NT","TN","ZZ"
        );
        return forbidden1.indexOf(s.charAt(0)) < 0
            && forbidden2.indexOf(s.charAt(1)) < 0
            && !forbiddenPfx.contains(s.substring(0, 2));
    }

    /**
     * BIC/SWIFT code — ISO 9362.
     * 4-letter bank + 2-letter country + 2-char location + optional 3-char branch.
     * Length: 8 or 11 characters.
     */
    public static boolean bicValid(String bicStr) {
        String s = bicStr.strip().toUpperCase();
        return s.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DISPATCH TABLE
    // ══════════════════════════════════════════════════════════════════════════

    private static final Map<String, Function<String, Boolean>> VALIDATORS = Map.ofEntries(
        // Turkish
        Map.entry("tckn",    ValidatorUtil::tcknValid),
        Map.entry("ykn",     ValidatorUtil::yknValid),
        Map.entry("vkn",     ValidatorUtil::vknValid),
        Map.entry("taxid",   ValidatorUtil::vknValid),
        // Banking / Financial
        Map.entry("iban",    ValidatorUtil::ibanValid),
        Map.entry("cardnum", ValidatorUtil::luhnValid),
        Map.entry("bic",     ValidatorUtil::bicValid),
        Map.entry("swift",   ValidatorUtil::bicValid),
        // US
        Map.entry("ssn",     ValidatorUtil::ssnValid),
        Map.entry("ein",     v -> true),   // format-only
        // UK
        Map.entry("nin",     ValidatorUtil::ninValid),
        // Telecom (Luhn)
        Map.entry("imei",    ValidatorUtil::luhnValid),
        Map.entry("iccid",   ValidatorUtil::luhnValid)
    );

    /**
     * Validate {@code value} for the given Mock Jutsu {@code type}.
     *
     * @return {@code Boolean.TRUE} valid · {@code Boolean.FALSE} invalid ·
     *         {@code null} no validator registered for this type
     */
    public static Boolean validate(String type, String value) {
        if (type == null || value == null) return null;
        var fn = VALIDATORS.get(type.strip().toLowerCase());
        if (fn == null) return null;
        try {
            return fn.apply(value);
        } catch (Exception e) {
            return false;
        }
    }
}
