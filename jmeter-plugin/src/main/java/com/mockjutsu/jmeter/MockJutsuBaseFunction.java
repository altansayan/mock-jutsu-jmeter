package com.mockjutsu.jmeter;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class MockJutsuBaseFunction extends AbstractFunction {

    private CompoundVariable[] params = new CompoundVariable[0];

    protected abstract String typeDescription();

    @Override
    public String execute(SampleResult prev, Sampler current) throws InvalidVariableException {
        int n = params.length;

        // Evaluate all params upfront to scan for the "mask" keyword
        String[] ev = new String[n];
        for (int i = 0; i < n; i++) ev[i] = params[i].execute().trim();

        // "mask" keyword in ANY position triggers masking; remove it from positional list
        boolean mask = false;
        List<String> positional = new ArrayList<>();
        for (String p : ev) {
            if ("mask".equalsIgnoreCase(p)) {
                mask = true;
            } else {
                positional.add(p);
            }
        }
        // Legacy: params[3] = "true" / "1" / "yes" still works
        if (!mask && n > 3 && isTruthy(ev[3])) mask = true;

        String rawType = positional.size() > 0 ? positional.get(0) : "";
        String locale  = positional.size() > 1 ? positional.get(1).toUpperCase() : "TR";
        String varName = positional.size() > 2 ? positional.get(2) : "";
        if (locale.isEmpty()) locale = "TR";

        // Parse comma-separated types; "mask" keyword anywhere in the list sets mask flag
        String[] parts = rawType.split(",");
        List<String> typeList = new ArrayList<>();
        List<String> qualList = new ArrayList<>();
        for (String part : parts) {
            part = part.trim().toLowerCase();
            if (part.isEmpty()) continue;
            if ("mask".equals(part)) {
                mask = true;
                continue;
            }
            int colonIdx = part.indexOf(':');
            if (colonIdx >= 0) {
                typeList.add(part.substring(0, colonIdx).trim());
                qualList.add(part.substring(colonIdx + 1).trim());
            } else {
                typeList.add(part);
                qualList.add("");
            }
        }
        String[] types      = typeList.toArray(new String[0]);
        String[] qualifiers = qualList.toArray(new String[0]);

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
            "type[:qualifier][,type2...][,mask] — " + typeDescription(),
            "locale (TR/UK/US/DE/FR/RU) — optional",
            "varName — optional JMeter variable name to store result"
        );
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 3);
        params = parameters.toArray(new CompoundVariable[0]);
    }
}
