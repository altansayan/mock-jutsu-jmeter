package com.mockjutsu.jmeter;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

import java.util.Collection;
import java.util.List;

public abstract class MockJutsuBaseFunction extends AbstractFunction {

    private CompoundVariable[] params = new CompoundVariable[0];

    protected abstract String typeDescription();

    @Override
    public String execute(SampleResult prev, Sampler current) throws InvalidVariableException {
        String type    = params.length > 0 ? params[0].execute().trim().toLowerCase() : "";
        String locale  = params.length > 1 ? params[1].execute().trim().toUpperCase() : "TR";
        String varName = params.length > 2 ? params[2].execute().trim() : "";
        if (locale.isEmpty()) locale = "TR";
        String result = MockJutsuRegistry.generate(type, locale);
        if (!varName.isEmpty()) {
            JMeterVariables vars = getVariables();
            if (vars != null) vars.put(varName, result);
        }
        return result;
    }

    @Override
    public List<String> getArgumentDesc() {
        return List.of(
            "type — " + typeDescription(),
            "locale — TR | DE | FR | UK | US | RU (optional, default TR)",
            "varName — store result in a JMeter variable (optional)"
        );
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 3);
        params = parameters.toArray(new CompoundVariable[0]);
    }
}
