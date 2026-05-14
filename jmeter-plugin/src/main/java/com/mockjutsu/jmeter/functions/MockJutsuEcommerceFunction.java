package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuEcommerceFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_ecommerce"; }
    @Override protected String typeDescription() {
        return "product_name | sku | order_id | tracking_number | category | rating | dhl_tracking";
    }
}
