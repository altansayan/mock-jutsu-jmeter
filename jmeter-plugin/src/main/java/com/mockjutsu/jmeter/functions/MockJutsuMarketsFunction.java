package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuMarketsFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_markets"; }
    @Override protected String typeDescription() {
        return "isin | cusip | sedol | lei | fix_message | psd2_consent[:amount] | figi | nsin | stock_ticker | forex_pair | forex_rate[:EURUSD|USDTRY|...] | ric | mic | stock_exchange | option_contract | bond_yield | coupon_rate | settlement_date | portfolio_id | portfolio_id_masked";
    }
}
