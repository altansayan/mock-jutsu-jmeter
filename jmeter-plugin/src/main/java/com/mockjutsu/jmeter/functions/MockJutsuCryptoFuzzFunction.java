package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuCryptoFuzzFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_cryptofuzz"; }
    @Override protected String typeDescription() {
        return "jwt_attack | asn1_fuzz";
    }
}
