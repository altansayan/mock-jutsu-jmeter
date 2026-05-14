package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuHealthFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_health"; }
    @Override protected String typeDescription() {
        return "blood_type | bloodtype | nhs_number | nhsnumber | icd10 | height | weight | npi | bmi | hl7_message | fhir_patient | dicom_uid";
    }
}
