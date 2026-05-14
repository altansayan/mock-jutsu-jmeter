package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuSecurityFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_security"; }
    @Override protected String typeDescription() {
        return "cef_log | x509_cert | pcap_hex";
    }
}
