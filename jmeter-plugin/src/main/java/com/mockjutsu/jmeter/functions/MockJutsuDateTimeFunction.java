package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuDateTimeFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_datetime"; }
    @Override protected String typeDescription() {
        return "past_date | future_date | date_between | date_this_year | date_this_month | time_only | past_datetime | future_datetime";
    }
}
