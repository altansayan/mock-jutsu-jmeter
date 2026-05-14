package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuIdentityFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_identity"; }
    @Override protected String typeDescription() {
        return "tckn | ykn | taxid | vkn | nationalid | ssn | nin | inn | inn_individual | snils | sgk | mersis | ein | utr | crn | paye | ust_id | ustid | hrb | rvn | siren | siret | tva | ogrn | kpp | employer_id | insurance_id | firstname | lastname | fullname | patronymic | passport | license | age | gender | birthdate | tckn_masked | ssn_masked | nationality | vat_number | cardowner";
    }
}
