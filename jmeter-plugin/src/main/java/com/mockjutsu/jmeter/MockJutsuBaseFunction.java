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
        // params[0] = type(s) — single or comma-separated, e.g. "tckn,iban,cardnum"
        // params[1] = locale  (optional, default TR)
        // params[2] = varName (optional)
        // params[3] = mask    (optional, "true"/"false", default false)
        String rawType = n > 0 ? params[0].execute().trim() : "";
        String locale  = n > 1 ? params[1].execute().trim().toUpperCase() : "TR";
        String varName = n > 2 ? params[2].execute().trim() : "";
        boolean mask   = n > 3 && isTruthy(params[3].execute().trim());
        if (locale.isEmpty()) locale = "TR";

        String[] parts     = rawType.split(",");
        String[] types     = new String[parts.length];
        String[] qualifiers = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part    = parts[i].trim().toLowerCase();
            int colonIdx   = part.indexOf(':');
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
            String raw = MockJutsuRegistry.generate(types[0], locale, qualifiers[0]);
            result = mask ? MaskerUtil.mask(types[0], raw) : raw;
        } else {
            StringBuilder sb   = new StringBuilder("{");
            JMeterVariables vars = varName.isEmpty() ? null : getVariables();
            for (int i = 0; i < types.length; i++) {
                String raw = MockJutsuRegistry.generate(types[i], locale, qualifiers[i]);
                String val = mask ? MaskerUtil.mask(types[i], raw) : raw;
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

    /** Accepts "true", "1", "yes" (case-insensitive) as truthy. */
    private static boolean isTruthy(String s) {
        return "true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s);
    }

    /** Wraps scalar values in JSON quotes; passes through values that are already JSON. */
    private static String toJsonValue(String val) {
        if (val.startsWith("{") || val.startsWith("[")) return val;
        return "\"" + val.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    @Override
    public List<String> getArgumentDesc() {
        return List.of(
            "type[:qualifier][,type2...] — " + typeDescription(),
            "locale (TR/UK/US/DE/FR/RU) — optional, default TR",
            "varName — optional JMeter variable name to store result",
            "mask (true/false) — optional, default false"
        );
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 4);
        params = parameters.toArray(new CompoundVariable[0]);
    }
}
