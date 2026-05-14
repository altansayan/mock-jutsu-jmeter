package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuMrzFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_mrz"; }
    @Override protected String typeDescription() {
        return "mrz_td3 | mrz_td1";
    }
}
