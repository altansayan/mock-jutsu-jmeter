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

    /**
     * Parameter convention:
     *   1 param : ${__mockjutsu(tckn)}                     → type only
     *   2 params: ${__mockjutsu(tckn,TR)}                  → type, locale
     *   3 params: ${__mockjutsu(tckn,TR,myVar)}            → type, locale, varName  (original API)
     *   4+ params: ${__mockjutsu(tckn,iban,cardnum,uuid,,)} → types..., locale, varName
     *              last = varName, second-to-last = locale, rest = types → JSON object output
     */
    @Override
    public String execute(SampleResult prev, Sampler current) throws InvalidVariableException {
        int n = params.length;

        String[] types;
        String locale;
        String varName;

        if (n <= 3) {
            // Original single-type API — fully backward compatible
            types   = new String[]{ n > 0 ? params[0].execute().trim().toLowerCase() : "" };
            locale  = n > 1 ? params[1].execute().trim().toUpperCase() : "";
            varName = n > 2 ? params[2].execute().trim() : "";
        } else {
            // Multi-type: last two params are locale and varName, rest are types
            varName = params[n - 1].execute().trim();
            locale  = params[n - 2].execute().trim().toUpperCase();
            types   = new String[n - 2];
            for (int i = 0; i < n - 2; i++)
                types[i] = params[i].execute().trim().toLowerCase();
        }

        if (locale.isEmpty()) locale = "TR";

        String result;
        if (types.length == 1) {
            result = MockJutsuRegistry.generate(types[0], locale);
        } else {
            StringBuilder sb = new StringBuilder("{");
            JMeterVariables vars = varName.isEmpty() ? null : getVariables();
            for (int i = 0; i < types.length; i++) {
                String val = MockJutsuRegistry.generate(types[i], locale);
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
            "type — " + typeDescription(),
            "locale — TR | DE | FR | UK | US | RU (optional, default TR)",
            "varName — store result in a JMeter variable (optional)"
        );
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 255);
        params = parameters.toArray(new CompoundVariable[0]);
    }
}
