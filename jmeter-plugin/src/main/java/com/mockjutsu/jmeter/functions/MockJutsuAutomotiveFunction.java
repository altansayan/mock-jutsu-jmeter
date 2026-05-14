package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuAutomotiveFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_automotive"; }
    @Override protected String typeDescription() {
        return "can_frame | obd2_response";
    }
}
