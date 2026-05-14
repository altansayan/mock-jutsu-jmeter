package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuBarcodeFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_barcode"; }
    @Override protected String typeDescription() {
        return "ean13 | ean8 | upca | isbn13 | isbn10 | gs1_128";
    }
}
