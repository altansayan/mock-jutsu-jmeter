package com.mockjutsu.jmeter;

/**
 * Generic catch-all: ${__mockjutsu(type,locale,varName)}
 * Single type  → ${__mockjutsu(tckn,,)}              returns the generated value.
 * Multi types  → ${__mockjutsu(tckn,iban,cardnum,uuid,,)} returns a JSON object.
 *   Pass types as separate comma-separated JMeter params; last two are locale and varName.
 *   If varName is set, the full result (single value or JSON) is stored in that JMeter variable.
 *   Note: multi-type mode does NOT create per-type variables (varName_tckn, etc.) —
 *   the entire JSON is stored under the single varName.
 */
public final class MockJutsuFunction extends MockJutsuBaseFunction {

    @Override
    public String getReferenceKey() { return "__mockjutsu"; }

    @Override
    protected String typeDescription() {
        return "type — any of 340 types — tckn | iban | cardnum | uuid | email | ... ; " +
               "for multi-type: ${__mockjutsu(tckn,iban,cardnum,uuid,,)} — types as separate params, " +
               "last two = locale, varName (stores full result in JMeter variable). " +
               "Includes IntlIDs (br_cpf, in_aadhaar, kr_rrn, ...) and category functions.";
    }
}
