package com.mockjutsu.jmeter.generators;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Compliance / AML / KYC data generator. Mirrors compliance.py. */
public final class ComplianceGen {

    private ComplianceGen() {}

    private static final String[] PEP_STATUSES      = {"PEP","RCA","No","Unknown"};
    private static final String[] AML_RATINGS        = {"Low","Medium","High","Critical"};
    private static final String[] CDD_LEVELS         = {"Standard","Enhanced","Simplified"};
    private static final String[] KYC_DOC_TYPES      = {"Passport","National ID","Driver License","Utility Bill","Bank Statement","Tax Document","Residence Permit"};
    private static final String[] ONBOARDING_METHODS = {"Online","Branch","Agent","Mobile","Partner","API","Video Call"};
    private static final String[] SAR_PREFIXES       = {"SAR","SUS","AML","FIU"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "policy_number"         -> policyNumber(rng);
            case "claim_number"          -> claimNumber(rng);
            case "pep_status"            -> pick(rng, PEP_STATUSES);
            case "aml_risk_rating"       -> pick(rng, AML_RATINGS);
            case "cdd_level"             -> pick(rng, CDD_LEVELS);
            case "sar_number"            -> sarNumber(rng);
            case "ubo_ownership_percentage" -> String.format(java.util.Locale.US, "%.2f", 10.0 + rng.nextDouble(0, 90.0));
            case "kyc_document_type"     -> pick(rng, KYC_DOC_TYPES);
            case "consent_id"            -> "CONSENT-" + UUID.randomUUID().toString().substring(0, 18).toUpperCase();
            case "tpp_id"                -> "TPP-" + String.format("%08d", rng.nextInt(10000000, 99999999));
            case "onboarding_method"     -> pick(rng, ONBOARDING_METHODS);
            case "sanctions_hit"         -> rng.nextInt(10) == 0 ? "Yes" : "No";
            case "sanctions_hit_masked"  -> "***";
            default                      -> "ERROR: Unknown compliance type '" + type + "'";
        };
    }

    private static String policyNumber(ThreadLocalRandom rng) {
        return "POL-" + LocalDate.now() + "-" + String.format("%05d", rng.nextInt(10000, 99999));
    }

    private static String claimNumber(ThreadLocalRandom rng) {
        return "CLM-" + LocalDate.now().getYear() + "-" + String.format("%07d", rng.nextInt(1000000, 9999999));
    }

    private static String sarNumber(ThreadLocalRandom rng) {
        String prefix = SAR_PREFIXES[rng.nextInt(SAR_PREFIXES.length)];
        return prefix + "-" + LocalDate.now().getYear() + "-" + String.format("%06d", rng.nextInt(100000, 999999));
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }
}
