package com.mockjutsu.jmeter;

/**
 * Generic catch-all: ${__mockjutsu(type,locale,varName)}
 * Single type  → returns the generated value as a string.
 * Multi types  → ${__mockjutsu(tckn\,iban\,cardnum\,uuid,,)} returns a JSON object.
 *   Use backslash-escaped commas to pass multiple types in JMeter expressions.
 *   If varName is set, each type is also stored as varName_tckn, varName_iban, etc.
 */
public final class MockJutsuFunction extends MockJutsuBaseFunction {

    @Override
    public String getReferenceKey() { return "__mockjutsu"; }

    @Override
    protected String typeDescription() {
        return "type — any type — tckn | iban | cardnum | uuid | email | ... (all ~251 types supported); " +
               "use tckn\\,iban\\,cardnum for multi-type JSON output";
    }
}
