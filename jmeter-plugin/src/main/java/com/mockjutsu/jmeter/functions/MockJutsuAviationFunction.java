package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuAviationFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_aviation"; }
    @Override protected String typeDescription() {
        return "iata_ticket | imo_number | pnr_code";
    }
}
