package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuCardPhysicsFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_cardphysics"; }
    @Override protected String typeDescription() {
        return "emv_arqc | emv_atc | emv_iad | iso8583_auth_request | iso8583_auth_response | iso8583_reversal | atm_session | pos_receipt";
    }
}
