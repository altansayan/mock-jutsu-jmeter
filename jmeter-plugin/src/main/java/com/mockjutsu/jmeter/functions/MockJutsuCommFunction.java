package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuCommFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_comm"; }
    @Override protected String typeDescription() {
        return "phone | phone_country | phone_area | phone_local | address_city | address_street | address_full | postalcode | plate | email";
    }
}
