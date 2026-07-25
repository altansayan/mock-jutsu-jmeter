package com.mockjutsu.jmeter;

import com.mockjutsu.jmeter.functions.*;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performans regresyon guard — Python test_performance.py ile aynı yaklaşım.
 *
 *  Fast  → 1000 iterasyon / tip, eşik 1.5ms/çağrı  (Python max_duration=1.5s)
 *  Heavy → 10 iterasyon  / tip, eşik per-tip (kriptografik / yüksek hesaplama)
 */
class PerfMeasurement {

    private static final int    ITERATIONS       = 1000;
    private static final double MAX_MS           = ITERATIONS * 1.5; // 1500.0 ms

    private static final int    HEAVY_ITERATIONS = 10;

    // Kriptografik / yüksek hesaplama gerektiren tipler
    private static final Set<String> HEAVY_PREFIXES = Set.of(
        "eth_wallet", "btc_wallet", "sol_wallet",
        "ai_embedding", "ai_vector",
        "oidc_token_set", "jwks",
        "x509_cert", "mnemonic",
        "webauthn_credential", "fido2_assertion",
        "mt940", "camt053",
        "swift_mt103", "pain001",
        "jwt_attack", "asn1_fuzz",
        "ubl_invoice", "oidc_token",
        "fhir_patient", "hl7_message"
    );

    // ── Fast: tüm tipler içinden ağır olanlar hariç ───────────────────────────
    static Stream<Object[]> fastCases() {
        return buildCases().stream()
            .filter(row -> {
                String typeSpec = (String) row[1];
                return HEAVY_PREFIXES.stream().noneMatch(typeSpec::startsWith);
            });
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("fastCases")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void test_performance_baseline(
            Class<? extends MockJutsuBaseFunction> cls,
            String typeSpec,
            String locale) throws Exception {

        MockJutsuBaseFunction fn = cls.getDeclaredConstructor().newInstance();
        fn.setParameters(List.of(new CompoundVariable(typeSpec + "|" + locale)));

        long startNs = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) fn.execute(null, null);
        double durationMs = (System.nanoTime() - startNs) / 1_000_000.0;

        assertTrue(durationMs < MAX_MS,
            String.format("Performance Regression! '%s' took %.4fms for %d calls (limit %.1fms).",
                typeSpec, durationMs, ITERATIONS, MAX_MS));
    }

    // ── Heavy: sadece ağır tipler, per-tip eşik ───────────────────────────────
    static Stream<Object[]> heavyCases() {
        return buildCases().stream()
            .filter(row -> {
                String typeSpec = (String) row[1];
                return HEAVY_PREFIXES.stream().anyMatch(typeSpec::startsWith);
            });
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("heavyCases")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void test_performance_heavy(
            Class<? extends MockJutsuBaseFunction> cls,
            String typeSpec,
            String locale) throws Exception {

        double maxMs = heavyThresholdMs(typeSpec);

        MockJutsuBaseFunction fn = cls.getDeclaredConstructor().newInstance();
        fn.setParameters(List.of(new CompoundVariable(typeSpec + "|" + locale)));

        long startNs = System.nanoTime();
        for (int i = 0; i < HEAVY_ITERATIONS; i++) fn.execute(null, null);
        double durationMs = (System.nanoTime() - startNs) / 1_000_000.0;

        assertTrue(durationMs < maxMs,
            String.format("Performance Regression! '%s' took %.4fms for %d calls (limit %.1fms = %.1fms/call).",
                typeSpec, durationMs, HEAVY_ITERATIONS, maxMs, maxMs / HEAVY_ITERATIONS));
    }

    // Per-call threshold (ms) × HEAVY_ITERATIONS = toplam limit
    private static double heavyThresholdMs(String t) {
        if (t.startsWith("oidc_token_set"))                              return HEAVY_ITERATIONS * 200.0;
        if (t.startsWith("jwks"))                                        return HEAVY_ITERATIONS * 100.0;
        if (t.startsWith("eth_wallet") || t.startsWith("btc_wallet")
                || t.startsWith("sol_wallet"))                           return HEAVY_ITERATIONS * 100.0;
        if (t.startsWith("ubl_invoice"))                                 return HEAVY_ITERATIONS *  50.0;
        if (t.startsWith("x509_cert"))                                   return HEAVY_ITERATIONS *  50.0;
        if (t.startsWith("webauthn_credential")
                || t.startsWith("fido2_assertion"))                      return HEAVY_ITERATIONS *  50.0;
        if (t.startsWith("mt940") || t.startsWith("camt053"))            return HEAVY_ITERATIONS *  30.0;
        if (t.startsWith("mnemonic"))                                    return HEAVY_ITERATIONS *  30.0;
        if (t.startsWith("oidc_token"))                                  return HEAVY_ITERATIONS *  30.0;
        if (t.startsWith("swift_mt103") || t.startsWith("pain001"))      return HEAVY_ITERATIONS *  30.0;
        if (t.startsWith("fhir_patient") || t.startsWith("hl7_message")) return HEAVY_ITERATIONS *  30.0;
        if (t.startsWith("jwt_attack") || t.startsWith("asn1_fuzz"))     return HEAVY_ITERATIONS *  25.0;
        if (t.startsWith("ai_embedding") || t.startsWith("ai_vector"))   return HEAVY_ITERATIONS *  10.0;
        return HEAVY_ITERATIONS * 50.0;
    }

    // ── Tüm tip + qualifier kombinasyonları ───────────────────────────────────
    private static List<Object[]> buildCases() {
        List<Object[]> c = new ArrayList<>();

        // ── Identity ──────────────────────────────────────────────────────────
        Class<?> id = MockJutsuIdentityFunction.class;
        for (String t : List.of("tckn","ykn","taxid","vkn","nationalid","ssn","nin",
                "inn","inn_individual","snils","sgk","mersis","ein","utr","crn","paye",
                "ust_id","ustid","hrb","rvn","siren","siret","tva","ogrn","kpp",
                "employer_id","insurance_id","passport","license","gender",
                "birthdate","tckn_masked","ssn_masked","nationality","vat_number"))
            c.add(row(id, t, "TR"));
        for (String q : List.of("male","female"))
            for (String t : List.of("firstname","lastname","fullname","patronymic"))
                c.add(row(id, t + ":" + q, "TR"));
        c.add(row(id, "firstname", "TR")); c.add(row(id, "lastname", "TR"));
        c.add(row(id, "fullname",  "TR")); c.add(row(id, "age:18-65", "TR"));
        c.add(row(id, "age",       "TR")); c.add(row(id, "cardowner", "TR"));

        // ── Financial ─────────────────────────────────────────────────────────
        Class<?> fin = MockJutsuFinancialFunction.class;
        for (String net : List.of("visa","mc","amex","troy","jcb","discover","unionpay","mir","maestro"))
            c.add(row(fin, "cardnum:" + net, "TR"));
        c.add(row(fin, "cardnum", "TR"));
        for (String t : List.of("cardnetwork","cardtype","cardstatus","cardcategory",
                "cvv3","cvv4","issuer","expiry","expirymonth","expiryyear","pin",
                "balance","iban","credit_score","sepa_qr","emv_qr_p2p",
                "emv_qr_atm","emv_qr_pos","3ds_cavv","3ds_eci"))
            c.add(row(fin, t, "TR"));

        // ── Banking ───────────────────────────────────────────────────────────
        Class<?> bank = MockJutsuBankingFunction.class;
        for (String t : List.of("swift","bic","sort_code","routing_number","wire_routing_number",
                "bik_code","transaction","bank_name","sepa_ref","creditor_ref",
                "account_type","transaction_type","transaction_description",
                "ifsc_code","bsb_code","check_number","micr_line","payment_reference",
                "account_number","account_number_masked","micr_line_masked",
                "transaction_description_masked","check_number_masked","payment_reference_masked"))
            c.add(row(bank, t, "TR"));

        // ── Meta ──────────────────────────────────────────────────────────────
        Class<?> meta = MockJutsuMetaFunction.class;
        for (String t : List.of("uuid","requestid","correlationid","sessionid",
                "idempotencykey","deviceid","ipv4","ipv6","browser_name","browser_version",
                "browser_engine","useragent","timestamp","timestamp_iso","clientversion",
                "bearertoken","apppassword","jwt","mac_address","domain","url",
                "api_key","totp_code","webhook_signature","transaction_id",
                "public_ip","private_ip","slug","http_method","http_status_code",
                "port_number","hostname","tld","uri_path"))
            c.add(row(meta, t, "TR"));
        for (String alg : List.of("sha256","sha512","md5","sha1"))
            c.add(row(meta, "hash:" + alg, "TR"));
        c.add(row(meta, "hash",      "TR"));
        for (String fmt : List.of("hex","rgb","name"))
            c.add(row(meta, "color:" + fmt, "TR"));
        c.add(row(meta, "color",     "TR"));
        c.add(row(meta, "signature", "TR"));

        // ── Communication ─────────────────────────────────────────────────────
        Class<?> comm = MockJutsuCommFunction.class;
        for (String t : List.of("phone","phone_country","phone_area","phone_local",
                "address_city","address_street","address_full","postalcode","plate","email"))
            c.add(row(comm, t, "TR"));

        // ── Corporate ─────────────────────────────────────────────────────────
        Class<?> corp = MockJutsuCorporateFunction.class;
        for (String t : List.of("company_name","job_title","occupation","jobtitle"))
            c.add(row(corp, t, "TR"));

        // ── Health ────────────────────────────────────────────────────────────
        Class<?> health = MockJutsuHealthFunction.class;
        for (String t : List.of("blood_type","bloodtype","nhs_number","nhsnumber",
                "icd10","height","weight","npi","bmi","dicom_uid"))
            c.add(row(health, t, "TR"));
        // HEAVY
        c.add(row(health, "fhir_patient", "TR"));
        c.add(row(health, "hl7_message",  "TR"));

        // ── Commerce ──────────────────────────────────────────────────────────
        Class<?> commerce = MockJutsuCommerceFunction.class;
        for (String t : List.of("currency","tax_rate","taxrate","invoice_number",
                "invoicenumber","vin","vehicle"))
            c.add(row(commerce, t, "TR"));

        // ── IoT ───────────────────────────────────────────────────────────────
        Class<?> iot = MockJutsuIoTFunction.class;
        for (String t : List.of("rfid_uid","epc","rfid_tag","nfc_uid","nfc_atqa","nfc_sak",
                "ndef_uri","ndef_text","apdu","nfc_tag","ir_nec","ir_rc5","ir_pronto",
                "ir_raw","mqtt_payload","lora_packet"))
            c.add(row(iot, t, "TR"));

        // ── Barcode ───────────────────────────────────────────────────────────
        Class<?> barcode = MockJutsuBarcodeFunction.class;
        for (String t : List.of("ean13","ean8","upca","isbn13","isbn10","gs1_128"))
            c.add(row(barcode, t, "TR"));

        // ── Telecom ───────────────────────────────────────────────────────────
        Class<?> telecom = MockJutsuTelecomFunction.class;
        for (String t : List.of("imei","imei2","iccid","imsi","msisdn"))
            c.add(row(telecom, t, "TR"));

        // ── Markets ───────────────────────────────────────────────────────────
        Class<?> markets = MockJutsuMarketsFunction.class;
        for (String t : List.of("isin","cusip","sedol","lei","fix_message","psd2_consent",
                "figi","nsin","stock_ticker","forex_pair","forex_rate","ric","mic",
                "stock_exchange","option_contract","bond_yield","coupon_rate",
                "settlement_date","portfolio_id","portfolio_id_masked"))
            c.add(row(markets, t, "TR"));

        // ── Crypto ────────────────────────────────────────────────────────────
        Class<?> crypto = MockJutsuCryptoFunction.class;
        for (String t : List.of("tx_hash","block_hash","nft_token_id","gas_price","gas_limit",
                "defi_protocol_name","blockchain_network","wallet_label","defi_position_type",
                "cryptocurrency_name","liquidity_pool_id","liquidity_pool_id_masked"))
            c.add(row(crypto, t, "TR"));
        c.add(row(crypto, "crypto_address:eth", "TR"));
        c.add(row(crypto, "crypto_address:btc", "TR"));
        // HEAVY — mnemonic (BIP39 wordlist + entropy)
        for (String wc : List.of("12","15","18","21","24"))
            c.add(row(crypto, "mnemonic:" + wc, "TR"));
        c.add(row(crypto, "mnemonic", "TR"));

        // ── Wallet — HEAVY (EC key gen: secp256k1, ed25519) ──────────────────
        Class<?> wallet = MockJutsuWalletFunction.class;
        c.add(row(wallet, "eth_wallet", "TR"));
        c.add(row(wallet, "btc_wallet", "TR"));
        c.add(row(wallet, "sol_wallet", "TR"));

        // ── Ecommerce ─────────────────────────────────────────────────────────
        Class<?> ecom = MockJutsuEcommerceFunction.class;
        for (String t : List.of("product_name","sku","order_id","category","rating","dhl_tracking"))
            c.add(row(ecom, t, "TR"));
        for (String car : List.of("dhl","ups","fedex"))
            c.add(row(ecom, "tracking_number:" + car, "TR"));
        c.add(row(ecom, "tracking_number", "TR"));

        // ── Location ──────────────────────────────────────────────────────────
        Class<?> loc = MockJutsuLocationFunction.class;
        for (String t : List.of("latitude","longitude","timezone","country_code","coordinates"))
            c.add(row(loc, t, "TR"));

        // ── Social ────────────────────────────────────────────────────────────
        Class<?> social = MockJutsuSocialFunction.class;
        for (String t : List.of("username","hashtag","bio","handle","follower_count"))
            c.add(row(social, t, "TR"));

        // ── Hardware ──────────────────────────────────────────────────────────
        Class<?> hw = MockJutsuHardwareFunction.class;
        for (String t : List.of("track1_data","track2_data","chip_data","pin_block","pin_block_fmt3"))
            c.add(row(hw, t, "TR"));

        // ── Card Physics ──────────────────────────────────────────────────────
        Class<?> cp = MockJutsuCardPhysicsFunction.class;
        for (String t : List.of("emv_arqc","emv_atc","emv_iad","iso8583_auth_request",
                "iso8583_auth_response","iso8583_reversal","atm_session","pos_receipt"))
            c.add(row(cp, t, "TR"));

        // ── Security ──────────────────────────────────────────────────────────
        Class<?> sec = MockJutsuSecurityFunction.class;
        for (String t : List.of("cef_log","pcap_hex","password","password_hash","cve_id"))
            c.add(row(sec, t, "TR"));
        // HEAVY — RSA/EC key + self-signed cert generation
        c.add(row(sec, "x509_cert", "TR"));

        // ── FIDO2 — HEAVY (P-256 assertion + attestation) ────────────────────
        Class<?> fido2 = MockJutsuFido2Function.class;
        c.add(row(fido2, "webauthn_credential", "TR"));
        c.add(row(fido2, "fido2_assertion",     "TR"));

        // ── CryptoFuzz — HEAVY (JWT malformed + ASN.1 fuzzing) ───────────────
        Class<?> fuzz = MockJutsuCryptoFuzzFunction.class;
        c.add(row(fuzz, "jwt_attack", "TR"));
        c.add(row(fuzz, "asn1_fuzz",  "TR"));

        // ── Aviation ──────────────────────────────────────────────────────────
        Class<?> avi = MockJutsuAviationFunction.class;
        for (String t : List.of("iata_ticket","imo_number","pnr_code"))
            c.add(row(avi, t, "TR"));

        // ── AI ────────────────────────────────────────────────────────────────
        Class<?> ai = MockJutsuAiFunction.class;
        c.add(row(ai, "ai_sparse_vector", "TR"));
        // HEAVY — high-dim Gaussian sampling
        c.add(row(ai, "ai_embedding", "TR"));
        c.add(row(ai, "ai_vector",    "TR"));

        // ── OIDC — HEAVY (RSA-2048 sign, JWK set assembly) ───────────────────
        Class<?> oidc = MockJutsuOidcFunction.class;
        c.add(row(oidc, "oidc_token_set", "TR"));
        c.add(row(oidc, "jwks",           "TR"));
        c.add(row(oidc, "oidc_token",     "TR"));

        // ── Bank Statement — HEAVY (multi-entry MT940/camt.053 serialization) ─
        Class<?> bs = MockJutsuBankStatementFunction.class;
        c.add(row(bs, "mt940",   "TR"));
        c.add(row(bs, "camt053", "TR"));

        // ── EDI ───────────────────────────────────────────────────────────────
        Class<?> edi = MockJutsuEdiFunction.class;
        for (String t : List.of("edi_850","edifact_orders"))
            c.add(row(edi, t, "TR"));

        // ── Event Sourcing ────────────────────────────────────────────────────
        Class<?> es = MockJutsuEventSourcingFunction.class;
        for (String t : List.of("event_stream","cdc_event"))
            c.add(row(es, t, "TR"));

        // ── Telemetry ─────────────────────────────────────────────────────────
        Class<?> tele = MockJutsuTelemetryFunction.class;
        for (String t : List.of("fdr_record","drone_telemetry"))
            c.add(row(tele, t, "TR"));

        // ── MRZ ───────────────────────────────────────────────────────────────
        Class<?> mrz = MockJutsuMrzFunction.class;
        for (String t : List.of("mrz_td3","mrz_td1"))
            c.add(row(mrz, t, "TR"));

        // ── OHLCV ─────────────────────────────────────────────────────────────
        Class<?> ohlcv = MockJutsuOhlcvFunction.class;
        for (String t : List.of("ohlcv_candles","market_tick"))
            c.add(row(ohlcv, t, "TR"));

        // ── NMEA ──────────────────────────────────────────────────────────────
        Class<?> nmea = MockJutsuNmeaFunction.class;
        for (String t : List.of("nmea_gpgga","nmea_gprmc"))
            c.add(row(nmea, t, "TR"));

        // ── Prometheus ────────────────────────────────────────────────────────
        Class<?> prom = MockJutsuPrometheusFunction.class;
        for (String t : List.of("prometheus_metrics","openmetrics_snapshot"))
            c.add(row(prom, t, "TR"));

        // ── GameDev ───────────────────────────────────────────────────────────
        Class<?> game = MockJutsuGameDevFunction.class;
        for (String t : List.of("quaternion","navmesh_path"))
            c.add(row(game, t, "TR"));

        // ── UBL ───────────────────────────────────────────────────────────────
        Class<?> ubl = MockJutsuUblFunction.class;
        c.add(row(ubl, "xmldsig",    "TR"));
        // HEAVY — full XML invoice + XMLDSig envelope
        c.add(row(ubl, "ubl_invoice", "TR"));

        // ── Automotive ────────────────────────────────────────────────────────
        Class<?> auto = MockJutsuAutomotiveFunction.class;
        for (String t : List.of("can_frame","obd2_response"))
            c.add(row(auto, t, "TR"));

        // ── TLE ───────────────────────────────────────────────────────────────
        Class<?> tle = MockJutsuTleFunction.class;
        c.add(row(tle, "tle_satellite", "TR"));

        // ── Payments ──────────────────────────────────────────────────────────
        Class<?> pay = MockJutsuPaymentsFunction.class;
        for (String t : List.of("nacha_ach","sepa_mandate","fedwire"))
            c.add(row(pay, t, "TR"));
        // HEAVY — ISO 20022 XML serialization
        c.add(row(pay, "swift_mt103", "TR"));
        c.add(row(pay, "pain001",     "TR"));

        // ── Compliance ────────────────────────────────────────────────────────
        Class<?> comp = MockJutsuComplianceFunction.class;
        for (String t : List.of("policy_number","claim_number","pep_status","aml_risk_rating",
                "cdd_level","sar_number","ubo_ownership_percentage","kyc_document_type",
                "consent_id","tpp_id","onboarding_method","sanctions_hit",
                "policy_number_masked","claim_number_masked","sar_number_masked",
                "ubo_ownership_percentage_masked","consent_id_masked"))
            c.add(row(comp, t, "TR"));

        // ── Financial Ext ─────────────────────────────────────────────────────
        Class<?> finExt = MockJutsuFinancialExtFunction.class;
        for (String t : List.of("credit_score_model","credit_score_tier","credit_limit",
                "credit_utilization","credit_card_issuer_name","apr","loan_type",
                "mortgage_rate","mortgage_term","premium_amount","deductible",
                "coverage_limit","claim_status","credit_limit_masked",
                "mortgage_rate_masked","premium_amount_masked"))
            c.add(row(finExt, t, "TR"));

        // ── DateTime ──────────────────────────────────────────────────────────
        Class<?> dt = MockJutsuDateTimeFunction.class;
        for (String t : List.of("past_date","future_date","date_between","date_this_year",
                "date_this_month","time_only","past_datetime","future_datetime"))
            c.add(row(dt, t, "TR"));

        // ── IntlIds ───────────────────────────────────────────────────────────
        Class<?> intl = MockJutsuIntlIdsFunction.class;
        for (String t : List.of("br_cpf","br_cnpj","in_pan","in_aadhaar","in_gstin",
                "in_epic","cn_ric","mx_curp","mx_rfc","it_codicefiscale",
                "es_dni","es_nie","es_ccc","de_idnr","de_stnr","pk_cnic",
                "jp_cn","jp_in","kr_rrn","kr_brn","nl_bsn","pl_pesel",
                "se_personnummer","dk_cpr","fi_hetu","no_fodselsnummer",
                "au_abn","au_tfn","au_acn","my_nric","th_pin","th_tin",
                "sg_uen","za_idnr","ca_bn","nz_ird","ar_cuit","ar_dni",
                "cl_rut","co_nit","il_idnr","ro_cnp","ro_cui","hr_oib",
                "bg_egn","lt_asmens","ee_ik","pt_cc","eg_tn"))
            c.add(row(intl, t, "TR"));

        // ── Regex ─────────────────────────────────────────────────────────────
        Class<?> regex = MockJutsuRegexFunction.class;
        for (String pat : List.of("[A-Z]{3}-\\d{4}", "\\d{3}-\\d{2}-\\d{4}", "[a-z0-9]{8}"))
            c.add(row(regex, "reverse_regex:" + pat, "TR"));

        return c;
    }

    private static Object[] row(Class<?> cls, String typeSpec, String locale) {
        return new Object[]{cls, typeSpec, locale};
    }
}
