package com.mockjutsu.jmeter;

import com.mockjutsu.jmeter.functions.*;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Performans regresyon guard — 429 tip+qualifier kombinasyonunu gerçek
 * JMeter function interface'inden geçirerek avg ms/call ölçer.
 *
 * Her tip için per-type eşik tanımlıdır; eşik aşılırsa test FAIL eder.
 * Eşikler CI VM gecikme payıyla belirlenmiştir (ölçülen × ~100x headroom).
 *
 * Ölçüm çıktısını görmek için: mvn test -Dtest=PerfMeasurement -pl jmeter-plugin
 */
class PerfMeasurement {

    private static final List<Object[]> CASES = buildCases();

    @Test
    void measureAllTypes() throws Exception {
        // Warmup — JVM class loading + JIT
        for (Object[] row : CASES) invokeN(row, 3);

        // Gerçek ölçüm
        List<String[]> results = new ArrayList<>();
        for (Object[] row : CASES) {
            String typeSpec = (String) row[1];
            int calls = isSlow(typeSpec) ? 20 : 100;

            long ns = System.nanoTime();
            invokeN(row, calls);
            double avgMs = (System.nanoTime() - ns) / 1_000_000.0 / calls;

            results.add(new String[]{
                typeSpec,
                (String) row[2],
                ((Class<?>) row[0]).getSimpleName(),
                String.format("%.4f", avgMs),
                String.valueOf(calls)
            });
        }

        // Yavaştan hızlıya sırala
        results.sort((a, b) -> Double.compare(Double.parseDouble(b[3]), Double.parseDouble(a[3])));

        // Tabloyu yazdır
        System.out.println("\n=== MOCK JUTSU PERF MEASUREMENT RESULTS ===");
        System.out.printf("%-52s %-6s %-42s %10s %6s%n",
            "typeSpec", "locale", "function", "avg_ms", "calls");
        System.out.println("-".repeat(125));
        for (String[] r : results) {
            System.out.printf("%-52s %-6s %-42s %10s %6s%n",
                r[0], r[1], r[2], r[3], r[4]);
        }
        System.out.println("=== TOTAL CASES: " + results.size() + " ===\n");

        // Eşik kontrolü — ihlal varsa test FAIL
        List<String> violations = new ArrayList<>();
        for (String[] r : results) {
            double ms = Double.parseDouble(r[3]);
            double threshold = thresholdMs(r[0]);
            if (ms > threshold) {
                String msg = String.format("[SLOW] %-52s avg=%.4fms threshold=%.1fms", r[0], ms, threshold);
                System.out.println(msg);
                violations.add(msg);
            }
        }

        if (!violations.isEmpty()) {
            fail("Performance regression — " + violations.size() + " case(s) exceeded threshold:\n"
                + String.join("\n", violations));
        }
        System.out.println("[OK] All " + results.size() + " cases within per-type thresholds.");
    }

    // ── Per-type thresholds (CI VM headroom: ~100x observed on local JVM) ──────
    // Observed max: oidc_token_set=0.93ms, jwks=0.33ms, others<0.25ms
    private static double thresholdMs(String t) {
        if (t.startsWith("oidc_token_set"))  return 100.0;  // observed 0.93ms
        if (t.startsWith("jwks"))            return  50.0;  // observed 0.33ms
        if (t.startsWith("ubl_invoice"))     return  30.0;  // observed 0.25ms
        if (t.startsWith("eth_wallet") || t.startsWith("btc_wallet")
                                         || t.startsWith("sol_wallet")) return 25.0;
        if (t.startsWith("x509_cert"))       return  20.0;
        if (t.startsWith("webauthn")    || t.startsWith("fido2"))      return  15.0;
        if (t.startsWith("mt940")       || t.startsWith("camt053"))    return  15.0;
        if (t.startsWith("mnemonic"))   return  10.0;
        if (t.startsWith("oidc_token")) return  10.0;  // single token (not token_set)
        if (t.startsWith("swift_mt103") || t.startsWith("pain001"))    return  10.0;
        if (t.startsWith("hl7")         || t.startsWith("fhir"))       return  10.0;
        if (t.startsWith("jwt_attack")  || t.startsWith("asn1_fuzz")) return  10.0;
        if (t.startsWith("ai_embedding") || t.startsWith("ai_vector")
                                    || t.startsWith("ai_sparse_vector")) return   5.0;
        return 5.0;  // tüm diğer tipler
    }

    private static boolean isSlow(String t) {
        return t.startsWith("eth_wallet")    || t.startsWith("btc_wallet")
            || t.startsWith("sol_wallet")    || t.startsWith("oidc_token_set")
            || t.startsWith("jwks")          || t.startsWith("jwt_attack")
            || t.startsWith("webauthn")      || t.startsWith("fido2")
            || t.startsWith("mnemonic")      || t.startsWith("x509")
            || t.startsWith("oidc_token")    || t.startsWith("ubl_invoice")
            || t.startsWith("mt940")         || t.startsWith("camt053")
            || t.startsWith("swift_mt103")   || t.startsWith("pain001")
            || t.startsWith("fhir")          || t.startsWith("hl7");
    }

    // ── Invoke: tek instance, N çağrı (class instantiation overhead'i dışarıda) ──
    @SuppressWarnings({"unchecked","rawtypes"})
    private static void invokeN(Object[] row, int times) throws Exception {
        Class<? extends MockJutsuBaseFunction> cls =
            (Class<? extends MockJutsuBaseFunction>) row[0];
        String param = row[1] + "|" + row[2];
        MockJutsuBaseFunction fn = cls.getDeclaredConstructor().newInstance();
        fn.setParameters(List.of(new CompoundVariable(param)));
        for (int i = 0; i < times; i++) fn.execute(null, null);
    }

    private static Object[] row(Class<?> cls, String typeSpec, String locale) {
        return new Object[]{cls, typeSpec, locale};
    }

    // ── 429 tip+qualifier kombinasyonu ────────────────────────────────────────
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
                "icd10","height","weight","npi","bmi","hl7_message","fhir_patient","dicom_uid"))
            c.add(row(health, t, "TR"));

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
        for (String t : List.of("btc_address","eth_address","tx_hash","block_hash",
                "nft_token_id","gas_price","gas_limit","defi_protocol_name",
                "blockchain_network","wallet_label","defi_position_type",
                "cryptocurrency_name","liquidity_pool_id","liquidity_pool_id_masked"))
            c.add(row(crypto, t, "TR"));
        c.add(row(crypto, "crypto_address:eth", "TR"));
        c.add(row(crypto, "crypto_address:btc", "TR"));
        for (String wc : List.of("12","15","18","21","24"))
            c.add(row(crypto, "mnemonic:" + wc, "TR"));
        c.add(row(crypto, "mnemonic", "TR"));

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
        for (String t : List.of("cef_log","x509_cert","pcap_hex","password","password_hash","cve_id"))
            c.add(row(sec, t, "TR"));

        // ── Aviation ──────────────────────────────────────────────────────────
        Class<?> avi = MockJutsuAviationFunction.class;
        for (String t : List.of("iata_ticket","imo_number","pnr_code"))
            c.add(row(avi, t, "TR"));

        // ── FIDO2 ─────────────────────────────────────────────────────────────
        Class<?> fido = MockJutsuFido2Function.class;
        for (String t : List.of("webauthn_credential","fido2_assertion"))
            c.add(row(fido, t, "TR"));

        // ── Wallet ────────────────────────────────────────────────────────────
        Class<?> wallet = MockJutsuWalletFunction.class;
        for (String t : List.of("eth_wallet","btc_wallet","sol_wallet"))
            c.add(row(wallet, t, "TR"));

        // ── AI ────────────────────────────────────────────────────────────────
        Class<?> ai = MockJutsuAiFunction.class;
        for (String t : List.of("ai_embedding","ai_vector","ai_sparse_vector"))
            c.add(row(ai, t, "TR"));
        for (String dim : List.of("128","256","512","1536"))
            c.add(row(ai, "ai_embedding:" + dim, "TR"));

        // ── OIDC ──────────────────────────────────────────────────────────────
        Class<?> oidc = MockJutsuOidcFunction.class;
        for (String t : List.of("oidc_token_set","jwks","oidc_token"))
            c.add(row(oidc, t, "TR"));

        // ── Bank Statement ────────────────────────────────────────────────────
        Class<?> bs = MockJutsuBankStatementFunction.class;
        for (String t : List.of("mt940","camt053"))
            c.add(row(bs, t, "TR"));

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

        // ── Crypto Fuzz ───────────────────────────────────────────────────────
        Class<?> fuzz = MockJutsuCryptoFuzzFunction.class;
        for (String t : List.of("jwt_attack","asn1_fuzz"))
            c.add(row(fuzz, t, "TR"));

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
        for (String t : List.of("ubl_invoice","xmldsig"))
            c.add(row(ubl, t, "TR"));

        // ── Automotive ────────────────────────────────────────────────────────
        Class<?> auto = MockJutsuAutomotiveFunction.class;
        for (String t : List.of("can_frame","obd2_response"))
            c.add(row(auto, t, "TR"));

        // ── TLE ───────────────────────────────────────────────────────────────
        Class<?> tle = MockJutsuTleFunction.class;
        c.add(row(tle, "tle_satellite", "TR"));

        // ── Payments ──────────────────────────────────────────────────────────
        Class<?> pay = MockJutsuPaymentsFunction.class;
        for (String t : List.of("swift_mt103","pain001","nacha_ach","sepa_mandate","fedwire"))
            c.add(row(pay, t, "TR"));

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
}
