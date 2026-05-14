package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuOidcFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_oidc"; }
    @Override protected String typeDescription() {
        return "oidc_token_set | jwks | oidc_token";
    }
}
