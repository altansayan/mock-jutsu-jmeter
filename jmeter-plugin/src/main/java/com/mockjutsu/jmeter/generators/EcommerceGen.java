package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class EcommerceGen {
    private EcommerceGen() {}
    private static final String[] PRODUCTS  = {"Wireless Headphones","Smart Watch","Laptop Stand","USB-C Hub","Mechanical Keyboard","Monitor Arm","Webcam","Mouse Pad XL","RGB Mouse","SSD 1TB"};
    private static final String[] CATEGORIES= {"Electronics","Clothing","Books","Sports","Home","Beauty","Toys","Garden","Automotive","Office"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "product_name"   -> PRODUCTS[rng.nextInt(PRODUCTS.length)];
            case "sku"            -> "SKU-" + String.format("%04d", rng.nextInt(1000,9999)) + "-" + String.format("%04d", rng.nextInt(1000,9999));
            case "order_id"       -> "ORD-" + String.format("%010d", rng.nextLong(1000000000L, 9999999999L));
            case "tracking_number"-> "1Z" + MetaGen.randomHex(rng, 16).toUpperCase();
            case "category"       -> CATEGORIES[rng.nextInt(CATEGORIES.length)];
            case "rating"         -> String.format("%.1f", 1.0 + rng.nextDouble(4));
            case "dhl_tracking"   -> String.format("%010d", rng.nextLong(1000000000L, 9999999999L));
            default -> "ERROR: Unknown ecommerce type '" + type + "'";
        };
    }
}
