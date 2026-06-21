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
        int n = params.length;
        // params[0] = type(s) — single or comma-separated list, e.g. "tckn, iban, cardnum"
        // params[1] = locale (optional, default TR)
        // params[2] = varName (optional)
        String rawType = n > 0 ? params[0].execute().trim() : "";
        String locale  = n > 1 ? params[1].execute().trim().toUpperCase() : "";
        String varName = n > 2 ? params[2].execute().trim() : "";
        if (locale.isEmpty()) locale = "TR";

        String[] parts = rawType.split(",");
        String[] types = new String[parts.length];
        String[] qualifiers = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim().toLowerCase();
            int colonIdx = part.indexOf(':');
            if (colonIdx >= 0) {
                types[i]      = part.substring(0, colonIdx).trim();
                qualifiers[i] = part.substring(colonIdx + 1).trim();
            } else {
                types[i]      = part;
                qualifiers[i] = "";
            }
        }

        String result;
        if (types.length == 1) {
            result = MockJutsuRegistry.generate(types[0], locale, qualifiers[0]);
        } else {
            StringBuilder sb = new StringBuilder("{");
            JMeterVariables vars = varName.isEmpty() ? null : getVariables();
            for (int i = 0; i < types.length; i++) {
                String val = MockJutsuRegistry.generate(types[i], locale, qualifiers[i]);
                sb.append('"').append(types[i]).append("\":").append(toJsonValue(val));
                if (i < types.length - 1) sb.append(',');
                if (vars != null) vars.put(varName + "_" + types[i], val);
            }
            sb.append('}');
            result = sb.toString();
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
            "type[:qualifier][,locale][,varName] — " + typeDescription()
        );
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 3);
        params = parameters.toArray(new CompoundVariable[0]);
    }
}
