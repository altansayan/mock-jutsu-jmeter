package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuBankStatementFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_bankstatement"; }
    @Override protected String typeDescription() {
        return "mt940 | camt053";
    }
}
