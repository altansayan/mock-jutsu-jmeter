package com.mockjutsu.jmeter;

/** Generic catch-all: ${__mockjutsu(type,locale,varName)} — accepts any of the ~245 types. */
public final class MockJutsuFunction extends MockJutsuBaseFunction {

    @Override
    public String getReferenceKey() { return "__mockjutsu"; }

    @Override
    protected String typeDescription() {
        return "any type — tckn | iban | cardnum | uuid | email | ... (all ~245 types supported)";
    }
}
