package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuOhlcvFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_ohlcv"; }
    @Override protected String typeDescription() {
        return "ohlcv_candles | market_tick";
    }
}
