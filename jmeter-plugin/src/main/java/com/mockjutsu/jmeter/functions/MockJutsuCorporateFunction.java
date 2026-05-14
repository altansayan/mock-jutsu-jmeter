package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuCorporateFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_corporate"; }
    @Override protected String typeDescription() {
        return "company_name | job_title | occupation | jobtitle";
    }
}
