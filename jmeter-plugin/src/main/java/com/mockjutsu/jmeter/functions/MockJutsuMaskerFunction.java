package com.mockjutsu.jmeter.functions;

import com.mockjutsu.jmeter.MaskerUtil;
import com.mockjutsu.jmeter.ValidatorUtil;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

import java.util.Collection;
import java.util.List;

/**
 * JMeter EL function: {@code ${__mockjutsuMasker(type,value[,varName])}}.
 *
 * <p>Validates {@code value} for the given type using {@link ValidatorUtil};
 * returns the regulation-compliant masked form via {@link MaskerUtil} if valid,
 * or {@code "No Valid Data"} if validation fails.
 *
 * <p>Types without a registered validator are masked directly (no validation).
 *
 * <pre>
 *   ${__mockjutsuMasker(tckn,25123456794)}         → 25*******94
 *   ${__mockjutsuMasker(tckn,99999)}               → No Valid Data
 *   ${__mockjutsuMasker(iban,TR330006100519786457841326)}  → TR33 **** **** **** **** **13 26
 *   ${__mockjutsuMasker(tckn,25123456794,maskedVar)}  → stored in JMeter variable maskedVar
 * </pre>
 */
public final class MockJutsuMaskerFunction extends AbstractFunction {

    private static final String KEY = "__mockjutsuMasker";
    private static final String NO_VALID_DATA = "No Valid Data";

    private CompoundVariable[] params = new CompoundVariable[0];

    @Override
    public String getReferenceKey() {
        return KEY;
    }

    @Override
    public List<String> getArgumentDesc() {
        return List.of(
            "type  —  e.g. tckn | iban | cardnum | ssn | nin | bic | imei | …",
            "value  —  the real-world value to validate and mask",
            "varName  (optional)  —  JMeter variable to store the result"
        );
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 2, 3);
        params = parameters.toArray(new CompoundVariable[0]);
    }

    @Override
    public String execute(SampleResult prev, Sampler current) throws InvalidVariableException {
        String type    = params[0].execute().strip().toLowerCase();
        String value   = params[1].execute();
        String varName = params.length > 2 ? params[2].execute().strip() : "";

        Boolean isValid = ValidatorUtil.validate(type, value);

        String result;
        if (Boolean.FALSE.equals(isValid)) {
            result = NO_VALID_DATA;
        } else {
            // isValid == true  → valid; passed checksum
            // isValid == null  → no validator registered; mask directly
            result = MaskerUtil.mask(type, value);
        }

        if (!varName.isEmpty()) {
            JMeterVariables vars = getVariables();
            if (vars != null) vars.put(varName, result);
        }

        return result;
    }
}
