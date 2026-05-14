package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuUblFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_ubl"; }
    @Override protected String typeDescription() {
        return "ubl_invoice | xmldsig";
    }
}
