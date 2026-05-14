package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuTelecomFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_telecom"; }
    @Override protected String typeDescription() {
        return "imei | imei2 | iccid | imsi | msisdn";
    }
}
