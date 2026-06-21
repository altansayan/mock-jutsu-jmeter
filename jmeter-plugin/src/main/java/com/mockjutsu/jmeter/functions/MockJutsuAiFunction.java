package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuAiFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_ai"; }
    @Override protected String typeDescription() {
        return "ai_embedding[:dims] | ai_vector[:dims] | ai_sparse_vector[:dims|nnz]";
    }
}
