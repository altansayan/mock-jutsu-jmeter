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
import java.util.Set;

public abstract class MockJutsuBaseFunction extends AbstractFunction {

    private static final Set<String> LOCALES = Set.of("TR", "UK", "US", "DE", "FR", "RU");

    private CompoundVariable[] params = new CompoundVariable[0];

    protected abstract String typeDescription();

    @Override
    public String execute(SampleResult prev, Sampler current) throws InvalidVariableException {
        int n = params.length;
        String rawParam = n > 0 ? params[0].execute().trim() : "";

        // ─── Parse single-parameter: type[:qualifier][,type2...][,locale][,varName][,mask]
        //
        // Rules:
        //   • "mask" keyword (any position) → enables masking
        //   • known locale code (TR/UK/US/DE/FR/RU) → sets locale
        //   • tokens BEFORE the first locale/mask keyword → type names (bare or type:qualifier)
        //   • tokens AFTER the first locale/mask keyword → varName
        //
        // Examples (all in a single JMeter parameter field → no trailing commas ever):
        //   cardnum                          → type only
        //   cardnum:visa                     → type + qualifier
        //   cardnum:visa,mask                → masked
        //   cardnum:visa,TR                  → with locale
        //   cardnum:visa,TR,mask             → locale + masked
        //   cardnum:visa,TR,myVar            → locale + store in JMeter var
        //   cardnum:visa,TR,myVar,mask       → all options
        //   cardnum,iban,TR                  → multi-type with locale

        String[]      tokens   = rawParam.split(",", -1);
        List<String>  typeList = new ArrayList<>();
        List<String>  qualList = new ArrayList<>();
        String        locale   = "";
        String        varName  = "";
        boolean       mask     = false;
        boolean       seenSuffix = false;

        for (String tok : tokens) {
            tok = tok.trim();
            if (tok.isEmpty()) continue;

            String tokLower = tok.toLowerCase();
            String tokUpper = tok.toUpperCase();

            if ("mask".equals(tokLower)) {
                mask = true;
                seenSuffix = true;
            } else if (LOCALES.contains(tokUpper)) {
                locale = tokUpper;
                seenSuffix = true;
            } else if (!seenSuffix) {
                // Still in type-parsing zone
                int colon = tokLower.indexOf(':');
                if (colon >= 0) {
                    typeList.add(tokLower.substring(0, colon).trim());
                    qualList.add(tok.substring(colon + 1).trim()); // preserve qualifier case
                } else {
                    typeList.add(tokLower);
                    qualList.add("");
                }
            } else {
                // After first locale/mask → store as JMeter variable name
                varName = tok;
            }
        }

        if (locale.isEmpty()) locale = "TR";

        String[] types      = typeList.toArray(new String[0]);
        String[] qualifiers = qualList.toArray(new String[0]);

        // ─── Generate ────────────────────────────────────────────────────────
        String result;
        if (types.length == 0) {
            result = "ERROR: no type specified";
        } else if (types.length == 1) {
            String raw = MockJutsuRegistry.generate(types[0], locale, qualifiers[0]);
            result = mask ? MaskerUtil.mask(types[0], raw) : raw;
        } else {
            StringBuilder sb = new StringBuilder("{");
            for (int i = 0; i < types.length; i++) {
                String raw = MockJutsuRegistry.generate(types[i], locale, qualifiers[i]);
                String val = mask ? MaskerUtil.mask(types[i], raw) : raw;
                sb.append('"').append(types[i]).append("\":").append(toJsonValue(val));
                if (i < types.length - 1) sb.append(',');
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

    private static String toJsonValue(String val) {
        if (val.startsWith("{") || val.startsWith("[")) return val;
        return "\"" + val.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    @Override
    public List<String> getArgumentDesc() {
        return List.of(
            "type[:qualifier][,type2...][,locale][,varName][,mask] — " + typeDescription()
        );
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 1);
        params = parameters.toArray(new CompoundVariable[0]);
    }
}
