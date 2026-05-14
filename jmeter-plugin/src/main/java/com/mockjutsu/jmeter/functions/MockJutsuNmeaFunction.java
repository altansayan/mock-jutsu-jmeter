package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuNmeaFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_nmea"; }
    @Override protected String typeDescription() {
        return "nmea_gpgga | nmea_gprmc";
    }
}
