package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuRegexFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_regex"; }
    @Override protected String typeDescription() {
        return "regex_string";
    }
}
