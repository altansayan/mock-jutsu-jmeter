package com.mockjutsu.jmeter.functions;

import com.mockjutsu.jmeter.MockJutsuBaseFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies every category function: correct key prefix, 3-param description, non-empty typeDescription. */
class CategoryFunctionTest {

    static Stream<MockJutsuBaseFunction> allFunctions() {
        return Stream.of(
            new MockJutsuIdentityFunction(),
            new MockJutsuFinancialFunction(),
            new MockJutsuCommFunction(),
            new MockJutsuMetaFunction(),
            new MockJutsuBankingFunction(),
            new MockJutsuCorporateFunction(),
            new MockJutsuHealthFunction(),
            new MockJutsuCommerceFunction(),
            new MockJutsuIoTFunction(),
            new MockJutsuBarcodeFunction(),
            new MockJutsuTelecomFunction(),
            new MockJutsuMarketsFunction(),
            new MockJutsuCryptoFunction(),
            new MockJutsuEcommerceFunction(),
            new MockJutsuLocationFunction(),
            new MockJutsuSocialFunction(),
            new MockJutsuHardwareFunction(),
            new MockJutsuCardPhysicsFunction(),
            new MockJutsuSecurityFunction(),
            new MockJutsuAviationFunction(),
            new MockJutsuFido2Function(),
            new MockJutsuWalletFunction(),
            new MockJutsuAiFunction(),
            new MockJutsuOidcFunction(),
            new MockJutsuBankStatementFunction(),
            new MockJutsuEdiFunction(),
            new MockJutsuEventSourcingFunction(),
            new MockJutsuTelemetryFunction(),
            new MockJutsuCryptoFuzzFunction(),
            new MockJutsuMrzFunction(),
            new MockJutsuOhlcvFunction(),
            new MockJutsuNmeaFunction(),
            new MockJutsuPrometheusFunction(),
            new MockJutsuGameDevFunction(),
            new MockJutsuUblFunction(),
            new MockJutsuAutomotiveFunction(),
            new MockJutsuTleFunction(),
            new MockJutsuPaymentsFunction(),
            new MockJutsuRegexFunction(),
            new MockJutsuIntlIdsFunction(),
            new MockJutsuComplianceFunction(),
            new MockJutsuFinancialExtFunction(),
            new MockJutsuDateTimeFunction()
        );
    }

    @ParameterizedTest
    @MethodSource("allFunctions")
    void keyStartsWithMockjutsuUnderscore(MockJutsuBaseFunction fn) {
        String key = fn.getReferenceKey();
        assertTrue(key.startsWith("__mockjutsu_"),
            key + " must start with __mockjutsu_");
    }

    @ParameterizedTest
    @MethodSource("allFunctions")
    void typeDescriptionNonEmpty(MockJutsuBaseFunction fn) {
        // getArgumentDesc()[0] contains "type — <types>"; params 1-3 are locale/varName/mask
        List<String> desc = fn.getArgumentDesc();
        assertFalse(desc.isEmpty(), fn.getReferenceKey() + " must have at least 1 param description");
        String typeDesc = desc.get(0);
        assertTrue(typeDesc.contains("|") || typeDesc.contains("type"),
            fn.getReferenceKey() + " typeDescription must list types");
    }

    @ParameterizedTest
    @MethodSource("allFunctions")
    void keysAreUnique(MockJutsuBaseFunction fn) {
        // Each key must not duplicate __mockjutsu (the generic one)
        assertNotEquals("__mockjutsu", fn.getReferenceKey());
    }

    @Test
    void totalFunctionCount() {
        assertEquals(43, allFunctions().count(), "Must have exactly 43 category functions");
    }
}
