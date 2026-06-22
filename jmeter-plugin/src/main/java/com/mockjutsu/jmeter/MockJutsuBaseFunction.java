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

        // ─── Two usage modes (both produce clean output, no backslash escaping):
        //
        // A) Function Helper (1 JMeter param) → user types only the type:
        //      cardnum:visa   →  ${__mockjutsu_financial(cardnum:visa)}
        //
        // B) Manual / HOW-TO copy-paste (multiple JMeter params, comma = param separator):
        //      ${__mockjutsu_financial(cardnum:visa,mask)}       → param[0]=type, param[1]=mask
        //      ${__mockjutsu_financial(cardnum:visa,TR,mask)}    → param[0]=type, param[1]=TR, param[2]=mask
        //      ${__mockjutsu_financial(cardnum:visa,TR,myVar,mask)} → all four
        //
        // In mode B, JMeter treats each comma as a parameter separator (NOT escaped).
        // Mode A avoids trailing commas because getArgumentDesc() declares only 1 field.

        List<String>  typeList = new ArrayList<>();
        List<String>  qualList = new ArrayList<>();
        String        locale   = "";
        String        varName  = "";
        boolean       mask     = false;

        // ── Collect extra params (mode B: param[1..3] are locale/mask/varName) ──
        for (int i = 1; i < n; i++) {
            String p = params[i].execute().trim();
            if (p.isEmpty()) continue;
            if ("mask".equalsIgnoreCase(p))        mask   = true;
            else if (LOCALES.contains(p.toUpperCase())) locale = p.toUpperCase();
            else                                    varName = p;
        }

        // ── Parse typeSpec (param[0]): comma-separated type[:qualifier] tokens ──
        // "mask" keyword inside typeSpec also handled (mode A convenience)
        boolean seenSuffix = false;
        for (String tok : rawParam.split(",", -1)) {
            tok = tok.trim();
            if (tok.isEmpty()) continue;
            String tokLower = tok.toLowerCase();
            String tokUpper = tok.toUpperCase();
            if ("mask".equals(tokLower)) {
                mask = true; seenSuffix = true;
            } else if (LOCALES.contains(tokUpper)) {
                locale = tokUpper; seenSuffix = true;
            } else if (!seenSuffix) {
                int colon = tokLower.indexOf(':');
                if (colon >= 0) {
                    typeList.add(tokLower.substring(0, colon).trim());
                    qualList.add(tok.substring(colon + 1).trim());
                } else {
                    typeList.add(tokLower);
                    qualList.add("");
                }
            } else {
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
        checkParameterCount(parameters, 1, 4);
        params = parameters.toArray(new CompoundVariable[0]);
    }
}
