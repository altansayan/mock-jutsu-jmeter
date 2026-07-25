package com.mockjutsu.jmeter;

import com.mockjutsu.jmeter.generators.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Central dispatch hub — mirrors core.py type sets and routing.
 * Each generator class maps 1:1 to the corresponding Python generator module.
 */
public final class MockJutsuRegistry {

    private MockJutsuRegistry() {}

    // ── Type sets (mirror core.py) ───────────────────────────────────────────

    private static final Set<String> IDENTITY_TYPES = Set.of(
        "tckn","ykn","taxid","vkn","nationalid","ssn","nin","inn","inn_individual",
        "snils","sgk","mersis","ein","utr","crn","paye","ust_id","ustid","hrb","rvn",
        "siren","siret","tva","ogrn","kpp","employer_id","insurance_id",
        "firstname","lastname","fullname","patronymic","passport","license",
        "age","gender","birthdate","tckn_masked","ssn_masked","nationality","vat_number"
    );

    private static final Set<String> FINANCIAL_TYPES = Set.of(
        "cardnum","cardnetwork","cardtype","cardstatus","cvv3","cvv4",
        "issuer","expiry","expirymonth","expiryyear","pin","balance",
        "iban","cardcategory","credit_score","sepa_qr","emv_qr_p2p",
        "emv_qr_atm","emv_qr_pos","3ds_cavv","3ds_eci"
    );

    private static final Set<String> COMM_TYPES = Set.of(
        "phone","phone_country","phone_area","phone_local",
        "address_city","address_street","address_full","postalcode","plate","email"
    );

    private static final Set<String> META_TYPES = Set.of(
        "uuid","requestid","correlationid","sessionid","idempotencykey",
        "deviceid","ipv4","ipv6","browser_name","browser_version","browser_engine",
        "useragent","timestamp","timestamp_iso","clientversion","bearertoken",
        "signature","apppassword","jwt","hash","mac_address","domain","url","color",
        "api_key","totp_code","webhook_signature","transaction_id","public_ip","private_ip",
        "slug","http_method","http_status_code","port_number","hostname","tld","uri_path"
    );

    private static final Set<String> BANKING_TYPES = Set.of(
        "swift","bic","sort_code","routing_number","wire_routing_number","bik_code",
        "transaction","bank_name","sepa_ref","creditor_ref",
        "account_type","transaction_type","transaction_description",
        "ifsc_code","bsb_code","check_number","micr_line",
        "payment_reference","account_number","account_number_masked",
        "micr_line_masked","transaction_description_masked","check_number_masked","payment_reference_masked"
    );

    private static final Set<String> CORPORATE_TYPES = Set.of(
        "company_name","job_title","occupation","jobtitle"
    );

    private static final Set<String> HEALTH_TYPES = Set.of(
        "blood_type","bloodtype","nhs_number","nhsnumber","icd10",
        "height","weight","npi","bmi","hl7_message","fhir_patient","dicom_uid"
    );

    private static final Set<String> COMMERCE_TYPES = Set.of(
        "currency","tax_rate","taxrate","invoice_number","invoicenumber","vin","vehicle"
    );

    private static final Set<String> IOT_TYPES = Set.of(
        "rfid_uid","epc","rfid_tag","nfc_uid","nfc_atqa","nfc_sak",
        "ndef_uri","ndef_text","apdu","nfc_tag",
        "ir_nec","ir_rc5","ir_pronto","ir_raw","mqtt_payload","lora_packet"
    );

    private static final Set<String> BARCODE_TYPES = Set.of(
        "ean13","ean8","upca","isbn13","isbn10","gs1_128"
    );

    private static final Set<String> TELECOM_TYPES = Set.of(
        "imei","imei2","iccid","imsi","msisdn"
    );

    private static final Set<String> SECURITIES_TYPES = Set.of(
        "isin","cusip","sedol","lei","fix_message","psd2_consent",
        "figi","nsin","stock_ticker","forex_pair","forex_rate","ric","mic",
        "stock_exchange","option_contract","bond_yield","coupon_rate",
        "settlement_date","portfolio_id","portfolio_id_masked"
    );

    private static final Set<String> CRYPTO_TYPES = Set.of(
        "btc_address","eth_address","crypto_address","tx_hash","block_hash","mnemonic",
        "nft_token_id","gas_price","gas_limit","defi_protocol_name","blockchain_network",
        "wallet_label","defi_position_type","cryptocurrency_name",
        "liquidity_pool_id","liquidity_pool_id_masked"
    );

    private static final Set<String> ECOMMERCE_TYPES = Set.of(
        "product_name","sku","order_id","tracking_number","category","rating","dhl_tracking"
    );

    private static final Set<String> LOCATION_TYPES = Set.of(
        "latitude","longitude","timezone","country_code","coordinates"
    );

    private static final Set<String> SOCIAL_TYPES = Set.of(
        "username","hashtag","bio","handle","follower_count"
    );

    private static final Set<String> HARDWARE_TYPES = Set.of(
        "track1_data","track2_data","chip_data","pin_block","pin_block_fmt3"
    );

    private static final Set<String> CARDPHYSICS_TYPES = Set.of(
        "emv_arqc","emv_atc","emv_iad",
        "iso8583_auth_request","iso8583_auth_response","iso8583_reversal",
        "atm_session","pos_receipt"
    );

    private static final Set<String> CYBERSEC_TYPES = Set.of(
        "cef_log","x509_cert","pcap_hex","password","password_hash","cve_id"
    );

    private static final Set<String> AVIATION_TYPES = Set.of(
        "iata_ticket","imo_number","pnr_code"
    );

    private static final Set<String> FIDO2_TYPES = Set.of(
        "webauthn_credential","fido2_assertion"
    );

    private static final Set<String> WALLET_TYPES = Set.of(
        "eth_wallet","btc_wallet","sol_wallet"
    );

    private static final Set<String> AI_VECTOR_TYPES = Set.of(
        "ai_embedding","ai_vector","ai_sparse_vector"
    );

    private static final Set<String> OIDC_TYPES = Set.of(
        "oidc_token_set","jwks","oidc_token"
    );

    private static final Set<String> BANK_STATEMENT_TYPES = Set.of(
        "mt940","camt053"
    );

    private static final Set<String> EDI_TYPES = Set.of(
        "edi_850","edifact_orders"
    );

    private static final Set<String> EVENT_SOURCING_TYPES = Set.of(
        "event_stream","cdc_event"
    );

    private static final Set<String> TELEMETRY_TYPES = Set.of(
        "fdr_record","drone_telemetry"
    );

    private static final Set<String> CRYPTO_FUZZ_TYPES = Set.of(
        "jwt_attack","asn1_fuzz"
    );

    private static final Set<String> MRZ_TYPES = Set.of(
        "mrz_td3","mrz_td1"
    );

    private static final Set<String> OHLCV_TYPES = Set.of(
        "ohlcv_candles","market_tick"
    );

    private static final Set<String> NMEA_TYPES = Set.of(
        "nmea_gpgga","nmea_gprmc"
    );

    private static final Set<String> PROMETHEUS_TYPES = Set.of(
        "prometheus_metrics","openmetrics_snapshot"
    );

    private static final Set<String> GAMEDEV_TYPES = Set.of(
        "quaternion","navmesh_path"
    );

    private static final Set<String> UBL_TYPES = Set.of(
        "ubl_invoice","xmldsig"
    );

    private static final Set<String> AUTOMOTIVE_TYPES = Set.of(
        "can_frame","obd2_response"
    );

    private static final Set<String> TLE_TYPES = Set.of(
        "tle_satellite"
    );

    private static final Set<String> PAYMENTS_TYPES = Set.of(
        "swift_mt103","pain001","nacha_ach","sepa_mandate","fedwire"
    );

    private static final Set<String> COMPLIANCE_TYPES = Set.of(
        "policy_number","claim_number","pep_status","aml_risk_rating","cdd_level",
        "sar_number","ubo_ownership_percentage","kyc_document_type","consent_id",
        "tpp_id","onboarding_method","sanctions_hit",
        "policy_number_masked","claim_number_masked","sar_number_masked",
        "ubo_ownership_percentage_masked","consent_id_masked"
    );

    private static final Set<String> FINANCIAL_EXT_TYPES = Set.of(
        "credit_score_model","credit_score_tier","credit_limit","credit_utilization",
        "credit_card_issuer_name","apr","loan_type","mortgage_rate","mortgage_term",
        "premium_amount","deductible","coverage_limit","claim_status",
        "credit_limit_masked","mortgage_rate_masked","premium_amount_masked"
    );

    private static final Set<String> DATETIME_TYPES = Set.of(
        "past_date","future_date","date_between","date_this_year",
        "date_this_month","time_only","past_datetime","future_datetime"
    );

    private static final Set<String> REVERSE_REGEX_TYPES = Set.of(
        "reverse_regex"
    );

    private static final Set<String> INTL_IDS_TYPES = Set.of(
        "br_cpf","br_cnpj",
        "in_pan","in_aadhaar","in_gstin","in_epic",
        "cn_ric",
        "mx_curp","mx_rfc",
        "it_codicefiscale",
        "es_dni","es_nie","es_ccc",
        "de_idnr","de_stnr",
        "pk_cnic",
        "jp_cn","jp_in",
        "kr_rrn","kr_brn",
        "nl_bsn",
        "pl_pesel",
        "se_personnummer",
        "dk_cpr",
        "fi_hetu",
        "no_fodselsnummer",
        "au_abn","au_tfn","au_acn",
        "my_nric",
        "th_pin","th_tin",
        "sg_uen",
        "za_idnr",
        "ca_bn",
        "nz_ird",
        "ar_cuit","ar_dni",
        "cl_rut",
        "co_nit",
        "il_idnr",
        "ro_cnp","ro_cui",
        "hr_oib",
        "bg_egn",
        "lt_asmens",
        "ee_ik",
        "pt_cc",
        "eg_tn"
    );

    // ── O(1) Dispatch Map ────────────────────────────────────────────────────

    @FunctionalInterface
    private interface GeneratorFn {
        String call(String type, String locale, String qualifier);
    }

    private static final Map<String, GeneratorFn> DISPATCH;

    static {
        HashMap<String, GeneratorFn> m = new HashMap<>(512);
        IDENTITY_TYPES    .forEach(t -> m.put(t, IdentityGen::generate));
        FINANCIAL_TYPES   .forEach(t -> m.put(t, FinancialGen::generate));
        COMM_TYPES        .forEach(t -> m.put(t, (tp, lc, q) -> CommunicationGen.generate(tp, lc)));
        META_TYPES        .forEach(t -> m.put(t, MetaGen::generate));
        BANKING_TYPES     .forEach(t -> m.put(t, (tp, lc, q) -> BankingGen.generate(tp, lc)));
        CORPORATE_TYPES   .forEach(t -> m.put(t, (tp, lc, q) -> CorporateGen.generate(tp, lc)));
        HEALTH_TYPES      .forEach(t -> m.put(t, (tp, lc, q) -> HealthGen.generate(tp, lc)));
        COMMERCE_TYPES    .forEach(t -> m.put(t, (tp, lc, q) -> CommerceGen.generate(tp, lc)));
        IOT_TYPES         .forEach(t -> m.put(t, (tp, lc, q) -> IoTGen.generate(tp, lc)));
        BARCODE_TYPES     .forEach(t -> m.put(t, (tp, lc, q) -> BarcodeGen.generate(tp, lc)));
        TELECOM_TYPES     .forEach(t -> m.put(t, (tp, lc, q) -> TelecomGen.generate(tp, lc)));
        SECURITIES_TYPES  .forEach(t -> m.put(t, FinancialMarketsGen::generate));
        CRYPTO_TYPES      .forEach(t -> m.put(t, CryptoGen::generate));
        ECOMMERCE_TYPES   .forEach(t -> m.put(t, EcommerceGen::generate));
        LOCATION_TYPES    .forEach(t -> m.put(t, (tp, lc, q) -> LocationGen.generate(tp, lc)));
        SOCIAL_TYPES      .forEach(t -> m.put(t, (tp, lc, q) -> SocialGen.generate(tp, lc)));
        HARDWARE_TYPES    .forEach(t -> m.put(t, (tp, lc, q) -> HardwareGen.generate(tp, lc)));
        CARDPHYSICS_TYPES .forEach(t -> m.put(t, (tp, lc, q) -> CardPhysicsGen.generate(tp, lc)));
        CYBERSEC_TYPES    .forEach(t -> m.put(t, (tp, lc, q) -> SecurityGen.generate(tp, lc)));
        AVIATION_TYPES    .forEach(t -> m.put(t, (tp, lc, q) -> AviationGen.generate(tp, lc)));
        FIDO2_TYPES       .forEach(t -> m.put(t, (tp, lc, q) -> Fido2Gen.generate(tp, lc)));
        WALLET_TYPES      .forEach(t -> m.put(t, (tp, lc, q) -> WalletGen.generate(tp, lc)));
        AI_VECTOR_TYPES   .forEach(t -> m.put(t, AiVectorGen::generate));
        OIDC_TYPES        .forEach(t -> m.put(t, (tp, lc, q) -> OidcGen.generate(tp, lc)));
        BANK_STATEMENT_TYPES.forEach(t -> m.put(t, (tp, lc, q) -> BankStatementGen.generate(tp, lc)));
        EDI_TYPES         .forEach(t -> m.put(t, (tp, lc, q) -> EdiGen.generate(tp, lc)));
        EVENT_SOURCING_TYPES.forEach(t -> m.put(t, (tp, lc, q) -> EventSourcingGen.generate(tp, lc)));
        TELEMETRY_TYPES   .forEach(t -> m.put(t, (tp, lc, q) -> TelemetryGen.generate(tp, lc)));
        CRYPTO_FUZZ_TYPES .forEach(t -> m.put(t, (tp, lc, q) -> CryptoFuzzGen.generate(tp, lc)));
        MRZ_TYPES         .forEach(t -> m.put(t, (tp, lc, q) -> MrzGen.generate(tp, lc)));
        OHLCV_TYPES       .forEach(t -> m.put(t, (tp, lc, q) -> OhlcvGen.generate(tp, lc)));
        NMEA_TYPES        .forEach(t -> m.put(t, (tp, lc, q) -> NmeaGen.generate(tp, lc)));
        PROMETHEUS_TYPES  .forEach(t -> m.put(t, (tp, lc, q) -> PrometheusGen.generate(tp, lc)));
        GAMEDEV_TYPES     .forEach(t -> m.put(t, (tp, lc, q) -> GameDevGen.generate(tp, lc)));
        UBL_TYPES         .forEach(t -> m.put(t, (tp, lc, q) -> UblGen.generate(tp, lc)));
        AUTOMOTIVE_TYPES  .forEach(t -> m.put(t, (tp, lc, q) -> AutomotiveGen.generate(tp, lc)));
        TLE_TYPES         .forEach(t -> m.put(t, (tp, lc, q) -> TleGen.generate(tp, lc)));
        PAYMENTS_TYPES    .forEach(t -> m.put(t, (tp, lc, q) -> PaymentsGen.generate(tp, lc)));
        REVERSE_REGEX_TYPES.forEach(t -> m.put(t, ReverseRegexGen::generate));
        INTL_IDS_TYPES    .forEach(t -> m.put(t, (tp, lc, q) -> IntlIdsGen.generate(tp, lc)));
        COMPLIANCE_TYPES  .forEach(t -> m.put(t, (tp, lc, q) -> ComplianceGen.generate(tp, lc)));
        FINANCIAL_EXT_TYPES.forEach(t -> m.put(t, (tp, lc, q) -> FinancialExtGen.generate(tp, lc)));
        DATETIME_TYPES    .forEach(t -> m.put(t, DateTimeGen::generate));
        // special case: cardowner = uppercase fullname (qualifier = gender)
        m.put("cardowner", (tp, lc, q) -> IdentityGen.generate("fullname", lc, q).toUpperCase());
        DISPATCH = m;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public static String generate(String type, String locale) {
        return generate(type, locale, "");
    }

    public static String generate(String type, String locale, String qualifier) {
        if (type == null || type.isEmpty()) return "ERROR: Missing DataType";
        GeneratorFn fn = DISPATCH.get(type);
        return fn != null ? fn.call(type, locale, qualifier) : "ERROR: Unknown DataType '" + type + "'";
    }
}
