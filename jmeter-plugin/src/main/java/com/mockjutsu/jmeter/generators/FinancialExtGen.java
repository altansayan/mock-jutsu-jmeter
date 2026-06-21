package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** Extended financial attributes — credit, insurance, loan. Mirrors financial_ext.py. */
public final class FinancialExtGen {

    private FinancialExtGen() {}

    private static final String[] CREDIT_MODELS   = {"FICO","VantageScore","Equifax","Experian","TransUnion","ClearScore","CreditKarma"};
    private static final String[] CREDIT_TIERS    = {"Excellent","Good","Fair","Poor","Very Poor"};
    private static final String[] LOAN_TYPES       = {"Mortgage","Auto","Personal","Student","Business","Home Equity","Payday","Construction"};
    private static final String[] CLAIM_STATUSES   = {"Open","Pending","Approved","Denied","Under Review","Closed","Withdrawn"};
    private static final String[] ISSUERS          = {"Novex Premier","Apex Select","Zircon Elite","Orbit Infinite","Vertex Signature","Axmark Platinum"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "credit_score_model"    -> pick(rng, CREDIT_MODELS);
            case "credit_score_tier"     -> pick(rng, CREDIT_TIERS);
            case "credit_limit"          -> String.format(java.util.Locale.US, "%.2f", 1000.0 + rng.nextDouble(0, 49000.0));
            case "credit_utilization"    -> String.format(java.util.Locale.US, "%.2f", rng.nextDouble(0, 100.0));
            case "credit_card_issuer_name" -> pick(rng, ISSUERS);
            case "apr"                   -> String.format(java.util.Locale.US, "%.2f", 5.0 + rng.nextDouble(0, 30.0));
            case "loan_type"             -> pick(rng, LOAN_TYPES);
            case "mortgage_rate"         -> String.format(java.util.Locale.US, "%.2f", 1.5 + rng.nextDouble(0, 8.5));
            case "mortgage_term"         -> pick(rng, new String[]{"5","10","15","20","25","30"});
            case "premium_amount"        -> String.format(java.util.Locale.US, "%.2f", 50.0 + rng.nextDouble(0, 5000.0));
            case "deductible"            -> String.format(java.util.Locale.US, "%.2f", 250.0 + rng.nextDouble(0, 9750.0));
            case "coverage_limit"        -> String.format(java.util.Locale.US, "%.2f", 10000.0 + rng.nextDouble(0, 990000.0));
            case "claim_status"          -> pick(rng, CLAIM_STATUSES);
            default -> "ERROR: Unknown financial_ext type '" + type + "'";
        };
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }
}
