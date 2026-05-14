package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuEdiFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_edi"; }
    @Override protected String typeDescription() {
        return "edi_850 | edifact_orders";
    }
}
