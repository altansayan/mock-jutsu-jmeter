package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuFinancialExtFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_financial_ext"; }
    @Override protected String typeDescription() {
        return "credit_score_model | credit_score_tier | credit_limit | credit_utilization | credit_card_issuer_name | apr | loan_type | mortgage_rate | mortgage_term | premium_amount | deductible | coverage_limit | claim_status";
    }
}
