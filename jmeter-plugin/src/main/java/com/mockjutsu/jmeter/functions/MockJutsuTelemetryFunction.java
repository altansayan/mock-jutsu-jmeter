package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuTelemetryFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_telemetry"; }
    @Override protected String typeDescription() {
        return "fdr_record | drone_telemetry";
    }
}
