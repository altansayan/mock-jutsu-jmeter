package com.mockjutsu.jmeter;

import com.mockjutsu.jmeter.generators.*;

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
        "api_key","totp_code","webhook_signature","transaction_id","public_ip","private_ip"
    );

    private static final Set<String> BANKING_TYPES = Set.of(
        "swift","bic","sort_code","routing_number","bik_code",
        "transaction","bank_name","sepa_ref","creditor_ref"
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
        "isin","cusip","sedol","lei","fix_message","psd2_consent"
    );

    private static final Set<String> CRYPTO_TYPES = Set.of(
        "btc_address","eth_address","crypto_address","tx_hash","block_hash","mnemonic"
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
        "cef_log","x509_cert","pcap_hex"
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

    private static final Set<String> REVERSE_REGEX_TYPES = Set.of(
        "regex_string"
    );

    // ── Dispatch ─────────────────────────────────────────────────────────────

    public static String generate(String type, String locale) {
        if (type == null || type.isEmpty())    return "ERROR: Missing DataType";

        // special case: cardowner = uppercase fullname
        if ("cardowner".equals(type))
            return IdentityGen.generate("fullname", locale).toUpperCase();

        if (IDENTITY_TYPES.contains(type))     return IdentityGen.generate(type, locale);
        if (FINANCIAL_TYPES.contains(type))    return FinancialGen.generate(type, locale);
        if (COMM_TYPES.contains(type))         return CommunicationGen.generate(type, locale);
        if (META_TYPES.contains(type))         return MetaGen.generate(type, locale);
        if (BANKING_TYPES.contains(type))      return BankingGen.generate(type, locale);
        if (CORPORATE_TYPES.contains(type))    return CorporateGen.generate(type, locale);
        if (HEALTH_TYPES.contains(type))       return HealthGen.generate(type, locale);
        if (COMMERCE_TYPES.contains(type))     return CommerceGen.generate(type, locale);
        if (IOT_TYPES.contains(type))          return IoTGen.generate(type, locale);
        if (BARCODE_TYPES.contains(type))      return BarcodeGen.generate(type, locale);
        if (TELECOM_TYPES.contains(type))      return TelecomGen.generate(type, locale);
        if (SECURITIES_TYPES.contains(type))   return FinancialMarketsGen.generate(type, locale);
        if (CRYPTO_TYPES.contains(type))       return CryptoGen.generate(type, locale);
        if (ECOMMERCE_TYPES.contains(type))    return EcommerceGen.generate(type, locale);
        if (LOCATION_TYPES.contains(type))     return LocationGen.generate(type, locale);
        if (SOCIAL_TYPES.contains(type))       return SocialGen.generate(type, locale);
        if (HARDWARE_TYPES.contains(type))     return HardwareGen.generate(type, locale);
        if (CARDPHYSICS_TYPES.contains(type))  return CardPhysicsGen.generate(type, locale);
        if (CYBERSEC_TYPES.contains(type))     return SecurityGen.generate(type, locale);
        if (AVIATION_TYPES.contains(type))     return AviationGen.generate(type, locale);
        if (FIDO2_TYPES.contains(type))        return Fido2Gen.generate(type, locale);
        if (WALLET_TYPES.contains(type))       return WalletGen.generate(type, locale);
        if (AI_VECTOR_TYPES.contains(type))    return AiVectorGen.generate(type, locale);
        if (OIDC_TYPES.contains(type))         return OidcGen.generate(type, locale);
        if (BANK_STATEMENT_TYPES.contains(type)) return BankStatementGen.generate(type, locale);
        if (EDI_TYPES.contains(type))          return EdiGen.generate(type, locale);
        if (EVENT_SOURCING_TYPES.contains(type)) return EventSourcingGen.generate(type, locale);
        if (TELEMETRY_TYPES.contains(type))    return TelemetryGen.generate(type, locale);
        if (CRYPTO_FUZZ_TYPES.contains(type))  return CryptoFuzzGen.generate(type, locale);
        if (MRZ_TYPES.contains(type))          return MrzGen.generate(type, locale);
        if (OHLCV_TYPES.contains(type))        return OhlcvGen.generate(type, locale);
        if (NMEA_TYPES.contains(type))         return NmeaGen.generate(type, locale);
        if (PROMETHEUS_TYPES.contains(type))   return PrometheusGen.generate(type, locale);
        if (GAMEDEV_TYPES.contains(type))      return GameDevGen.generate(type, locale);
        if (UBL_TYPES.contains(type))          return UblGen.generate(type, locale);
        if (AUTOMOTIVE_TYPES.contains(type))   return AutomotiveGen.generate(type, locale);
        if (TLE_TYPES.contains(type))          return TleGen.generate(type, locale);
        if (PAYMENTS_TYPES.contains(type))     return PaymentsGen.generate(type, locale);
        if (REVERSE_REGEX_TYPES.contains(type)) return ReverseRegexGen.generate(type, locale);

        return "ERROR: Unknown DataType '" + type + "'";
    }
}
