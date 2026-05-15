package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class EcommerceGen {
    private EcommerceGen() {}

    private static final String[] PRODUCTS   = {
        "Wireless Headphones","Smart Watch","Laptop Stand","USB-C Hub","Mechanical Keyboard",
        "Monitor Arm","Webcam","Mouse Pad XL","RGB Mouse","SSD 1TB"
    };
    private static final String[] CATEGORIES = {
        "Electronics","Clothing","Books","Sports","Home","Beauty","Toys","Garden","Automotive","Office"
    };
    private static final String SKU_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // USPS tracking prefixes (2-letter service codes)
    private static final String[] USPS_PREFIXES = {"94","93","92","91","82"};

    // Rating weighted distribution: weights for 1.0..5.0 in 0.5 steps
    // Python: weights=[1,2,3,4,8,12,20,25,25] → values=["1.0","1.5","2.0","2.5","3.0","3.5","4.0","4.5","5.0"]
    private static final double[] RATING_VALUES  = {1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};
    private static final int[]    RATING_WEIGHTS = {1,   2,   3,   4,   8,   12,  20,  25,  25};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "product_name"    -> pick(rng, PRODUCTS);
            case "sku"             -> sku(rng);
            case "order_id"        -> orderId(rng);
            case "tracking_number" -> uspsTracking(rng);
            case "category"        -> pick(rng, CATEGORIES);
            case "rating"          -> rating(rng);
            case "dhl_tracking"    -> dhlTracking(rng);
            default -> "ERROR: Unknown ecommerce type '" + type + "'";
        };
    }

    // SKU: {2-4 uppercase letters}-{4-8 digits}
    private static String sku(ThreadLocalRandom rng) {
        int letterLen = 2 + rng.nextInt(3); // 2-4
        int digitLen  = 4 + rng.nextInt(5); // 4-8
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < letterLen; i++) sb.append(SKU_ALPHA.charAt(rng.nextInt(26)));
        sb.append('-');
        for (int i = 0; i < digitLen; i++) sb.append(rng.nextInt(10));
        return sb.toString();
    }

    // order_id: ORD-{8-12 alphanumeric, CSPRNG}
    private static String orderId(ThreadLocalRandom rng) {
        int len = 8 + rng.nextInt(5); // 8-12
        StringBuilder sb = new StringBuilder("ORD-");
        for (int i = 0; i < len; i++) sb.append(ORD_CHARS.charAt(rng.nextInt(ORD_CHARS.length())));
        return sb.toString();
    }

    // USPS tracking: {prefix(2)}{19 digits}{Luhn check digit} = 22 chars
    private static String uspsTracking(ThreadLocalRandom rng) {
        String prefix = USPS_PREFIXES[rng.nextInt(USPS_PREFIXES.length)];
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 19; i++) sb.append(rng.nextInt(10));
        sb.append(luhnCheck(sb.toString()));
        return sb.toString();
    }

    // DHL: JD{8 digits}{Luhn check digit}
    private static String dhlTracking(ThreadLocalRandom rng) {
        StringBuilder sb = new StringBuilder("JD");
        for (int i = 0; i < 8; i++) sb.append(rng.nextInt(10));
        sb.append(luhnCheck(sb.toString()));
        return sb.toString();
    }

    // Weighted rating
    private static String rating(ThreadLocalRandom rng) {
        int total = 0;
        for (int w : RATING_WEIGHTS) total += w;
        int r = rng.nextInt(total);
        int cum = 0;
        for (int i = 0; i < RATING_WEIGHTS.length; i++) {
            cum += RATING_WEIGHTS[i];
            if (r < cum) return String.format("%.1f", RATING_VALUES[i]);
        }
        return "5.0";
    }

    // Luhn check digit
    private static int luhnCheck(String digits) {
        int sum = 0;
        boolean alt = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (alt) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
            alt = !alt;
        }
        return (10 - (sum % 10)) % 10;
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) { return arr[rng.nextInt(arr.length)]; }
}
