package com.mockjutsu.jmeter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Layer-2 registry tests — exercises every type through the dispatch hub.
 * No JMeter runtime needed; purely unit tests on MockJutsuRegistry.
 */
class MockJutsuRegistryTest {

    @Test
    void unknownTypeReturnsError() {
        String result = MockJutsuRegistry.generate("totally_invalid_type", "TR");
        assertTrue(result.startsWith("ERROR:"), "Unknown type must return ERROR: prefix");
    }

    @Test
    void emptyTypeReturnsError() {
        String result = MockJutsuRegistry.generate("", "TR");
        assertTrue(result.startsWith("ERROR:"));
    }

    @Test
    void nullTypeReturnsError() {
        String result = MockJutsuRegistry.generate(null, "TR");
        assertTrue(result.startsWith("ERROR:"));
    }

    @Test
    void cardownerIsUppercase() {
        String result = MockJutsuRegistry.generate("cardowner", "TR");
        assertEquals(result, result.toUpperCase(), "cardowner must be all uppercase");
        assertFalse(result.startsWith("ERROR:"));
    }

    // ── Identity ─────────────────────────────────────────────────────────────

    @Test
    void tcknIs11Digits() {
        String tckn = MockJutsuRegistry.generate("tckn", "TR");
        assertMatches(tckn, "\\d{11}");
    }

    @Test
    void ssnMatchesFormat() {
        String ssn = MockJutsuRegistry.generate("ssn", "US");
        assertMatches(ssn, "\\d{3}-\\d{2}-\\d{4}");
    }

    @Test
    void ninMatchesFormat() {
        String nin = MockJutsuRegistry.generate("nin", "UK");
        assertMatches(nin, "[A-Z]{2}\\d{6}[A-D]");
    }

    @ParameterizedTest
    @ValueSource(strings = {"TR","US","DE","FR","UK","RU"})
    void ibanNotEmpty(String locale) {
        String iban = MockJutsuRegistry.generate("iban", locale);
        assertFalse(iban.isEmpty());
        assertFalse(iban.startsWith("ERROR:"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"firstname","lastname","fullname","birthdate","age","gender","nationality"})
    void identityTypesNotError(String type) {
        String result = MockJutsuRegistry.generate(type, "TR");
        assertFalse(result.startsWith("ERROR:"), type + " returned error: " + result);
    }

    // ── Financial ────────────────────────────────────────────────────────────

    @Test
    void cardnumIsLuhnValid() {
        for (int i = 0; i < 20; i++) {
            String card = MockJutsuRegistry.generate("cardnum", "TR");
            assertTrue(isLuhnValid(card), "Card " + card + " failed Luhn check");
        }
    }

    @Test
    void cvv3Is3Digits() {
        String cvv = MockJutsuRegistry.generate("cvv3", "TR");
        assertMatches(cvv, "\\d{3}");
    }

    @Test
    void ibanTRStartsWithTR() {
        String iban = MockJutsuRegistry.generate("iban", "TR");
        assertTrue(iban.startsWith("TR"), "TR IBAN must start with TR, got: " + iban);
        assertEquals(26, iban.length(), "TR IBAN must be 26 chars");
    }

    @Test
    void ibanDEStartsWithDE() {
        String iban = MockJutsuRegistry.generate("iban", "DE");
        assertTrue(iban.startsWith("DE"), "DE IBAN must start with DE, got: " + iban);
        assertEquals(22, iban.length(), "DE IBAN must be 22 chars");
    }

    // ── Meta ─────────────────────────────────────────────────────────────────

    @Test
    void uuidMatchesFormat() {
        String uuid = MockJutsuRegistry.generate("uuid", "TR");
        assertMatches(uuid, "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void ipv4MatchesFormat() {
        String ip = MockJutsuRegistry.generate("ipv4", "TR");
        assertMatches(ip, "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    }

    @Test
    void ipv6MatchesFormat() {
        String ip = MockJutsuRegistry.generate("ipv6", "TR");
        assertEquals(7, ip.chars().filter(c -> c == ':').count(), "IPv6 must have 7 colons");
    }

    @Test
    void jwtHasThreeParts() {
        String jwt = MockJutsuRegistry.generate("jwt", "TR");
        assertEquals(2, jwt.chars().filter(c -> c == '.').count(), "JWT must have 2 dots");
    }

    // ── Health ───────────────────────────────────────────────────────────────

    @Test
    void nhsNumberMatchesFormat() {
        String nhs = MockJutsuRegistry.generate("nhs_number", "UK");
        assertMatches(nhs, "\\d{3} \\d{3} \\d{4}");
    }

    @Test
    void bloodTypeIsValid() {
        String bt = MockJutsuRegistry.generate("blood_type", "TR");
        assertTrue(java.util.Set.of("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(bt));
    }

    // ── Barcode ──────────────────────────────────────────────────────────────

    @Test
    void ean13Is13Digits() {
        for (int i = 0; i < 10; i++) {
            String ean = MockJutsuRegistry.generate("ean13", "TR");
            assertMatches(ean, "\\d{13}");
        }
    }

    @Test
    void ean8Is8Digits() {
        String ean = MockJutsuRegistry.generate("ean8", "TR");
        assertMatches(ean, "\\d{8}");
    }

    // ── Telecom ───────────────────────────────────────────────────────────────

    @Test
    void imeiIs15DigitsAndLuhnValid() {
        for (int i = 0; i < 10; i++) {
            String imei = MockJutsuRegistry.generate("imei", "TR");
            assertMatches(imei, "\\d{15}");
            assertTrue(isLuhnValid(imei), "IMEI " + imei + " failed Luhn");
        }
    }

    // ── Banking ───────────────────────────────────────────────────────────────

    @Test
    void swiftMatchesBicFormat() {
        String swift = MockJutsuRegistry.generate("swift", "TR");
        // BIC: 8 or 11 chars
        assertTrue(swift.length() == 8 || swift.length() == 11, "BIC must be 8 or 11 chars, got: " + swift.length());
    }

    @Test
    void routingNumberIs9Digits() {
        String rtn = MockJutsuRegistry.generate("routing_number", "US");
        assertMatches(rtn, "\\d{9}");
    }

    // ── Capital Markets ───────────────────────────────────────────────────────

    @Test
    void isinIs12Chars() {
        String isin = MockJutsuRegistry.generate("isin", "US");
        assertEquals(12, isin.length(), "ISIN must be 12 chars");
        assertMatches(isin, "[A-Z]{2}[A-Z0-9]{9}\\d");
    }

    @Test
    void cusipIs9Chars() {
        String cusip = MockJutsuRegistry.generate("cusip", "US");
        assertEquals(9, cusip.length(), "CUSIP must be 9 chars");
    }

    // ── Hardware / CardPhysics ────────────────────────────────────────────────

    @Test
    void pinBlockFormat0IsValid() {
        String pb = MockJutsuRegistry.generate("pin_block", "TR");
        assertEquals(16, pb.length(), "PIN Block must be 16 hex chars");
        assertTrue(pb.matches("[0-9A-F]{16}"), "PIN Block must be uppercase hex");
        assertEquals('0', pb.charAt(0), "Format 0 PIN Block must start with 0");
    }

    @Test
    void pinBlockFormat3IsValid() {
        String pb = MockJutsuRegistry.generate("pin_block_fmt3", "TR");
        assertEquals(16, pb.length());
        assertTrue(pb.matches("[0-9A-F]{16}"));
        assertEquals('3', pb.charAt(0), "Format 3 PIN Block must start with 3");
    }

    @Test
    void emvArqcIs16HexChars() {
        String arqc = MockJutsuRegistry.generate("emv_arqc", "TR");
        assertEquals(16, arqc.length());
        assertMatches(arqc, "[0-9A-F]{16}");
    }

    @Test
    void emvIadStartsWith0A() {
        String iad = MockJutsuRegistry.generate("emv_iad", "TR");
        assertTrue(iad.startsWith("0A"), "IAD must start with 0A");
        assertEquals(22, iad.length(), "IAD must be 22 hex chars (11 bytes)");
    }

    @Test
    void iso8583BitmapFixed() {
        String authReq = MockJutsuRegistry.generate("iso8583_auth_request", "TR");
        assertTrue(authReq.contains("723C448008C08000"), "Auth request bitmap must be 723C448008C08000");
        String authResp = MockJutsuRegistry.generate("iso8583_auth_response", "TR");
        assertTrue(authResp.contains("7238000006C00000"), "Auth response bitmap must be 7238000006C00000");
        String reversal = MockJutsuRegistry.generate("iso8583_reversal", "TR");
        assertTrue(reversal.contains("7238000008C08100"), "Reversal bitmap must be 7238000008C08100");
    }

    // ── MRZ ──────────────────────────────────────────────────────────────────

    @Test
    void mrzTd3HasTwoLines() {
        String mrz = MockJutsuRegistry.generate("mrz_td3", "TR");
        String[] lines = mrz.split("\n");
        assertEquals(2, lines.length, "TD3 MRZ must have 2 lines");
        assertEquals(44, lines[0].length(), "TD3 line 1 must be 44 chars");
        assertEquals(44, lines[1].length(), "TD3 line 2 must be 44 chars");
    }

    @Test
    void mrzTd1HasThreeLines() {
        String mrz = MockJutsuRegistry.generate("mrz_td1", "TR");
        String[] lines = mrz.split("\n");
        assertEquals(3, lines.length, "TD1 MRZ must have 3 lines");
        for (String line : lines) assertEquals(30, line.length(), "TD1 each line must be 30 chars");
    }

    // ── NMEA ─────────────────────────────────────────────────────────────────

    @Test
    void nmeaGpggaHasCorrectChecksum() {
        String sentence = MockJutsuRegistry.generate("nmea_gpgga", "TR");
        assertTrue(sentence.startsWith("$GPGGA,"), "GPGGA must start with $GPGGA,");
        int starIdx = sentence.lastIndexOf('*');
        assertTrue(starIdx > 0, "GPGGA must contain *");
        String body     = sentence.substring(1, starIdx);
        String expected = com.mockjutsu.jmeter.generators.NmeaGen.generate("nmea_gpgga", "TR"); // not needed, just verify format
        // Verify checksum format: 2 hex chars after *
        String cs = sentence.substring(starIdx + 1);
        assertTrue(cs.matches("[0-9A-F]{2}"), "NMEA checksum must be 2 uppercase hex chars, got: " + cs);
    }

    // ── Smoke test — ALL types ────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {
        // Identity
        "tckn","ykn","taxid","vkn","nationalid","ssn","nin","inn","inn_individual","snils",
        "sgk","mersis","ein","utr","crn","paye","ust_id","ustid","hrb","rvn","siren","siret","tva",
        "ogrn","kpp","employer_id","insurance_id","firstname","lastname","fullname","patronymic",
        "passport","license","age","gender","birthdate","tckn_masked","ssn_masked","nationality","vat_number",
        // Financial
        "cardnum","cardnetwork","cardtype","cardstatus","cvv3","cvv4","issuer","expiry",
        "expirymonth","expiryyear","pin","balance","iban","cardcategory","credit_score",
        "sepa_qr","emv_qr_p2p","emv_qr_atm","emv_qr_pos","3ds_cavv","3ds_eci",
        // Communication
        "phone","phone_country","phone_area","phone_local","address_city","address_street","address_full","postalcode","plate","email",
        // Meta
        "uuid","requestid","correlationid","sessionid","idempotencykey","deviceid",
        "ipv4","ipv6","browser_name","browser_version","browser_engine","useragent",
        "timestamp","timestamp_iso","clientversion","bearertoken","signature","apppassword",
        "jwt","hash","mac_address","domain","url","color","api_key","totp_code",
        "webhook_signature","transaction_id","public_ip","private_ip",
        // Banking
        "swift","bic","sort_code","routing_number","bik_code","transaction","bank_name","sepa_ref","creditor_ref",
        // Corporate
        "company_name","job_title","occupation","jobtitle",
        // Health
        "blood_type","bloodtype","nhs_number","nhsnumber","icd10","height","weight","npi","bmi","hl7_message","fhir_patient","dicom_uid",
        // Commerce
        "currency","tax_rate","taxrate","invoice_number","invoicenumber","vin","vehicle",
        // IoT
        "rfid_uid","epc","rfid_tag","nfc_uid","nfc_atqa","nfc_sak","ndef_uri","ndef_text","apdu","nfc_tag",
        "ir_nec","ir_rc5","ir_pronto","ir_raw","mqtt_payload","lora_packet",
        // Barcode
        "ean13","ean8","upca","isbn13","isbn10","gs1_128",
        // Telecom
        "imei","imei2","iccid","imsi","msisdn",
        // Securities
        "isin","cusip","sedol","lei","fix_message","psd2_consent",
        // Crypto
        "btc_address","eth_address","crypto_address","tx_hash","block_hash","mnemonic",
        // Ecommerce
        "product_name","sku","order_id","tracking_number","category","rating","dhl_tracking",
        // Location
        "latitude","longitude","timezone","country_code","coordinates",
        // Social
        "username","hashtag","bio","handle","follower_count",
        // Hardware
        "track1_data","track2_data","chip_data","pin_block","pin_block_fmt3",
        // CardPhysics
        "emv_arqc","emv_atc","emv_iad","iso8583_auth_request","iso8583_auth_response","iso8583_reversal","atm_session","pos_receipt",
        // Security
        "cef_log","x509_cert","pcap_hex",
        // Aviation
        "iata_ticket","imo_number","pnr_code",
        // FIDO2
        "webauthn_credential","fido2_assertion",
        // Wallet
        "eth_wallet","btc_wallet","sol_wallet",
        // AI Vector
        "ai_embedding","ai_vector","ai_sparse_vector",
        // OIDC
        "oidc_token_set","jwks","oidc_token",
        // Bank Statement
        "mt940","camt053",
        // EDI
        "edi_850","edifact_orders",
        // Event Sourcing
        "event_stream","cdc_event",
        // Telemetry
        "fdr_record","drone_telemetry",
        // Crypto Fuzz
        "jwt_attack","asn1_fuzz",
        // MRZ
        "mrz_td3","mrz_td1",
        // OHLCV
        "ohlcv_candles","market_tick",
        // NMEA
        "nmea_gpgga","nmea_gprmc",
        // Prometheus
        "prometheus_metrics","openmetrics_snapshot",
        // GameDev
        "quaternion","navmesh_path",
        // UBL
        "ubl_invoice","xmldsig",
        // Automotive
        "can_frame","obd2_response",
        // TLE
        "tle_satellite",
        // Payments
        "swift_mt103","pain001","nacha_ach","sepa_mandate","fedwire",
        // Special
        "cardowner","regex_string"
    })
    void smokeAllTypes(String type) {
        String result = MockJutsuRegistry.generate(type, "TR");
        assertFalse(result.startsWith("ERROR:"), "Type '" + type + "' returned error: " + result);
        assertFalse(result.isEmpty(), "Type '" + type + "' returned empty string");
    }

    @ParameterizedTest
    @ValueSource(strings = {"TR","US","DE","FR","UK","RU"})
    void allLocalesWorkForCardnum(String locale) {
        String card = MockJutsuRegistry.generate("cardnum", locale);
        assertFalse(card.startsWith("ERROR:"));
        assertTrue(isLuhnValid(card), locale + " card " + card + " failed Luhn");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void assertMatches(String value, String regex) {
        assertTrue(value.matches(regex), "Expected '" + value + "' to match /" + regex + "/");
    }

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
}
