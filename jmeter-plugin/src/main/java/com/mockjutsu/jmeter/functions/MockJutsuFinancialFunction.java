package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuFinancialFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_financial"; }
    @Override protected String typeDescription() {
        return "cardnum | cardnetwork | cardtype | cardstatus | cvv3 | cvv4 | issuer | expiry | expirymonth | expiryyear | pin | balance | iban | cardcategory | credit_score | sepa_qr | emv_qr_p2p | emv_qr_atm | emv_qr_pos | 3ds_cavv | 3ds_eci";
    }
}
