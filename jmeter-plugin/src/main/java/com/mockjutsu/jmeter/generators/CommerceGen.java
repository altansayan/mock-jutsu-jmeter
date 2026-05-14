package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class CommerceGen {
    private CommerceGen() {}

    private static final String[] CURRENCIES = {
        "USD","EUR","GBP","TRY","JPY","CAD","CHF","AUD","CNY","SEK","NOK","RUB","BRL","INR","MXN"
    };
    private static final String[] VEHICLE_MAKES = {"Toyota","Honda","Ford","BMW","Mercedes-Benz","Volkswagen","Hyundai","Kia","Renault","Peugeot"};
    private static final String[] VEHICLE_MODELS = {"Sedan","SUV","Hatchback","Pickup","Coupe","Minivan","Crossover"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "currency"                        -> pick(rng, CURRENCIES);
            case "tax_rate","taxrate"              -> String.format("%.1f%%", rng.nextDouble(5, 25));
            case "invoice_number","invoicenumber"  -> "INV-" + String.format("%08d", rng.nextInt(10000000, 99999999));
            case "vin"                             -> vin(rng);
            case "vehicle"                         -> pick(rng, VEHICLE_MAKES) + " " + pick(rng, VEHICLE_MODELS);
            default -> "ERROR: Unknown commerce type '" + type + "'";
        };
    }

    // VIN — ISO 3779, 17 chars (no I, O, Q), positions 9=check, 10=model year
    private static String vin(ThreadLocalRandom rng) {
        String vinChars = "ABCDEFGHJKLMNPRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 17; i++) {
            if (i == 8) { sb.append(rng.nextInt(0,10)); continue; } // check digit placeholder
            if (i == 9) { sb.append("ABCDEFGHJKLMNPRSTUVWXYZ".charAt(rng.nextInt(23))); continue; } // model year
            sb.append(vinChars.charAt(rng.nextInt(vinChars.length())));
        }
        return sb.toString();
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) { return arr[rng.nextInt(arr.length)]; }
}
