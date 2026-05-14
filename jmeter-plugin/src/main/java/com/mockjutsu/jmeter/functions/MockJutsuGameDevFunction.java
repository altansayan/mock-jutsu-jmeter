package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuGameDevFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_gamedev"; }
    @Override protected String typeDescription() {
        return "quaternion | navmesh_path";
    }
}
