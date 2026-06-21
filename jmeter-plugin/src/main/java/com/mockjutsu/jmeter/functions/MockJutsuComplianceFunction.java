package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuComplianceFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_compliance"; }
    @Override protected String typeDescription() {
        return "policy_number | claim_number | pep_status | aml_risk_rating | cdd_level | sar_number | ubo_ownership_percentage | kyc_document_type | consent_id | tpp_id | onboarding_method | sanctions_hit | sanctions_hit_masked";
    }
}
