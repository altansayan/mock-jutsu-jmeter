package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuBankingFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_banking"; }
    @Override protected String typeDescription() {
        return "swift | bic | sort_code | routing_number | wire_routing_number | bik_code | transaction | bank_name | sepa_ref | creditor_ref | account_type | transaction_type | transaction_description | ifsc_code | bsb_code | check_number | micr_line | payment_reference | account_number | account_number_masked";
    }
}
