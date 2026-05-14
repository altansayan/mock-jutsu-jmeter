package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuSocialFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_social"; }
    @Override protected String typeDescription() {
        return "username | hashtag | bio | handle | follower_count";
    }
}
