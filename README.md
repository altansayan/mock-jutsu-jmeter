# 🥷 mock-jutsu

**Privacy-first, multi-platform test data generation engine.**

`mock-jutsu` is a high-utility data generator designed for QA engineers and developers. It provides format-valid, synthetic data for fintech, security, and general web/mobile testing.

## 🚀 Mission
To provide the testing community with a single tool that generates realistic, locale-aware data (IDs, Credit Cards, IBANs, etc.) without ever touching real sensitive information.

## 📁 Repository Structure

- `/python`: The core logic engine and CLI tool.
- `/jmeter-plugin`: Java-based `AbstractFunction` plugin for JMeter integration.
- `/web-ui`: A portable, single-file HTML generator.
- `/docs`: Detailed data reference and implementation plans.

## 🛠 Features
- **40+ Data Types**: From Identity (TCKN, SSN) to Financial (Luhn-valid cards, IBAN) and Security (HMAC Signatures).
- **Multi-Locale**: Support for TR, UK, DE, FR, RU, and US.
- **Coherent Profiling**: Linked data generation (Matching Browser + User Agent + OS).

---
*Created with the power of Ninjutsu for the QA community.*
