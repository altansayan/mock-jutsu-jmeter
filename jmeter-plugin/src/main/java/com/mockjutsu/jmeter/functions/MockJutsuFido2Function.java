package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuFido2Function extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_fido2"; }
    @Override protected String typeDescription() {
        return "webauthn_credential | fido2_assertion";
    }
}
