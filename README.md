# mock-jutsu — JMeter Function Plugin

[![Build Status](https://github.com/altansayan/mock-jutsu-jmeter/actions/workflows/build.yml/badge.svg)](https://github.com/altansayan/mock-jutsu-jmeter/actions)
[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net)
[![JMeter](https://img.shields.io/badge/JMeter-5.6%2B-red)](https://jmeter.apache.org)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

Generate **912-tested**, format-valid synthetic test data directly inside JMeter test plans — no Python, no subprocess, no external dependencies.

```
${__mockjutsu_identity(tckn,TR,)}       → 46396909916
${__mockjutsu_financial(iban,DE,)}      → DE89370400440532013000
${__mockjutsu_financial(cardnum,US,)}   → 4532015112830366
${__mockjutsu_banking(swift,TR,)}       → AKBKTRIS
${__mockjutsu_mrz(mrz_td3,TR,)}        → P<TUR... (2×44 chars)
${__mockjutsu(uuid,,myId)}              → stores UUID in ${myId}
```

---

## Installation

1. Download `mock-jutsu-jmeter-1.0.0.jar` from [Releases](https://github.com/altansayan/mock-jutsu-jmeter/releases)
2. Copy to `$JMETER_HOME/lib/ext/`
3. Restart JMeter
4. Open **Options → Function Helper Dialog** — search for `mockjutsu`

---

## Usage

### Syntax

```
${__mockjutsu_<category>(type,locale,varName)}
```

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `type` | Yes | — | Data type to generate (see categories below) |
| `locale` | No | `TR` | `TR` \| `DE` \| `FR` \| `UK` \| `US` \| `RU` |
| `varName` | No | — | Store result in a JMeter variable |

### Generic function (all types)

```
${__mockjutsu(tckn,TR,myVar)}
```

---

## Categories & Types

### Identity — `__mockjutsu_identity`
`tckn` `ykn` `taxid` `vkn` `nationalid` `ssn` `nin` `inn` `inn_individual` `snils` `sgk` `mersis` `ein` `utr` `crn` `paye` `ust_id` `siren` `siret` `tva` `ogrn` `kpp` `employer_id` `insurance_id` `firstname` `lastname` `fullname` `patronymic` `passport` `license` `age` `gender` `birthdate` `tckn_masked` `ssn_masked` `nationality` `vat_number` `cardowner`

### Financial — `__mockjutsu_financial`
`cardnum` `cardnetwork` `cardtype` `cardstatus` `cvv3` `cvv4` `issuer` `expiry` `expirymonth` `expiryyear` `pin` `balance` `iban` `cardcategory` `credit_score` `sepa_qr` `emv_qr_p2p` `emv_qr_atm` `emv_qr_pos` `3ds_cavv` `3ds_eci`

### Banking — `__mockjutsu_banking`
`swift` `bic` `sort_code` `routing_number` `bik_code` `transaction` `bank_name` `sepa_ref` `creditor_ref`

### Payments — `__mockjutsu_payments`
`swift_mt103` `pain001` `nacha_ach` `sepa_mandate` `fedwire`

### Card Physics — `__mockjutsu_cardphysics`
`emv_arqc` `emv_atc` `emv_iad` `iso8583_auth_request` `iso8583_auth_response` `iso8583_reversal` `atm_session` `pos_receipt`

### Hardware — `__mockjutsu_hardware`
`track1_data` `track2_data` `chip_data` `pin_block` `pin_block_fmt3`

### Communication — `__mockjutsu_comm`
`phone` `phone_country` `phone_area` `phone_local` `address_city` `address_street` `address_full` `postalcode` `plate` `email`

### Meta — `__mockjutsu_meta`
`uuid` `requestid` `correlationid` `sessionid` `idempotencykey` `deviceid` `ipv4` `ipv6` `browser_name` `browser_version` `useragent` `timestamp` `timestamp_iso` `clientversion` `bearertoken` `signature` `apppassword` `jwt` `hash` `mac_address` `domain` `url` `color` `api_key` `totp_code` `webhook_signature` `transaction_id` `public_ip` `private_ip`

### Health — `__mockjutsu_health`
`blood_type` `nhs_number` `icd10` `height` `weight` `npi` `bmi` `hl7_message` `fhir_patient` `dicom_uid`

### Corporate — `__mockjutsu_corporate`
`company_name` `job_title` `occupation`

### Commerce — `__mockjutsu_commerce`
`currency` `tax_rate` `invoice_number` `vin` `vehicle`

### Barcode — `__mockjutsu_barcode`
`ean13` `ean8` `upca` `isbn13` `isbn10` `gs1_128`

### Telecom — `__mockjutsu_telecom`
`imei` `imei2` `iccid` `imsi` `msisdn`

### IoT — `__mockjutsu_iot`
`rfid_uid` `epc` `rfid_tag` `nfc_uid` `nfc_atqa` `nfc_sak` `ndef_uri` `ndef_text` `apdu` `mqtt_payload` `lora_packet`

### Location — `__mockjutsu_location`
`latitude` `longitude` `timezone` `country_code` `coordinates`

### Social — `__mockjutsu_social`
`username` `hashtag` `bio` `handle` `follower_count`

### E-Commerce — `__mockjutsu_ecommerce`
`product_name` `sku` `order_id` `tracking_number` `category` `rating` `dhl_tracking`

### Capital Markets — `__mockjutsu_markets`
`isin` `cusip` `sedol` `lei` `fix_message` `psd2_consent`

### Crypto — `__mockjutsu_crypto`
`btc_address` `eth_address` `crypto_address` `tx_hash` `block_hash` `mnemonic`

### Web3 Wallets — `__mockjutsu_wallet`
`eth_wallet` `btc_wallet` `sol_wallet`

### Security — `__mockjutsu_security`
`cef_log` `x509_cert` `pcap_hex`

### FIDO2 / WebAuthn — `__mockjutsu_fido2`
`webauthn_credential` `fido2_assertion`

### OIDC / JWT — `__mockjutsu_oidc`
`oidc_token_set` `jwks` `oidc_token`

### AI & Vector — `__mockjutsu_ai`
`ai_embedding` `ai_vector` `ai_sparse_vector`

### Aviation & Maritime — `__mockjutsu_aviation`
`iata_ticket` `imo_number` `pnr_code`

### Automotive — `__mockjutsu_automotive`
`can_frame` `obd2_response`

### MRZ — `__mockjutsu_mrz`
`mrz_td3` `mrz_td1`

### NMEA GPS — `__mockjutsu_nmea`
`nmea_gpgga` `nmea_gprmc`

### OHLCV / Market Data — `__mockjutsu_ohlcv`
`ohlcv_candles` `market_tick`

### Bank Statement — `__mockjutsu_bankstatement`
`mt940` `camt053`

### EDI — `__mockjutsu_edi`
`edi_850` `edifact_orders`

### Event Sourcing — `__mockjutsu_eventsourcing`
`event_stream` `cdc_event`

### Telemetry / FDR — `__mockjutsu_telemetry`
`fdr_record` `drone_telemetry`

### Prometheus — `__mockjutsu_prometheus`
`prometheus_metrics` `openmetrics_snapshot`

### Game Dev — `__mockjutsu_gamedev`
`quaternion` `navmesh_path`

### UBL / E-Invoice — `__mockjutsu_ubl`
`ubl_invoice` `xmldsig`

### Satellite / TLE — `__mockjutsu_tle`
`tle_satellite`

### Crypto Fuzzing — `__mockjutsu_cryptofuzz`
`jwt_attack` `asn1_fuzz`

### Regex — `__mockjutsu_regex`
`regex_string`

---

## Real-World Examples

### HTTP Request body with dynamic TCKN + IBAN
```
{
  "citizenId": "${__mockjutsu_identity(tckn,TR,)}",
  "iban": "${__mockjutsu_financial(iban,TR,)}",
  "cardNumber": "${__mockjutsu_financial(cardnum,TR,card)}",
  "requestId": "${__mockjutsu_meta(uuid,,rid)}"
}
```

### Storing and reusing values
```
// Generate once, reuse everywhere
${__mockjutsu_identity(tckn,TR,myTckn)}

// Later in the same request or another sampler:
${myTckn}
```

### Multi-locale load test
```
${__mockjutsu_identity(ssn,US,)}      // US Social Security Number
${__mockjutsu_identity(nin,UK,)}      // UK National Insurance Number
${__mockjutsu_identity(tckn,TR,)}     // Turkish Citizen ID
${__mockjutsu_financial(iban,DE,)}    // German IBAN (MOD-97 valid)
```

---

## Algorithm Guarantees

All generated data passes real checksum and format validation:

| Type | Algorithm |
|------|-----------|
| TCKN | d9/d10 Turkish checksum |
| IBAN | MOD-97 (ISO 13616) |
| Card numbers | Luhn |
| IMEI | Luhn |
| NHS Number | Modulo 11 |
| EAN-13/8 | GS1 checksum |
| ISIN | Luhn (alphanumeric) |
| MRZ | ICAO Doc 9303 composite check digit |
| NMEA | XOR checksum |
| PIN Block | ISO 9564-1 Format 0 & 3 |
| ISO 8583 | Pre-computed bitmaps |
| ABA Routing | Modulo 10 |

---

## Building from Source

```bash
git clone https://github.com/altansayan/mock-jutsu-jmeter.git
cd mock-jutsu-jmeter/jmeter-plugin
mvn test          # 912 tests
mvn package       # produces target/mock-jutsu-jmeter-1.0.0.jar
```

**Requirements:** Java 17+, Maven 3.8+

---

## Related Projects

- [mock-jutsu Python API](https://github.com/altansayan/mock-jutsu) — REST API + CLI, same data types, PyPI package

---

## License

MIT © 2026 Altan Sezer Ayan
