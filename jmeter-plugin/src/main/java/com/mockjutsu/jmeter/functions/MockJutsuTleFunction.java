package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuTleFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_tle"; }
    @Override protected String typeDescription() {
        return "tle_satellite";
    }
}
