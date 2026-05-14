package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuHardwareFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_hardware"; }
    @Override protected String typeDescription() {
        return "track1_data | track2_data | chip_data | pin_block | pin_block_fmt3";
    }
}
