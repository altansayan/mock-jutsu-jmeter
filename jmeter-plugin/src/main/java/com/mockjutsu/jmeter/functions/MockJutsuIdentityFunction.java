package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuIdentityFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_identity"; }
    @Override protected String typeDescription() {
        return "tckn[:prefix] | ykn | taxid | vkn | nationalid | ssn | nin | inn | inn_individual | snils | sgk | mersis | ein | utr | crn | paye | ust_id | hrb | rvn | siren | siret | tva | ogrn | kpp | employer_id | insurance_id | firstname[:male|female] | lastname[:male|female] | fullname[:male|female] | patronymic[:male|female] | cardowner[:male|female] | passport | license | age[:min-max] | gender | birthdate | tckn_masked | ssn_masked | nationality | vat_number";
    }
}
