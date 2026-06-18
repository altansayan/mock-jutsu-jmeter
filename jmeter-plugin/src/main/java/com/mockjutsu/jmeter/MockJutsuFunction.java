package com.mockjutsu.jmeter;

/**
 * Generic catch-all: ${__mockjutsu(type,locale,varName)}
 * Single type  → ${__mockjutsu(tckn,,)}              returns the generated value.
 * Multi types  → ${__mockjutsu(tckn,iban,cardnum,uuid,,)} returns a JSON object.
 *   Pass types as separate comma-separated JMeter params; last two are locale and varName.
 *   If varName is set, each type is also stored as varName_tckn, varName_iban, etc.
 */
public final class MockJutsuFunction extends MockJutsuBaseFunction {

    @Override
    public String getReferenceKey() { return "__mockjutsu"; }

    @Override
    protected String typeDescription() {
        return "type — any type — tckn | iban | cardnum | uuid | email | ... (all ~251 types supported); " +
               "for multi-type: ${__mockjutsu(tckn,iban,cardnum,uuid,,)} — types as separate params, last two = locale, varName. ~300 types supported including IntlIDs (br_cpf, in_aadhaar, kr_rrn, ...";
    }
}
