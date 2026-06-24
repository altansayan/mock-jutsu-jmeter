# Changelog

All notable changes to Mock Jutsu JMeter Plugin are documented here.

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
