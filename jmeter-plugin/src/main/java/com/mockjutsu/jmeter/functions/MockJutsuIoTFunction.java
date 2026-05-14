package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuIoTFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_iot"; }
    @Override protected String typeDescription() {
        return "rfid_uid | epc | rfid_tag | nfc_uid | nfc_atqa | nfc_sak | ndef_uri | ndef_text | apdu | nfc_tag | ir_nec | ir_rc5 | ir_pronto | ir_raw | mqtt_payload | lora_packet";
    }
}
