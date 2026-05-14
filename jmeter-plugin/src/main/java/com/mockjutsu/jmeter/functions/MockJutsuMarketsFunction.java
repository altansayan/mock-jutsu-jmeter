package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuMarketsFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_markets"; }
    @Override protected String typeDescription() {
        return "isin | cusip | sedol | lei | fix_message | psd2_consent";
    }
}
