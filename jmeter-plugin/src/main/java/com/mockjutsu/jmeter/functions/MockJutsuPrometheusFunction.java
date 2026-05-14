package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuPrometheusFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_prometheus"; }
    @Override protected String typeDescription() {
        return "prometheus_metrics | openmetrics_snapshot";
    }
}
