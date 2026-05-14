package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class LocationGen {
    private LocationGen() {}
    private static final String[] TIMEZONES = {"UTC","Europe/Istanbul","America/New_York","Europe/Berlin","Europe/Paris","Europe/London","Europe/Moscow"};
    private static final String[] COUNTRY_CODES = {"TR","US","DE","FR","GB","RU","JP","CA","AU","CN","BR","IN","IT","ES","NL"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double lat = -90  + rng.nextDouble(180);
        double lon = -180 + rng.nextDouble(360);
        return switch (type) {
            case "latitude"     -> String.format("%.6f", lat);
            case "longitude"    -> String.format("%.6f", lon);
            case "timezone"     -> TIMEZONES[rng.nextInt(TIMEZONES.length)];
            case "country_code" -> COUNTRY_CODES[rng.nextInt(COUNTRY_CODES.length)];
            case "coordinates"  -> String.format("%.6f,%.6f", lat, lon);
            default -> "ERROR: Unknown location type '" + type + "'";
        };
    }
}
