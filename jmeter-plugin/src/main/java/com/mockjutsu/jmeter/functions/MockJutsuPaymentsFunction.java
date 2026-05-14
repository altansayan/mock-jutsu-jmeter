package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuPaymentsFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_payments"; }
    @Override protected String typeDescription() {
        return "swift_mt103 | pain001 | nacha_ach | sepa_mandate | fedwire";
    }
}
