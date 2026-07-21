# Changelog

All notable changes to Mock Jutsu JMeter Plugin are documented here.

## [1.0.1] - 2026-07-22

**Not backward compatible with 1.0.0.** The previous release's algorithms were
silently wrong for the majority of types — this release corrects them to match
the Python (`mockjutsu`) ground truth exactly. Any value previously generated
by 1.0.0 for an affected type (checksum, format, or structure) will differ
under 1.0.1. 1.0.0 is deprecated and should not be used.

### Fixed
- **273 mismatches** found and corrected across all 39 generator files, via a
  full audit comparing every shared type against the Python engine's real
  output (not just structural/exception checks).
- `ean13Check()` (shared by EAN-13, EAN-8, UPC-A, GS1-128) — checksum weight
  assignment was only coincidentally correct for even-length payloads
  (EAN-13); odd-length payloads (EAN-8, UPC-A, GS1-128) produced invalid
  check digits. Verified against official GS1 test vectors.
- `EcommerceGen.dhlTracking()` — Luhn checksum was computed over the "JD"
  prefix letters instead of the numeric body only.
- Turkish-locale JVM default `String.toUpperCase()`/`toLowerCase()` producing
  dotted/dotless İ instead of ASCII I in card-network and IBAN generation.
- `OidcGen` — real P-256 ECDSA (ES256) signing implemented from scratch
  (previously not cryptographically valid), verified against independent
  signature verification.
- `Fido2Gen` — real CBOR-encoded WebAuthn/FIDO2 attestation objects
  (previously placeholder structure).
- Numerous `str(dict)`-vs-`json.dumps()` output format mismatches (IoT and
  commerce types returning Python repr syntax instead of JSON).
- Wallet/crypto address derivation, BIP-39 mnemonics, and ~30 additional
  algorithm-level bugs from the original audit pass (see project memory for
  full list).
- Turkish-locale JVM default `String.format("%f"...)` (no explicit `Locale`)
  producing comma instead of period as the decimal separator — found in
  `TleGen` (epoch day), `EcommerceGen` (rating), `FinancialGen` (balance),
  and `LocationGen` (latitude/longitude). Caught by running the full existing
  JUnit suite (4183 tests) against the fix, which the original 273-mismatch
  audit had not exercised.
- Removed `sanctions_hit_masked` — a dead type registered in the JMeter
  dispatcher with no backing implementation and no Python counterpart.

### Changed
- `TelecomGen.imei()` and `BarcodeGen.ean13()` signatures adjusted as part of
  the correctness fixes; internal unit tests updated to match.
- Updated ~35 pre-existing unit test assertions that encoded the old (Java-only,
  pre-1.0.1) output format/casing/values instead of the real Python-parity
  behavior — verified each individually against Python ground truth before
  changing (casing of card type/status/category, IoT `str(dict)` vs JSON
  quoting, UK NIN/PAYE/German RVN/HRB spacing, `tx_hash`/`signature`/
  `timestamp_iso`/`swift_mt103` format assumptions, and others).

## [1.0.0] - 2026-06-01

### Initial Release
- 390+ format-valid mock data types via JMeter custom functions
- Syntax: `${__mockjutsu_*(type|locale|varName|mask)}`
- 6 locales: TR, UK, US, DE, FR, RU
- Pipe-separated options: `${__mockjutsu_financial(cardnum:visa|TR|myVar|mask)}`
- Mask support: PCI DSS, GDPR, KVKK
- Zero external dependencies (fat JAR)
- 5955 tests passed
- Categories: financial, identity, healthcare, crypto, IoT, network and more
