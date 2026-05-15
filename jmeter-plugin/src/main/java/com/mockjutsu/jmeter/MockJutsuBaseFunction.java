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
        String type    = params.length > 0 ? params[0].execute().trim() : "";
        String locale  = params.length > 1 ? params[1].execute().trim().toUpperCase() : "TR";
        String varName = params.length > 2 ? params[2].execute().trim() : "";
        if (locale.isEmpty()) locale = "TR";

        String result;
        if (type.contains("|")) {
            // Multi-type: ${__mockjutsu(tckn|iban|cardnum|uuid,,)} → {"tckn":"...","iban":"..."}
            String[] types = type.split("\\|");
            StringBuilder sb = new StringBuilder("{");
            JMeterVariables vars = varName.isEmpty() ? null : getVariables();
            for (int i = 0; i < types.length; i++) {
                String t   = types[i].trim().toLowerCase();
                String val = MockJutsuRegistry.generate(t, locale);
                sb.append('"').append(t).append("\":").append(toJsonValue(val));
                if (i < types.length - 1) sb.append(',');
                if (vars != null) vars.put(varName + "_" + t, val);
            }
            sb.append('}');
            result = sb.toString();
        } else {
            result = MockJutsuRegistry.generate(type.toLowerCase(), locale);
        }

        if (!varName.isEmpty()) {
            JMeterVariables vars = getVariables();
            if (vars != null) vars.put(varName, result);
        }
        return result;
    }

    /** Wraps scalar values in JSON quotes; passes through values that are already JSON. */
    private static String toJsonValue(String val) {
        if (val.startsWith("{") || val.startsWith("["))
            return val;
        return "\"" + val.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
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
