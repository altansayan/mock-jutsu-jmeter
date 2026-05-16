package com.mockjutsu.jmeter;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Layer-3 format validation — every type checked against its actual output contract.
 * Replaces smoke-only assertFalse(result.isEmpty()) with real format assertions.
 */
class FormatValidationTest {

    // ── Identity ──────────────────────────────────────────────────────────────

    @RepeatedTest(20) void tckn_11_digits()        { assertMatches(g("tckn","TR"), "\\d{11}"); }
    @RepeatedTest(10) void ssn_format()             { assertMatches(g("ssn","US"), "\\d{3}-\\d{2}-\\d{4}"); }
    @RepeatedTest(10) void nin_format()             { assertMatches(g("nin","UK"), "[A-Z]{2}\\d{6}[A-D]"); }
    @RepeatedTest(10) void vkn_10_digits()          { assertMatches(g("vkn","TR"), "\\d{10}"); }
    @RepeatedTest(10) void ein_format()             { assertMatches(g("ein","US"), "\\d{2}-\\d{7}"); }
    @RepeatedTest(10) void sgk_format()             { assertMatches(g("sgk","TR"), "\\d{2}-\\d{7}-\\d\\.\\d{2}-\\d{2}"); }
    @RepeatedTest(10) void mersis_16_digits()       { assertMatches(g("mersis","TR"), "\\d{16}"); }

    @RepeatedTest(10) void age_18_to_99() {
        int age = Integer.parseInt(g("age","TR"));
        assertTrue(age >= 18 && age <= 99, "Age must be 18-99, got: " + age);
    }

    @RepeatedTest(10) void gender_valid() {
        String gv = g("gender","TR");
        assertTrue(gv.equals("M") || gv.equals("F") || gv.equalsIgnoreCase("Male")
            || gv.equalsIgnoreCase("Female") || gv.equals("Erkek") || gv.equals("Kadın"),
            "Invalid gender: " + gv);
    }

    @RepeatedTest(10) void tckn_masked_has_stars() { assertTrue(g("tckn_masked","TR").contains("*")); }
    @RepeatedTest(10) void ssn_masked_has_stars()  { assertTrue(g("ssn_masked","US").contains("*")); }

    @ParameterizedTest @ValueSource(strings = {"firstname","lastname","fullname","passport","license","nationality","birthdate"})
    void identity_strings_not_empty(String type) { assertNoError(g(type,"TR")); }

    // ── Financial ─────────────────────────────────────────────────────────────

    @RepeatedTest(20) void cardnum_luhn()    { assertTrue(luhn(g("cardnum","TR")), "Cardnum Luhn failed"); }
    @RepeatedTest(10) void cvv3_format()     { assertMatches(g("cvv3","TR"), "\\d{3}"); }
    @RepeatedTest(10) void cvv4_format()     { assertMatches(g("cvv4","TR"), "\\d{4}"); }
    @RepeatedTest(10) void pin_format()      { assertMatches(g("pin","TR"), "\\d{4}"); }
    @RepeatedTest(10) void expiry_format()   { assertMatches(g("expiry","TR"), "\\d{2}/\\d{2}"); }
    @RepeatedTest(10) void balance_decimal() { assertMatches(g("balance","TR"), "\\d+\\.\\d{2}"); }

    @RepeatedTest(10) void expirymonth_range() {
        int m = Integer.parseInt(g("expirymonth","TR"));
        assertTrue(m >= 1 && m <= 12, "Month out of range: " + m);
    }

    @ParameterizedTest @ValueSource(strings = {"TR","DE","FR","UK","RU"})
    void iban_starts_with_country(String locale) {
        String iban = g("iban", locale);
        String cc = locale.equals("UK") ? "GB" : locale;
        assertTrue(iban.startsWith(cc), "IBAN must start with " + cc + ", got: " + iban);
    }

    @RepeatedTest(10) void credit_score_range() {
        int cs = Integer.parseInt(g("credit_score","TR"));
        assertTrue(cs >= 300 && cs <= 850, "Credit score out of range: " + cs);
    }

    @RepeatedTest(10) void cardtype_valid() {
        String ct = g("cardtype","TR");
        assertTrue(ct.equals("credit") || ct.equals("debit") || ct.equals("prepaid"), "Invalid cardtype: " + ct);
    }

    @RepeatedTest(10) void cardstatus_valid() {
        String cs = g("cardstatus","TR");
        assertTrue(cs.equals("active") || cs.equals("inactive") || cs.equals("blocked") || cs.equals("expired"), "Invalid cardstatus: " + cs);
    }

    @RepeatedTest(10) void sepa_qr_starts_bcd() { assertTrue(g("sepa_qr","DE").startsWith("BCD")); }
    @RepeatedTest(10) void cavv_28_chars()       { assertEquals(28, g("3ds_cavv","TR").length()); }

    @RepeatedTest(10) void eci_valid() {
        String eci = g("3ds_eci","TR");
        assertTrue(eci.equals("05") || eci.equals("06") || eci.equals("07"), "Invalid ECI: " + eci);
    }

    // ── Meta ──────────────────────────────────────────────────────────────────

    @ParameterizedTest @ValueSource(strings = {"uuid","requestid","correlationid","sessionid","idempotencykey"})
    void uuid_format(String type) {
        assertMatches(g(type,"TR"), "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @RepeatedTest(10) void deviceid_uppercase_uuid() {
        assertMatches(g("deviceid","TR"), "[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}");
    }

    @RepeatedTest(10) void ipv4_format()     { assertMatches(g("ipv4","TR"), "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"); }
    @RepeatedTest(10) void ipv6_7_colons()   { assertEquals(7, g("ipv6","TR").chars().filter(c -> c==':').count()); }

    @RepeatedTest(10) void public_ip_not_private() {
        String ip = g("public_ip","TR");
        assertFalse(ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("127."), "Public IP must not be private: " + ip);
    }

    @RepeatedTest(10) void private_ip_rfc1918() {
        String ip = g("private_ip","TR");
        assertTrue(ip.startsWith("10.") || ip.startsWith("172.") || ip.startsWith("192.168."), "Not RFC1918: " + ip);
    }

    @RepeatedTest(10) void useragent_mozilla()   { assertTrue(g("useragent","TR").startsWith("Mozilla/5.0")); }
    @RepeatedTest(10) void jwt_two_dots()        { assertEquals(2, g("jwt","TR").chars().filter(c -> c=='.').count()); }
    @RepeatedTest(10) void bearer_prefix()       { assertTrue(g("bearertoken","TR").startsWith("Bearer ")); }
    @RepeatedTest(10) void hash_64_hex()         { assertMatches(g("hash","TR"), "[0-9a-f]{64}"); }
    @RepeatedTest(10) void mac_address_format()  { assertMatches(g("mac_address","TR"), "[0-9A-F]{2}(:[0-9A-F]{2}){5}"); }
    @RepeatedTest(10) void color_hex_format()    { assertMatches(g("color","TR"), "#[0-9A-F]{6}"); }
    @RepeatedTest(10) void api_key_mk_prefix()   { assertTrue(g("api_key","TR").startsWith("mk_")); }
    @RepeatedTest(10) void totp_6_digits()       { assertMatches(g("totp_code","TR"), "\\d{6}"); }
    @RepeatedTest(10) void webhook_sha256()      { assertTrue(g("webhook_signature","TR").startsWith("sha256=")); }
    @RepeatedTest(10) void txn_id_prefix()       { assertTrue(g("transaction_id","TR").startsWith("TXN")); }
    @RepeatedTest(10) void url_http()            { assertTrue(g("url","TR").startsWith("http")); }
    @RepeatedTest(10) void timestamp_numeric()   { assertMatches(g("timestamp","TR"), "\\d+"); }
    @RepeatedTest(10) void clientversion_format(){ assertMatches(g("clientversion","TR"), "\\d+\\.\\d+\\.\\d+"); }

    @RepeatedTest(10) void apppassword_no_consecutive() {
        String pw = g("apppassword","TR");
        assertMatches(pw, "\\d{6}");
        for (int i = 0; i < 5; i++)
            assertNotEquals(pw.charAt(i), pw.charAt(i+1), "Consecutive repeat at pos " + i + ": " + pw);
    }

    // ── Banking ───────────────────────────────────────────────────────────────

    @RepeatedTest(10) void bic_8_or_11() {
        int len = g("swift","TR").length();
        assertTrue(len == 8 || len == 11, "BIC must be 8 or 11, got: " + len);
    }

    @RepeatedTest(10) void sort_code_format()    { assertMatches(g("sort_code","UK"), "\\d{2}-\\d{2}-\\d{2}"); }
    @RepeatedTest(10) void bik_starts_with_04()  { assertTrue(g("bik_code","RU").startsWith("04")); }
    @RepeatedTest(10) void sepa_ref_prefix()     { assertTrue(g("sepa_ref","DE").startsWith("MOCKJ-E2E-")); }
    @RepeatedTest(10) void creditor_ref_rf()     { assertTrue(g("creditor_ref","DE").startsWith("RF")); }

    @RepeatedTest(20) void routing_number_aba() {
        String rn = g("routing_number","US");
        assertMatches(rn, "\\d{9}");
        int[] d = new int[9];
        for (int i = 0; i < 9; i++) d[i] = rn.charAt(i) - '0';
        int cs = 3*(d[0]+d[3]+d[6]) + 7*(d[1]+d[4]+d[7]) + (d[2]+d[5]+d[8]);
        assertEquals(0, cs % 10, "ABA checksum failed for: " + rn);
    }

    @RepeatedTest(10) void transaction_json_keys() {
        assertKeys(g("transaction","TR"), "ref","sender_iban","receiver_iban","amount","currency","description","channel","timestamp","status");
    }

    @RepeatedTest(10) void transaction_status_valid() {
        String t = g("transaction","TR");
        assertTrue(t.contains("COMPLETED") || t.contains("PENDING") || t.contains("FAILED"), "Invalid status in: " + t.substring(0,100));
    }

    // ── Corporate ─────────────────────────────────────────────────────────────

    @RepeatedTest(10) void company_name_has_space()  { assertTrue(g("company_name","TR").contains(" ")); }
    @RepeatedTest(10) void ru_company_guillemets()   { String n = g("company_name","RU"); assertTrue(n.contains("«") && n.contains("»"), "RU: " + n); }

    @ParameterizedTest @ValueSource(strings = {"job_title","jobtitle","occupation"})
    void job_title_not_empty(String type) { assertNoError(g(type,"TR")); }

    // ── Health ────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void blood_type_valid() {
        String bt = g("blood_type","TR");
        assertTrue(java.util.Set.of("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(bt), "Invalid blood type: " + bt);
    }

    @RepeatedTest(10) void nhs_number_format() { assertMatches(g("nhs_number","UK"), "\\d{3} \\d{3} \\d{4}"); }

    @RepeatedTest(10) void icd10_starts_uppercase() {
        String icd = g("icd10","US");
        assertTrue(Character.isUpperCase(icd.charAt(0)), "ICD-10 must start uppercase: " + icd);
    }

    @RepeatedTest(10) void height_contains_cm() { assertTrue(g("height","TR").contains("cm")); }
    @RepeatedTest(10) void weight_contains_kg() { assertTrue(g("weight","TR").contains("kg")); }
    @RepeatedTest(10) void bmi_decimal()         { assertMatches(g("bmi","TR"), "\\d+[.,]\\d+"); }
    @RepeatedTest(10) void npi_10_digits()       { assertMatches(g("npi","US"), "\\d{10}"); }
    @RepeatedTest(10) void hl7_starts_msh()      { assertTrue(g("hl7_message","US").startsWith("MSH")); }
    @RepeatedTest(10) void fhir_patient_json()   { assertKeys(g("fhir_patient","US"), "resourceType"); }
    @RepeatedTest(10) void dicom_uid_dot_start() { assertTrue(g("dicom_uid","US").startsWith("1.")); }

    // ── Commerce ──────────────────────────────────────────────────────────────

    @RepeatedTest(10) void currency_json()   { assertKeys(g("currency","TR"), "code","symbol","name","decimals"); }
    @RepeatedTest(10) void tax_rate_json()   { assertKeys(g("tax_rate","TR"), "rate","name","type"); }
    @RepeatedTest(10) void vehicle_json()    { assertKeys(g("vehicle","TR"), "make","model","year","vin","color","fuel"); }

    @RepeatedTest(20) void vin_17_chars_no_iloq() {
        String vin = g("vin","TR");
        assertEquals(17, vin.length(), "VIN must be 17 chars");
        assertFalse(vin.chars().anyMatch(c -> c=='I'||c=='O'||c=='Q'), "VIN must not contain I/O/Q: " + vin);
    }

    @ParameterizedTest @ValueSource(strings = {"TR","US","DE","FR","UK","RU"})
    void invoice_locale_prefix(String locale) {
        String inv = g("invoice_number", locale);
        boolean ok = switch (locale) {
            case "UK" -> inv.startsWith("INV/");
            case "DE" -> inv.startsWith("RE-");
            case "FR" -> inv.startsWith("FACT-");
            case "RU" -> inv.startsWith("СФ-");
            default   -> inv.startsWith("INV-");
        };
        assertTrue(ok, locale + " invoice wrong prefix: " + inv);
    }

    // ── IoT ───────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void rfid_uid_colon_hex() {
        assertMatches(g("rfid_uid","TR"), "([0-9A-F]{2}:){3,6}[0-9A-F]{2}");
    }

    @RepeatedTest(10) void epc_24_hex_starts_30() {
        String epc = g("epc","TR");
        assertEquals(24, epc.length(), "EPC must be 24 hex chars");
        assertTrue(epc.startsWith("30"), "EPC must start with 30 (SGTIN-96)");
        assertMatches(epc, "[0-9A-F]{24}");
    }

    @RepeatedTest(10) void nfc_atqa_colon_format() {
        assertMatches(g("nfc_atqa","TR"), "[0-9a-fA-F]{2}:[0-9a-fA-F]{2}");
    }

    @RepeatedTest(10) void nfc_sak_valid() {
        String sak = g("nfc_sak","TR");
        assertTrue(java.util.Set.of("00","08","18","20","60").contains(sak), "Invalid SAK: " + sak);
    }

    @RepeatedTest(10) void ndef_uri_json()      { assertKeys(g("ndef_uri","TR"), "raw_hex","decoded","tnf","type","prefix_code"); }
    @RepeatedTest(10) void ndef_text_json()     { assertKeys(g("ndef_text","TR"), "raw_hex","decoded","lang","tnf","type","encoding"); }
    @RepeatedTest(10) void apdu_json()          { assertKeys(g("apdu","TR"), "cla","ins","p1","p2","hex","description"); }
    @RepeatedTest(10) void nfc_tag_json()       { assertKeys(g("nfc_tag","TR"), "uid","atqa","sak","type","capacity_bytes"); }
    @RepeatedTest(10) void ir_nec_json()        { assertKeys(g("ir_nec","TR"), "address","command","carrier_hz","protocol"); }
    @RepeatedTest(10) void ir_nec_protocol()    { assertTrue(g("ir_nec","TR").contains("\"NEC\"")); }
    @RepeatedTest(10) void ir_rc5_json()        { assertKeys(g("ir_rc5","TR"), "system","command","carrier_hz","protocol"); }
    @RepeatedTest(10) void ir_rc5_protocol()    { assertTrue(g("ir_rc5","TR").contains("\"RC5\"")); }
    @RepeatedTest(10) void ir_pronto_header()   { assertTrue(g("ir_pronto","TR").startsWith("0000 006C")); }
    @RepeatedTest(10) void ir_raw_json()        { assertKeys(g("ir_raw","TR"), "carrier_hz","address","command","pulses","pulse_count"); }
    @RepeatedTest(10) void mqtt_payload_json()  { assertKeys(g("mqtt_payload","TR"), "device_id","timestamp","sensor_type","readings","rssi","snr","battery_pct"); }

    @RepeatedTest(10) void lora_packet_lowercase_hex() {
        assertMatches(g("lora_packet","TR"), "[0-9a-f]{2}( [0-9a-f]{2})+");
    }

    @RepeatedTest(10) void lora_packet_mhdr_40() {
        assertTrue(g("lora_packet","TR").startsWith("40 "), "LoRa MHDR must be 40 (unconfirmed uplink)");
    }

    // ── Barcode ───────────────────────────────────────────────────────────────

    @RepeatedTest(20) void ean13_13_digits()  { assertMatches(g("ean13","TR"), "\\d{13}"); }
    @RepeatedTest(10) void ean8_8_digits()    { assertMatches(g("ean8","TR"), "\\d{8}"); }
    @RepeatedTest(10) void upca_12_digits()   { assertMatches(g("upca","TR"), "\\d{12}"); }

    @RepeatedTest(10) void isbn13_prefix() {
        String isbn = g("isbn13","TR");
        assertTrue(isbn.startsWith("978") || isbn.startsWith("979"), "ISBN-13 must start with 978/979: " + isbn);
        assertEquals(13, isbn.length());
    }

    @RepeatedTest(10) void isbn10_format() {
        String isbn = g("isbn10","TR");
        assertEquals(10, isbn.length());
        assertMatches(isbn.substring(0,9), "\\d{9}");
        assertTrue(Character.isDigit(isbn.charAt(9)) || isbn.charAt(9)=='X', "ISBN-10 last char must be digit or X: " + isbn);
    }

    @RepeatedTest(10) void gs1_128_ais() {
        String gs1 = g("gs1_128","TR");
        assertTrue(gs1.startsWith("(01)"), "GS1-128 must start with (01): " + gs1);
        assertTrue(gs1.contains("(17)") && gs1.contains("(10)"), "GS1-128 must have AI(17) and AI(10): " + gs1);
    }

    // ── Telecom ───────────────────────────────────────────────────────────────

    @RepeatedTest(20) void imei_15_digits_luhn() {
        String imei = g("imei","TR");
        assertMatches(imei, "\\d{15}");
        assertTrue(luhn(imei), "IMEI Luhn failed: " + imei);
    }

    @RepeatedTest(10) void imei2_hyphen_format() { assertMatches(g("imei2","TR"), "\\d{2}-\\d{6}-\\d{6}-\\d"); }
    @RepeatedTest(10) void iccid_starts_89()     { assertTrue(g("iccid","TR").startsWith("89")); }
    @RepeatedTest(10) void imsi_15_digits()      { assertMatches(g("imsi","TR"), "\\d{15}"); }

    @ParameterizedTest @ValueSource(strings = {"TR","US","UK","DE","FR","RU"})
    void msisdn_plus_prefix(String locale) { assertTrue(g("msisdn",locale).startsWith("+")); }

    @RepeatedTest(10) void msisdn_tr_905()  { assertTrue(g("msisdn","TR").startsWith("+905")); }
    @RepeatedTest(10) void msisdn_us_1()    { assertTrue(g("msisdn","US").startsWith("+1")); }
    @RepeatedTest(10) void msisdn_uk_447()  { assertTrue(g("msisdn","UK").startsWith("+447")); }
    @RepeatedTest(10) void msisdn_de_491()  { assertTrue(g("msisdn","DE").startsWith("+491")); }
    @RepeatedTest(10) void msisdn_fr_336()  { assertTrue(g("msisdn","FR").startsWith("+336")); }
    @RepeatedTest(10) void msisdn_ru_79()   { assertTrue(g("msisdn","RU").startsWith("+79")); }

    // ── Securities ────────────────────────────────────────────────────────────

    @RepeatedTest(10) void isin_12_chars() {
        String isin = g("isin","US");
        assertEquals(12, isin.length());
        assertMatches(isin, "[A-Z]{2}[A-Z0-9]{9}\\d");
    }

    @RepeatedTest(10) void cusip_9_chars()  { assertEquals(9, g("cusip","US").length()); }
    @RepeatedTest(10) void sedol_7_chars()  { assertEquals(7, g("sedol","UK").length()); }
    @RepeatedTest(10) void lei_length()     { int len = g("lei","US").length(); assertTrue(len == 20 || len == 21, "LEI length: " + len); }
    @RepeatedTest(10) void fix_message_8eq(){ assertTrue(g("fix_message","US").startsWith("8=")); }
    @RepeatedTest(10) void psd2_consent_json(){ assertKeys(g("psd2_consent","DE"), "consentId"); }

    // ── Crypto ────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void btc_address_prefix() {
        String addr = g("btc_address","TR");
        assertTrue(addr.startsWith("1") || addr.startsWith("3") || addr.startsWith("bc1"), "BTC prefix: " + addr);
    }

    @RepeatedTest(10) void eth_address_0x42() {
        String addr = g("eth_address","TR");
        assertTrue(addr.startsWith("0x") && addr.length()==42, "ETH address format: " + addr);
    }

    @RepeatedTest(10) void tx_hash_0x64() {
        String h = g("tx_hash","TR");
        assertTrue(h.startsWith("0x") && h.length()==66, "tx_hash format: " + h);
    }

    @RepeatedTest(10) void block_hash_0x64() {
        String h = g("block_hash","TR");
        assertTrue(h.startsWith("0x") && h.length()==66, "block_hash format: " + h);
    }

    @RepeatedTest(10) void mnemonic_word_count() {
        String[] words = g("mnemonic","TR").split(" ");
        assertTrue(words.length==12 || words.length==24, "Mnemonic word count: " + words.length);
    }

    // ── Ecommerce ─────────────────────────────────────────────────────────────

    @RepeatedTest(10) void sku_format() { assertMatches(g("sku","TR"), "[A-Z]{2,4}-\\d{4,8}"); }

    @RepeatedTest(10) void order_id_format() {
        String oid = g("order_id","TR");
        assertTrue(oid.startsWith("ORD-"), "order_id prefix: " + oid);
        String suffix = oid.substring(4);
        assertTrue(suffix.length()>=8 && suffix.length()<=12, "order_id suffix length: " + suffix.length());
        assertMatches(suffix, "[A-Z0-9]+");
    }

    @RepeatedTest(10) void usps_tracking_22_digits() {
        String tn = g("tracking_number","US");
        assertEquals(22, tn.length(), "USPS tracking must be 22 chars: " + tn);
        assertMatches(tn, "\\d{22}");
    }

    @RepeatedTest(10) void dhl_tracking_jd11() {
        String dhl = g("dhl_tracking","TR");
        assertTrue(dhl.startsWith("JD") && dhl.length()==11, "DHL tracking: " + dhl);
    }

    @RepeatedTest(20) void rating_half_steps() {
        double r = Double.parseDouble(g("rating","TR"));
        assertTrue(r >= 1.0 && r <= 5.0, "Rating must be 1.0-5.0: " + r);
        assertEquals(0.0, (r * 2) - Math.round(r * 2), 0.001, "Rating must be 0.5 step: " + r);
    }

    // ── Location ──────────────────────────────────────────────────────────────

    @RepeatedTest(10) void latitude_global_range() {
        double lat = Double.parseDouble(g("latitude","TR"));
        assertTrue(lat >= -90 && lat <= 90);
    }

    @RepeatedTest(10) void latitude_tr_range() {
        double lat = Double.parseDouble(g("latitude","TR"));
        assertTrue(lat >= 36.0 && lat <= 42.0, "TR latitude must be 36-42: " + lat);
    }

    @RepeatedTest(10) void longitude_global_range() {
        double lon = Double.parseDouble(g("longitude","TR"));
        assertTrue(lon >= -180 && lon <= 180);
    }

    @RepeatedTest(10) void timezone_iana_slash() { assertTrue(g("timezone","TR").contains("/"), "Timezone must be IANA"); }
    @RepeatedTest(10) void country_code_2upper() { assertMatches(g("country_code","TR"), "[A-Z]{2}"); }

    @RepeatedTest(10) void coordinates_two_parts() {
        String coord = g("coordinates","TR");
        assertEquals(2, coord.split(",").length, "Coordinates must have 2 parts: " + coord);
    }

    // ── Social ────────────────────────────────────────────────────────────────

    @RepeatedTest(20) void username_4_to_15() {
        String u = g("username","TR");
        assertTrue(u.length()>=4 && u.length()<=15, "Username length: " + u.length() + " [" + u + "]");
        assertFalse(u.contains(" "), "Username must not contain spaces");
    }

    @RepeatedTest(10) void handle_at_prefix()    { assertTrue(g("handle","TR").startsWith("@")); }
    @RepeatedTest(10) void hashtag_hash_prefix() { assertTrue(g("hashtag","TR").startsWith("#")); }

    @RepeatedTest(10) void follower_count_range() {
        int fc = Integer.parseInt(g("follower_count","TR"));
        assertTrue(fc >= 0 && fc < 5_000_000, "Follower count: " + fc);
    }

    // ── Hardware ──────────────────────────────────────────────────────────────

    @RepeatedTest(10) void track1_format() {
        String t = g("track1_data","TR");
        assertTrue(t.startsWith("%B") && t.endsWith("?") && t.contains("^"), "Track1 format: " + t);
    }

    @RepeatedTest(10) void track1_mockj_name() {
        assertTrue(g("track1_data","TR").contains("MOCKJ"), "Track1 must contain MOCKJ name");
    }

    @RepeatedTest(10) void track2_format() {
        String t = g("track2_data","TR");
        assertTrue(t.startsWith(";") && t.endsWith("?") && t.contains("="), "Track2 format: " + t);
    }

    @RepeatedTest(10) void chip_data_tags() {
        String cd = g("chip_data","TR");
        assertTrue(cd.startsWith("9F02"), "chip_data must start with 9F02");
        assertTrue(cd.contains("5F2A"), "chip_data must have 5F2A (currency)");
        assertTrue(cd.contains("9A03"), "chip_data must have 9A03 (date)");
        assertTrue(cd.contains("9C01"), "chip_data must have 9C01 (tx type)");
    }

    // ── CardPhysics ───────────────────────────────────────────────────────────

    @RepeatedTest(10) void emv_arqc_16hex()       { assertMatches(g("emv_arqc","TR"), "[0-9A-F]{16}"); }
    @RepeatedTest(10) void emv_iad_0a_22chars()   { String iad = g("emv_iad","TR"); assertTrue(iad.startsWith("0A") && iad.length()==22); }
    @RepeatedTest(10) void iso8583_auth_bitmap()  { assertTrue(g("iso8583_auth_request","TR").contains("723C448008C08000")); }
    @RepeatedTest(10) void iso8583_resp_bitmap()  { assertTrue(g("iso8583_auth_response","TR").contains("7238000006C00000")); }
    @RepeatedTest(10) void iso8583_rev_bitmap()   { assertTrue(g("iso8583_reversal","TR").contains("7238000008C08100")); }
    @RepeatedTest(10) void atm_session_json()     { assertKeys(g("atm_session","TR"), "session_id"); }
    @RepeatedTest(10) void pos_receipt_not_empty(){ assertNoError(g("pos_receipt","TR")); }

    // ── ISO 8583 field-level assertions (standard: ISO 8583-1:1993) ───────────

    @ParameterizedTest @ValueSource(strings = {"TR","US","DE","UK","RU"})
    void iso8583_de049_exactly_3_digits(String locale) {
        // DE049 is n3 per ISO 8583 — must be exactly 3 numeric digits, no leading zero padding to 4
        String msg = g("iso8583_auth_request", locale);
        String de049 = extractJsonField(msg, "de049");
        assertMatches(de049, "\\d{3}", "DE049 must be n3 (exactly 3 digits) for locale " + locale + ", got: " + de049);
    }

    @ParameterizedTest @ValueSource(strings = {"TR","US","DE","UK","RU"})
    void iso8583_reversal_de049_exactly_3_digits(String locale) {
        String msg = g("iso8583_reversal", locale);
        String de049 = extractJsonField(msg, "de049");
        assertMatches(de049, "\\d{3}", "Reversal DE049 must be n3 for locale " + locale + ", got: " + de049);
    }

    @ParameterizedTest @ValueSource(strings = {"TR","DE","US"})
    void iso8583_de049_correct_value(String locale) {
        String msg = g("iso8583_auth_request", locale);
        String de049 = extractJsonField(msg, "de049");
        String expected = switch (locale) { case "DE" -> "978"; case "US" -> "840"; default -> "949"; };
        assertEquals(expected, de049, "DE049 wrong for locale " + locale);
    }

    @RepeatedTest(50) void iso8583_de022_entry_mode_varies() {
        // DE022 must not be hardcoded — must vary across samples
        java.util.Set<String> modes = new java.util.HashSet<>();
        for (int i = 0; i < 50; i++) modes.add(extractJsonField(g("iso8583_auth_request","TR"), "de022"));
        assertTrue(modes.size() > 1, "DE022 entry mode never varies (hardcoded): " + modes);
    }

    @RepeatedTest(50) void iso8583_de018_mcc_varies() {
        java.util.Set<String> mccs = new java.util.HashSet<>();
        for (int i = 0; i < 50; i++) mccs.add(extractJsonField(g("iso8583_auth_request","TR"), "de018"));
        assertTrue(mccs.size() > 1, "DE018 MCC never varies (hardcoded): " + mccs);
    }

    @RepeatedTest(10) void iso8583_auth_response_has_all_des() {
        // MTI 0110 bitmap declares DE 2,3,4,7,11,12,13,38,39,41,42 — all must be present
        String resp = g("iso8583_auth_response","TR");
        for (String de : new String[]{"de002","de003","de004","de007","de011","de012","de013","de038","de039","de041","de042"})
            assertTrue(resp.contains("\"" + de + "\""), "Auth response missing field: " + de + " in: " + resp.substring(0, Math.min(200,resp.length())));
    }

    @RepeatedTest(10) void iso8583_auth_request_has_all_des() {
        String req = g("iso8583_auth_request","TR");
        for (String de : new String[]{"de002","de003","de004","de007","de011","de012","de013","de014","de018","de022","de025","de037","de041","de042","de049"})
            assertTrue(req.contains("\"" + de + "\""), "Auth request missing field: " + de);
    }

    private static String extractJsonField(String json, String key) {
        // Extract value for "key":"value" (string fields, quoted)
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start == -1) return "";
        start += marker.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? "" : json.substring(start, end);
    }

    private static double extractNumericField(String json, String key) {
        // Extract value for "key":numeric (unquoted numeric fields)
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\"" + java.util.regex.Pattern.quote(key) + "\":\\s*([\\d.]+)")
            .matcher(json);
        if (!m.find()) return Double.NaN;
        return Double.parseDouble(m.group(1));
    }

    private static void assertMatches(String val, String regex, String msg) {
        assertTrue(val.matches(regex), msg);
    }

    // ── Security ──────────────────────────────────────────────────────────────

    @RepeatedTest(10) void cef_log_prefix()  { assertTrue(g("cef_log","TR").startsWith("CEF:")); }
    @RepeatedTest(10) void x509_pem_format() { assertTrue(g("x509_cert","TR").startsWith("-----BEGIN CERTIFICATE-----")); }
    @RepeatedTest(10) void pcap_hex_chars()  { assertMatches(g("pcap_hex","TR"), "[0-9A-Fa-f]+"); }

    // ── Aviation ──────────────────────────────────────────────────────────────

    @RepeatedTest(10) void imo_imo_prefix() { assertTrue(g("imo_number","TR").startsWith("IMO")); }
    @RepeatedTest(10) void pnr_6_alphanum() { assertMatches(g("pnr_code","TR"), "[A-Z0-9]{6}"); }
    @RepeatedTest(10) void iata_ticket_not_empty() { assertNoError(g("iata_ticket","TR")); }

    // ── FIDO2 ─────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void webauthn_credential_structure() {
        String wc = g("webauthn_credential","TR");
        assertKeys(wc, "id","rawId","type","response","clientExtensionResults");
        assertTrue(wc.contains("public-key") && wc.contains("clientDataJSON") && wc.contains("attestationObject"));
    }

    @RepeatedTest(10) void fido2_assertion_structure() {
        String fa = g("fido2_assertion","TR");
        assertKeys(fa, "id","rawId","type","response","clientExtensionResults");
        assertTrue(fa.contains("authenticatorData") && fa.contains("signature") && fa.contains("userHandle"));
    }

    // ── Wallet ────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void eth_wallet_json()  { assertKeys(g("eth_wallet","TR"), "address","network","type"); assertTrue(g("eth_wallet","TR").contains("ethereum")); }
    @RepeatedTest(10) void btc_wallet_json()  { assertKeys(g("btc_wallet","TR"), "address","network","type"); assertTrue(g("btc_wallet","TR").contains("bitcoin")); }
    @RepeatedTest(10) void sol_wallet_json()  { assertKeys(g("sol_wallet","TR"), "address","network","type"); assertTrue(g("sol_wallet","TR").contains("solana")); }

    // ── AI Vector ─────────────────────────────────────────────────────────────

    @RepeatedTest(10) void ai_embedding_array() {
        String emb = g("ai_embedding","TR");
        assertTrue(emb.startsWith("[") && emb.endsWith("]"), "ai_embedding must be JSON array");
    }

    @RepeatedTest(10) void ai_sparse_vector_json() { assertKeys(g("ai_sparse_vector","TR"), "indices"); }
    @RepeatedTest(10) void ai_vector_not_empty()   { assertNoError(g("ai_vector","TR")); }

    // ── OIDC ──────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void oidc_token_set_json() { assertKeys(g("oidc_token_set","TR"), "access_token","refresh_token","id_token"); }
    @RepeatedTest(10) void jwks_keys_field()     { assertKeys(g("jwks","TR"), "keys"); }
    @RepeatedTest(10) void oidc_token_jwt()      { assertEquals(2, g("oidc_token","TR").chars().filter(c->c=='.').count()); }

    // ── Bank Statement ────────────────────────────────────────────────────────

    @RepeatedTest(10) void mt940_20_tag()       { assertTrue(g("mt940","TR").startsWith(":20:")); }
    @RepeatedTest(10) void camt053_xml()        { assertTrue(g("camt053","TR").startsWith("<?xml")); }

    // ── EDI ───────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void edi_850_isa()       { assertTrue(g("edi_850","US").startsWith("ISA")); }
    @RepeatedTest(10) void edifact_unb()       { assertTrue(g("edifact_orders","DE").startsWith("UNB+")); }

    // ── Event Sourcing ────────────────────────────────────────────────────────

    @RepeatedTest(10) void event_stream_json() { assertKeys(g("event_stream","TR"), "eventId"); }
    @RepeatedTest(10) void cdc_event_json()    { assertKeys(g("cdc_event","TR"), "op"); }

    // ── Telemetry ─────────────────────────────────────────────────────────────

    @RepeatedTest(10) void fdr_record_json()      { assertKeys(g("fdr_record","TR"), "altitude_ft","airspeed_kts"); }
    @RepeatedTest(10) void drone_telemetry_json() { assertNoError(g("drone_telemetry","TR")); assertTrue(g("drone_telemetry","TR").startsWith("{")); }

    // ── Crypto Fuzz ───────────────────────────────────────────────────────────

    @RepeatedTest(10) void jwt_attack_json()  { assertKeys(g("jwt_attack","TR"), "attack","token"); }
    @RepeatedTest(10) void asn1_fuzz_json()   { assertKeys(g("asn1_fuzz","TR"), "hex"); }

    // ── MRZ ───────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void mrz_td3_two_44_lines() {
        String[] lines = g("mrz_td3","TR").split("\n");
        assertEquals(2, lines.length);
        assertEquals(44, lines[0].length());
        assertEquals(44, lines[1].length());
    }

    @RepeatedTest(10) void mrz_td1_three_30_lines() {
        String[] lines = g("mrz_td1","TR").split("\n");
        assertEquals(3, lines.length);
        for (String line : lines) assertEquals(30, line.length(), "TD1 line: " + line);
    }

    // ── OHLCV ─────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void ohlcv_candles_json() { assertKeys(g("ohlcv_candles","TR"), "open","close","high","low"); }
    @RepeatedTest(10) void market_tick_json()   { assertKeys(g("market_tick","TR"), "symbol","price"); }

    // ── NMEA ──────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void nmea_gpgga_format() {
        String s = g("nmea_gpgga","TR");
        assertTrue(s.startsWith("$GPGGA,") && s.contains("*"), "GPGGA format: " + s);
        assertMatches(s.substring(s.lastIndexOf('*')+1), "[0-9A-F]{2}");
    }

    @RepeatedTest(10) void nmea_gprmc_format() {
        String s = g("nmea_gprmc","TR");
        assertTrue(s.startsWith("$GPRMC,") && s.contains("*"), "GPRMC format: " + s);
    }

    // ── Prometheus ────────────────────────────────────────────────────────────

    @RepeatedTest(10) void prometheus_has_help_or_type() {
        String pm = g("prometheus_metrics","TR");
        assertTrue(pm.contains("# HELP") || pm.contains("# TYPE"), "Prometheus must have #HELP or #TYPE");
    }

    @RepeatedTest(10) void openmetrics_not_empty() { assertNoError(g("openmetrics_snapshot","TR")); }

    // ── GameDev ───────────────────────────────────────────────────────────────

    @RepeatedTest(10) void quaternion_xyzw()   { assertKeys(g("quaternion","TR"), "x","y","z","w"); }
    @RepeatedTest(10) void navmesh_path_json() { assertKeys(g("navmesh_path","TR"), "nodes"); }

    // ── UBL ───────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void ubl_invoice_xml() { assertTrue(g("ubl_invoice","TR").startsWith("<?xml")); }
    @RepeatedTest(10) void xmldsig_is_xml()  {
        String x = g("xmldsig","TR");
        assertTrue(x.startsWith("<?xml") || x.startsWith("<"), "xmldsig must be XML");
    }

    // ── Automotive ────────────────────────────────────────────────────────────

    @RepeatedTest(10) void can_frame_json()    { assertKeys(g("can_frame","TR"), "id","dlc","data"); }
    @RepeatedTest(10) void obd2_response_json(){ assertKeys(g("obd2_response","TR"), "pid","name","value"); }

    // ── TLE ───────────────────────────────────────────────────────────────────

    @RepeatedTest(10) void tle_three_lines() {
        String[] lines = g("tle_satellite","TR").split("\n");
        assertEquals(3, lines.length, "TLE must have 3 lines");
        assertTrue(lines[1].startsWith("1 "), "TLE line 1 must start with '1 '");
        assertTrue(lines[2].startsWith("2 "), "TLE line 2 must start with '2 '");
    }

    // ── Payments ──────────────────────────────────────────────────────────────

    @RepeatedTest(10) void swift_mt103_tag20()  { assertTrue(g("swift_mt103","TR").contains("{20:"), "MT103 must contain {20:"); }
    @RepeatedTest(10) void pain001_xml()        { assertTrue(g("pain001","TR").startsWith("<?xml")); }
    @RepeatedTest(10) void nacha_ach_not_empty(){ assertNoError(g("nacha_ach","US")); }
    @RepeatedTest(10) void sepa_mandate_json()  { assertKeys(g("sepa_mandate","DE"), "mandateId"); }
    @RepeatedTest(10) void fedwire_not_empty()  { assertNoError(g("fedwire","US")); }

    // ── Communication ─────────────────────────────────────────────────────────

    @RepeatedTest(10) void phone_tr_plus90()    { assertTrue(g("phone","TR").startsWith("+90")); }
    @RepeatedTest(10) void phone_us_plus1()     { assertTrue(g("phone","US").startsWith("+1")); }
    @RepeatedTest(10) void email_at_dot()       { String e = g("email","TR"); assertTrue(e.contains("@") && e.contains(".")); }
    @RepeatedTest(10) void postalcode_tr_5dig() { assertMatches(g("postalcode","TR"), "\\d{5}"); }

    @ParameterizedTest @ValueSource(strings = {"address_city","address_street","address_full"})
    void address_fields_not_empty(String type) { assertNoError(g(type,"TR")); }

    // ── Special ───────────────────────────────────────────────────────────────

    @RepeatedTest(10) void cardowner_uppercase()   { String co = g("cardowner","TR"); assertEquals(co, co.toUpperCase()); }
    @RepeatedTest(10) void regex_string_not_empty(){ assertNoError(g("regex_string","TR")); }

    // ── Identity (additional / alias types) ───────────────────────────────────

    @RepeatedTest(10) void ykn_starts_9()           { String y = g("ykn","TR"); assertMatches(y, "\\d{11}"); assertEquals('9', y.charAt(0)); }
    @RepeatedTest(10) void nationalid_not_empty()   { assertNoError(g("nationalid","TR")); }
    @RepeatedTest(10) void taxid_10_digits()        { assertMatches(g("taxid","TR"), "\\d{10}"); }
    @RepeatedTest(10) void inn_10_digits()          { assertMatches(g("inn","RU"), "\\d{10}"); }
    @RepeatedTest(10) void inn_individual_12()      { assertMatches(g("inn_individual","RU"), "\\d{12}"); }
    @RepeatedTest(10) void snils_format()           { assertMatches(g("snils","RU"), "\\d{3}-\\d{3}-\\d{3} \\d{2}"); }
    @RepeatedTest(10) void ogrn_13_digits()         { assertMatches(g("ogrn","RU"), "\\d{13}"); }
    @RepeatedTest(10) void kpp_9_digits()           { assertMatches(g("kpp","RU"), "\\d{9}"); }
    @RepeatedTest(10) void rvn_de_format()          { assertMatches(g("rvn","DE"), "\\d{8}[A-Z]\\d{2}"); }
    @RepeatedTest(10) void crn_uk_not_empty()       { assertNoError(g("crn","UK")); }
    @RepeatedTest(10) void hrb_de_prefix()          { assertTrue(g("hrb","DE").startsWith("HRB ")); }
    @RepeatedTest(10) void paye_uk_format()         { assertMatches(g("paye","UK"), "\\d{3}/[A-Z]\\d{6}"); }
    @RepeatedTest(10) void utr_uk_10_digits()       { assertMatches(g("utr","UK"), "\\d{10}"); }
    @RepeatedTest(10) void tva_fr_prefix()          { assertTrue(g("tva","FR").startsWith("FR")); }
    @RepeatedTest(10) void siren_9_digits()         { assertMatches(g("siren","FR"), "\\d{9}"); }
    @RepeatedTest(10) void siret_14_digits()        { assertMatches(g("siret","FR"), "\\d{14}"); }
    @RepeatedTest(10) void patronymic_ru_not_empty(){ assertNoError(g("patronymic","RU")); }

    @ParameterizedTest @ValueSource(strings = {"ust_id","ustid"})
    void ustid_de_format(String type)               { assertTrue(g(type,"DE").startsWith("DE")); assertMatches(g(type,"DE"), "DE\\d{9}"); }

    @ParameterizedTest @ValueSource(strings = {"vat_number","employer_id","insurance_id"})
    void identity_structured_not_empty(String type) { assertNoError(g(type,"TR")); }

    // ── Financial (additional / alias types) ─────────────────────────────────

    @RepeatedTest(10) void bic_8_or_11_chars()      { int len = g("bic","TR").length(); assertTrue(len==8||len==11, "BIC len: "+len); }

    @RepeatedTest(10) void expiryyear_range() {
        int y = Integer.parseInt(g("expiryyear","TR"));
        assertTrue(y >= 2026 && y <= 2033, "expiryyear out of range: " + y);
    }

    @RepeatedTest(10) void cardnetwork_valid() {
        assertTrue(java.util.Set.of("Visa","Mastercard","AmericanExpress","Discover","Troy")
            .contains(g("cardnetwork","TR")), "Invalid cardnetwork");
    }

    @RepeatedTest(10) void cardcategory_valid() {
        assertTrue(java.util.Set.of("classic","gold","platinum","business","infinite")
            .contains(g("cardcategory","TR")), "Invalid cardcategory");
    }

    @RepeatedTest(10) void issuer_not_empty()       { assertNoError(g("issuer","TR")); }

    // ── Meta (additional / alias types) ──────────────────────────────────────

    @RepeatedTest(10) void timestamp_iso_has_t_and_z() {
        String t = g("timestamp_iso","TR");
        assertTrue(t.contains("T") && (t.endsWith("Z") || t.contains("+")), "ISO timestamp: " + t);
    }

    @RepeatedTest(10) void browser_name_valid() {
        assertTrue(java.util.Set.of("Chrome","Firefox","Safari","Edge","Opera")
            .contains(g("browser_name","TR")));
    }

    @RepeatedTest(10) void browser_version_dotted() { assertMatches(g("browser_version","TR"), "\\d+\\.\\d+\\.\\d+\\.\\d+"); }

    @RepeatedTest(10) void browser_engine_valid() {
        assertTrue(java.util.Set.of("Blink","Gecko","WebKit")
            .contains(g("browser_engine","TR")));
    }

    @RepeatedTest(10) void signature_base64() {
        String sig = g("signature","TR");
        assertMatches(sig, "[A-Za-z0-9+/=]+");
        assertTrue(sig.length() >= 86, "signature length: " + sig.length());
    }

    @RepeatedTest(10) void domain_has_dot()         { assertTrue(g("domain","TR").contains(".")); }

    // ── Communication (additional types) ─────────────────────────────────────

    @RepeatedTest(10) void phone_area_digits()      { assertMatches(g("phone_area","TR"), "\\d+"); }
    @RepeatedTest(10) void phone_country_plus()     { assertTrue(g("phone_country","TR").startsWith("+")); }
    @RepeatedTest(10) void phone_local_7_digits()   { assertMatches(g("phone_local","TR"), "\\d{7}"); }
    @RepeatedTest(10) void plate_not_empty()        { assertNoError(g("plate","TR")); }

    // ── Banking (additional types) ────────────────────────────────────────────

    @RepeatedTest(10) void bank_name_not_empty()    { assertNoError(g("bank_name","TR")); }

    // ── Health (alias types) ──────────────────────────────────────────────────

    @RepeatedTest(10) void bloodtype_alias() {
        assertTrue(java.util.Set.of("A+","A-","B+","B-","AB+","AB-","O+","O-")
            .contains(g("bloodtype","TR")), "Invalid bloodtype");
    }

    @RepeatedTest(10) void nhsnumber_alias()        { assertMatches(g("nhsnumber","UK"), "\\d{3} \\d{3} \\d{4}"); }

    // ── Commerce (alias types) ────────────────────────────────────────────────

    @RepeatedTest(10) void taxrate_alias()          { assertKeys(g("taxrate","TR"), "rate","name","type"); }

    @ParameterizedTest @ValueSource(strings = {"TR","US","DE","FR","UK","RU"})
    void invoicenumber_alias(String locale) {
        String inv = g("invoicenumber", locale);
        boolean ok = switch (locale) {
            case "UK" -> inv.startsWith("INV/");
            case "DE" -> inv.startsWith("RE-");
            case "FR" -> inv.startsWith("FACT-");
            case "RU" -> inv.startsWith("СФ-");
            default   -> inv.startsWith("INV-");
        };
        assertTrue(ok, locale + " invoicenumber wrong prefix: " + inv);
    }

    // ── IoT (alias / additional types) ────────────────────────────────────────

    @RepeatedTest(10) void rfid_tag_alias()         { assertMatches(g("rfid_tag","TR"), "([0-9A-F]{2}:){3,6}[0-9A-F]{2}"); }
    @RepeatedTest(10) void nfc_uid_format()         { assertMatches(g("nfc_uid","TR"), "([0-9A-F]{2}:){3,6}[0-9A-F]{2}"); }

    // ── Ecommerce (additional types) ──────────────────────────────────────────

    @RepeatedTest(10) void product_name_not_empty() { assertNoError(g("product_name","TR")); }
    @RepeatedTest(10) void category_not_empty()     { assertNoError(g("category","TR")); }

    // ── Social (additional types) ─────────────────────────────────────────────

    @RepeatedTest(10) void bio_not_empty()          { assertNoError(g("bio","TR")); }

    // ── CardPhysics (additional types) ────────────────────────────────────────

    @RepeatedTest(10) void emv_atc_4_hex()          { assertMatches(g("emv_atc","TR"), "[0-9A-F]{4}"); }

    @ParameterizedTest @ValueSource(strings = {"emv_qr_p2p","emv_qr_atm","emv_qr_pos"})
    void emv_qr_format(String type) {
        String q = g(type,"TR");
        assertTrue(q.startsWith("000201"), "EMV QR must start with 000201: " + q.substring(0, Math.min(20, q.length())));
    }

    @RepeatedTest(10) void pin_block_fmt0_fvt() {
        String pb = g("pin_block","TR");
        assertEquals(16, pb.length());
        assertMatches(pb, "[0-9A-F]{16}");
        assertEquals('0', pb.charAt(0));
    }

    @RepeatedTest(10) void pin_block_fmt3_fvt() {
        String pb = g("pin_block_fmt3","TR");
        assertEquals(16, pb.length());
        assertMatches(pb, "[0-9A-F]{16}");
        assertEquals('3', pb.charAt(0));
    }

    // ── Crypto (additional types) ─────────────────────────────────────────────

    @RepeatedTest(10) void crypto_address_btc_or_eth() {
        String a = g("crypto_address","TR");
        boolean btc = a.startsWith("1") || a.startsWith("3") || a.startsWith("bc1");
        boolean eth = a.startsWith("0x") && a.length() == 42;
        assertTrue(btc || eth, "crypto_address format invalid: " + a);
    }

    // ── Bug-fix regression assertions ────────────────────────────────────────

    /** FR INSEE nationalid month must be 01-12 (never 13-20). */
    @RepeatedTest(100) void fr_nationalid_month_01_to_12() {
        String val = g("nationalid", "FR");
        assertTrue(val.matches("\\d{15}"), "FR nationalid must be 15 digits: " + val);
        int month = Integer.parseInt(val.substring(3, 5));
        assertTrue(month >= 1 && month <= 12,
            "FR nationalid month must be 01-12, got " + month + ": " + val);
    }

    /** FR insurance_id month must be 01-12. */
    @RepeatedTest(100) void fr_insurance_id_month_01_to_12() {
        String val = g("insurance_id", "FR");
        assertTrue(val.matches("\\d{15}"), "FR insurance_id must be 15 digits: " + val);
        int month = Integer.parseInt(val.substring(3, 5));
        assertTrue(month >= 1 && month <= 12,
            "FR insurance_id month must be 01-12, got " + month + ": " + val);
    }

    /** SEPA QR BIC bank code (4 letters) must have independent random chars, not AAAA/ZZZZ. */
    @RepeatedTest(1) void sepa_qr_bic_has_varied_bank_code() {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < 50; i++) {
            String qr = g("sepa_qr", "DE");
            String bic = qr.split("\n")[4];
            seen.add(bic.substring(0, 4));
        }
        assertTrue(seen.size() > 5,
            "SEPA QR BIC bank codes suspiciously uniform (may be same letter ×4): " + seen);
    }

    /** Market tick price must be STRICTLY greater than bid (not equal). */
    @RepeatedTest(200) void market_tick_price_strictly_gt_bid() {
        String json = g("market_tick", "TR");
        double bid   = extractNumericField(json, "bid");
        double price = extractNumericField(json, "price");
        assertTrue(price > bid, "price " + price + " must be strictly > bid " + bid);
    }

    /** Event stream events must contain aggregate_id and aggregate_type. */
    @RepeatedTest(10) void event_stream_has_aggregate_id() {
        String stream = g("event_stream", "TR");
        assertTrue(stream.contains("\"aggregate_id\""),
            "event_stream missing aggregate_id field");
        assertTrue(stream.contains("\"aggregate_type\""),
            "event_stream missing aggregate_type field");
        assertTrue(stream.contains("\"User\""),
            "event_stream aggregate_type must be 'User'");
    }

    /** MT940 :61: booking date must be MMDD (4 digits after YYMMDD value date). */
    @RepeatedTest(20) void mt940_61_booking_date_is_mmdd() {
        String stmt = g("mt940", "TR");
        for (String line : stmt.split("\n")) {
            if (line.startsWith(":61:")) {
                String body = line.substring(4);
                // value date (6) + booking date (4) + C/D (1) = first 11 chars
                assertTrue(body.matches("\\d{6}\\d{4}[CD].*"),
                    ":61: booking date must be MMDD after YYMMDD: " + line);
                int mm = Integer.parseInt(body.substring(6, 8));
                int dd = Integer.parseInt(body.substring(8, 10));
                assertTrue(mm >= 1 && mm <= 12, ":61: booking month " + mm + " invalid: " + line);
                assertTrue(dd >= 1 && dd <= 31, ":61: booking day " + dd + " invalid: " + line);
            }
        }
    }

    /** RC-5 command must be 0-127 and extended commands (64-127) must appear. */
    @RepeatedTest(1) void ir_rc5_command_covers_extended_range() {
        boolean sawHigh = false;
        for (int i = 0; i < 200; i++) {
            String json = g("ir_rc5", "TR");
            double cmd = extractNumericField(json, "command");
            assertTrue(cmd >= 0 && cmd <= 127, "RC-5 command out of 0-127: " + cmd);
            if (cmd >= 64) sawHigh = true;
        }
        assertTrue(sawHigh, "RC-5 extended commands (64-127) never appeared in 200 samples");
    }

    /** XMLDSig X.509 cert must start with ASN.1 DER SEQUENCE bytes 0x30 0x82. */
    @RepeatedTest(5) void xmldsig_x509_der_sequence_header() throws Exception {
        String json = g("xmldsig", "TR");
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("<ds:X509Certificate>([^<]+)</ds:X509Certificate>")
            .matcher(json);
        assertTrue(m.find(), "X509Certificate tag missing");
        byte[] der = java.util.Base64.getDecoder().decode(m.group(1));
        assertEquals((byte)0x30, der[0], "DER must start with 0x30 (SEQUENCE)");
        assertEquals((byte)0x82, der[1], "DER length must use 0x82 (long-form)");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String g(String type, String locale) { return MockJutsuRegistry.generate(type, locale); }

    private static void assertMatches(String val, String regex) {
        assertTrue(val.matches(regex), "Expected '" + val + "' to match /" + regex + "/");
    }

    private static void assertNoError(String val) {
        assertFalse(val.isEmpty(), "Value must not be empty");
        assertFalse(val.startsWith("ERROR:"), "Got error: " + val);
    }

    private static void assertKeys(String json, String... keys) {
        assertNoError(json);
        assertTrue(json.startsWith("{") || json.startsWith("["),
            "Expected JSON, got: " + json.substring(0, Math.min(80, json.length())));
        for (String key : keys)
            assertTrue(json.contains("\"" + key + "\""),
                "JSON missing key '" + key + "' in: " + json.substring(0, Math.min(120, json.length())));
    }

    private static boolean luhn(String number) {
        int sum = 0; boolean alt = false;
        for (int i = number.length()-1; i >= 0; i--) {
            int d = number.charAt(i) - '0';
            if (d < 0 || d > 9) return false;
            if (alt) { d *= 2; if (d > 9) d -= 9; }
            sum += d; alt = !alt;
        }
        return sum % 10 == 0;
    }
}
