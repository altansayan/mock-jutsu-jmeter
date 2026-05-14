package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuEventSourcingFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_eventsourcing"; }
    @Override protected String typeDescription() {
        return "event_stream | cdc_event";
    }
}
