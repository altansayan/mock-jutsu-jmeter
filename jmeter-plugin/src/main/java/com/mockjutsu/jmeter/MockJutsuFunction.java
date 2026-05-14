package com.mockjutsu.jmeter;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * JMeter custom function: ${__mockjutsu(type,locale,varName)}
 *
 * Install: copy mock-jutsu-jmeter-*.jar to $JMETER_HOME/lib/ext/ and restart JMeter.
 *
 * Examples:
 *   ${__mockjutsu(tckn)}              → 11-digit Turkish citizen ID
 *   ${__mockjutsu(iban,DE)}           → German IBAN
 *   ${__mockjutsu(cardnum,US,card)}   → US Visa card, also stored in ${card}
 *
 * Supported locales: TR | DE | FR | UK | US | RU  (default: TR)
 * All ~245 mock-jutsu types are supported except bulk and template.
 *
 * Developer: Altan Sezer Ayan — https://github.com/altansayan/mock-jutsu-jmeter
 */
public class MockJutsuFunction extends AbstractFunction {

    private static final String KEY = "__mockjutsu";
    private static final List<String> DESC = new LinkedList<>();

    static {
        DESC.add("type — data type to generate (required, e.g. tckn, iban, cardnum)");
        DESC.add("locale — TR | DE | FR | UK | US | RU (optional, default TR)");
        DESC.add("varName — store result in a JMeter variable (optional)");
    }

    private CompoundVariable[] params;

    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        String type    = params[0].execute().trim().toLowerCase();
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
    public void setParameters(Collection<CompoundVariable> parameters)
            throws InvalidVariableException {
        checkParameterCount(parameters, 1, 3);
        params = parameters.toArray(new CompoundVariable[0]);
    }

    @Override
    public String getReferenceKey() {
        return KEY;
    }

    @Override
    public List<String> getArgumentDesc() {
        return DESC;
    }
}
