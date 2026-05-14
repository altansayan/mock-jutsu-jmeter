package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuCommerceFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_commerce"; }
    @Override protected String typeDescription() {
        return "currency | tax_rate | taxrate | invoice_number | invoicenumber | vin | vehicle";
    }
}
