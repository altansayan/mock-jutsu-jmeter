# 🥷 mock-jutsu — Tam Veri Matrisi (Full Data Matrix)

Bu tablo, `mock-jutsu`'nun desteklediği tüm veri tiplerini, JMeter fonksiyon imzalarını ve ülkelere göre beklenen formatları içerir.

| # | Kategori | Veri Tipi | JMeter Fonksiyonu | 🇹🇷 TR Format | 🇬🇧 UK Format | 🇩🇪 DE Format | 🇫🇷 FR Format | 🇷🇺 RU Format | 🇺🇸 US Format |
|---|---|---|---|---|---|---|---|---|---|
| **1** | **Kimlik** | Ulusal ID | `${__mockjutsu(NationalID)}` | TCKN (11 h) | NIN (AB123456C) | Perso (10 h) | NIR (15 h) | Pasport (10 h) | SSN (9 h) |
| **2** | | Yabancı ID | `${__mockjutsu(YKN)}` | 99 ile başlar | — | — | — | — | — |
| **3-8** | | Kişisel | `FirstName, LastName, Patronymic, BirthDate, Age, Gender` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **9** | | Vergi No | `${__mockjutsu(TaxID)}` | 10 hane | UTR (10 h) | StID (11 h) | NIF (13 h) | INN (12 h) | EIN (9 h) |
| **10-11** | | Belgeler | `Passport, License` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **12-21** | **Finans** | Kart | `CardNum, CVV, PIN, Expiry, Balance, CardHolder, Issuer, Category` | 🌍 | 🌍 | 🌍 | 🌍 | 🌍 | 🌍 |
| **22** | | Hesap | `IBAN / BankAccount / RoutingNum` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **23-27** | **İletişim**| Detay | `Phone, Email, Address, PostalCode, Plate` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **28-29** | **Güvenlik**| Şifre | `Password, AppPassword` | 🌍 | 🌍 | 🌍 | 🌍 | 🌍 | 🌍 |
| **30** | **Header** | **Genel UUID** | `${__mockjutsu(UUID)}` | **UUID v4** | **UUID v4** | **UUID v4** | **UUID v4** | **UUID v4** | **UUID v4** |
| **31** | | Request ID | `${__mockjutsu(RequestID)}` | UUID v4 | UUID v4 | UUID v4 | UUID v4 | UUID v4 | UUID v4 |
| **32** | | Correlation ID| `${__mockjutsu(CorrelationID)}`| UUID v4 | UUID v4 | UUID v4 | UUID v4 | UUID v4 | UUID v4 |
| **33** | | Session ID | `${__mockjutsu(SessionID)}` | UUID v4 | UUID v4 | UUID v4 | UUID v4 | UUID v4 | UUID v4 |
| **34** | | Idem. Key | `${__mockjutsu(IdempotencyKey)}` | UUID v4 | UUID v4 | UUID v4 | UUID v4 | UUID v4 | UUID v4 |
| **35** | | Timestamp | `${__mockjutsu(Timestamp)}` | Unix Epoch | Unix Epoch | Unix Epoch | Unix Epoch | Unix Epoch | Unix Epoch |
| **36** | | Signature | `${__mockjutsu(Signature,key,payload)}` | HMAC-256 | HMAC-256 | HMAC-256 | HMAC-256 | HMAC-256 | HMAC-256 |
| **37** | **Cihaz** | User Agent | `${__mockjutsu(UA,iOS)}` | Mobile | Mobile | Mobile | Mobile | Mobile | Mobile |
| **38** | | Device ID | `${__mockjutsu(DeviceID)}` | IDFA/GAID | IDFA/GAID | IDFA/GAID | IDFA/GAID | IDFA/GAID | IDFA/GAID |
| **39** | **Browser** | Browser Name| `${__mockjutsu(BrowserName)}` | Chrome/FF.. | Chrome/FF.. | Chrome/FF.. | Chrome/FF.. | Chrome/FF.. | Chrome/FF.. |
| **40** | | Version | `${__mockjutsu(BrowserVersion)}`| SemVer | SemVer | SemVer | SemVer | SemVer | SemVer |
| **41** | **Network** | IP Adresi | `${__mockjutsu(IpAddress)}` | IPv4 | IPv4 | IPv4 | IPv4 | IPv4 | IPv4 |
| **42** | **Sürüm** | Client Ver. | `${__mockjutsu(ClientVersion)}` | 1.2.3 | 1.2.3 | 1.2.3 | 1.2.3 | 1.2.3 | 1.2.3 |
| **43** | **Paket** | Bundle | `${__mockjutsu(Bundle)}` | Full Profile| Full Profile| Full Profile| Full Profile| Full Profile| Full Profile|
