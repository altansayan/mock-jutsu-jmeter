package com.mockjutsu.jmeter;

import java.util.*;

/**
 * Batch CLI for parity checking against Python generator.
 *
 * Usage:
 *   --all             generate all known types
 *   --count N         samples per type (default 20)
 *   --locale LOCALE   locale (default TR)
 *   type1 type2 ...   explicit type list
 *
 * Output: newline-delimited format
 *   TYPE:sample1\nTYPE:sample2\n...
 *
 * Python parity_check.py reads this format.
 */
public final class MockJutsuCLI {

    static final List<String> ALL_TYPES = List.of(
        // Identity
        "tckn","ykn","taxid","vkn","nationalid","ssn","nin","inn","inn_individual",
        "snils","sgk","mersis","ein","utr","crn","paye","ust_id","ustid","hrb","rvn",
        "siren","siret","tva","ogrn","kpp","employer_id","insurance_id",
        "firstname","lastname","fullname","patronymic","passport","license",
        "age","gender","birthdate","tckn_masked","ssn_masked","nationality","vat_number","cardowner",
        // Financial
        "cardnum","cardnetwork","cardtype","cardstatus","cvv3","cvv4",
        "issuer","expiry","expirymonth","expiryyear","pin","balance",
        "iban","cardcategory","credit_score","sepa_qr","emv_qr_p2p",
        "emv_qr_atm","emv_qr_pos","3ds_cavv","3ds_eci",
        // Communication
        "phone","phone_country","phone_area","phone_local",
        "address_city","address_street","address_full","postalcode","plate","email",
        // Meta
        "uuid","requestid","correlationid","sessionid","idempotencykey",
        "deviceid","ipv4","ipv6","browser_name","browser_version","browser_engine",
        "useragent","timestamp","timestamp_iso","clientversion","bearertoken",
        "signature","apppassword","jwt","hash","mac_address","domain","url","color",
        "api_key","totp_code","webhook_signature","transaction_id","public_ip","private_ip",
        // Banking
        "swift","bic","sort_code","routing_number","bik_code",
        "transaction","bank_name","sepa_ref","creditor_ref",
        // Corporate
        "company_name","job_title","occupation","jobtitle",
        // Health
        "blood_type","bloodtype","nhs_number","nhsnumber","icd10",
        "height","weight","npi","bmi","hl7_message","fhir_patient","dicom_uid",
        // Commerce
        "currency","tax_rate","taxrate","invoice_number","invoicenumber","vin","vehicle",
        // IoT
        "rfid_uid","epc","rfid_tag","nfc_uid","nfc_atqa","nfc_sak",
        "ndef_uri","ndef_text","apdu","nfc_tag",
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
        "emv_arqc","emv_atc","emv_iad",
        "iso8583_auth_request","iso8583_auth_response","iso8583_reversal",
        "atm_session","pos_receipt",
        // CyberSec
        "cef_log","x509_cert","pcap_hex",
        // Aviation
        "iata_ticket","imo_number","pnr_code",
        // Fido2
        "webauthn_credential","fido2_assertion",
        // Wallet
        "eth_wallet","btc_wallet","sol_wallet",
        // AI
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
        // Regex
        "reverse_regex",
        // IntlIDs
        "br_cpf","br_cnpj",
        "in_pan","in_aadhaar","in_gstin","in_epic",
        "cn_ric","mx_curp","mx_rfc","it_codicefiscale",
        "es_dni","es_nie","es_ccc","de_idnr","de_stnr","pk_cnic",
        "jp_cn","jp_in","kr_rrn","kr_brn",
        "nl_bsn","pl_pesel","se_personnummer","dk_cpr","fi_hetu","no_fodselsnummer",
        "au_abn","au_tfn","au_acn","my_nric","th_pin","th_tin","sg_uen","za_idnr",
        "ca_bn","nz_ird","ar_cuit","ar_dni","cl_rut","co_nit","il_idnr",
        "ro_cnp","ro_cui","hr_oib","bg_egn","lt_asmens","ee_ik","pt_cc","eg_tn"
    );

    public static void main(String[] args) {
        boolean all = false;
        int count = 20;
        String locale = "TR";
        List<String> types = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--all"    -> all = true;
                case "--count"  -> count  = Integer.parseInt(args[++i]);
                case "--locale" -> locale = args[++i];
                default         -> types.add(args[i]);
            }
        }
        if (all) types = ALL_TYPES;

        // Output: TYPE\x1Fsample  (unit-separator as delimiter — safe since values may contain \n)
        // Python splits on \x1F
        char SEP = '';
        StringBuilder sb = new StringBuilder();
        for (String type : types) {
            for (int i = 0; i < count; i++) {
                String val = MockJutsuRegistry.generate(type, locale);
                // Escape newlines in value so each record is one line
                val = val.replace("\r", "").replace("\n", "\\n");
                sb.append(type).append(SEP).append(val).append('\n');
            }
        }
        System.out.print(sb);
    }
}
