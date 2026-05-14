package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuWalletFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_wallet"; }
    @Override protected String typeDescription() {
        return "eth_wallet | btc_wallet | sol_wallet";
    }
}
