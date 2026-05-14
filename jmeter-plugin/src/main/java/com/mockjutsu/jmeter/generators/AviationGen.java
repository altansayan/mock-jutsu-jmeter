package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class AviationGen {
    private AviationGen() {}
    private static final String[] AIRPORTS = {"IST","SAW","ESB","ADB","LHR","CDG","FRA","JFK","SVO","DXB","NRT"};
    private static final String[] IATA_CARRIERS = {"TK","PC","AJ","BA","AF","LH","UA","DL","AA","EK","SU"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "iata_ticket"  -> iataTicket(rng);
            case "imo_number"   -> imoNumber(rng);
            case "pnr_code"     -> pnr(rng);
            default -> "ERROR: Unknown aviation type '" + type + "'";
        };
    }

    private static String iataTicket(ThreadLocalRandom rng) {
        String carrier = IATA_CARRIERS[rng.nextInt(IATA_CARRIERS.length)];
        String origin  = AIRPORTS[rng.nextInt(AIRPORTS.length)];
        String dest    = AIRPORTS[rng.nextInt(AIRPORTS.length)];
        return carrier + String.format("%010d", rng.nextLong(1000000000L, 9999999999L)) +
               "/" + origin + "-" + dest;
    }

    // IMO: 7 digits with check digit (weighted sum mod 10)
    private static String imoNumber(ThreadLocalRandom rng) {
        int[] d = new int[6];
        for (int i = 0; i < 6; i++) d[i] = rng.nextInt(0,10);
        int sum = 0;
        int[] w = {7,6,5,4,3,2};
        for (int i = 0; i < 6; i++) sum += d[i] * w[i];
        int check = sum % 10;
        StringBuilder sb = new StringBuilder("IMO");
        for (int v : d) sb.append(v);
        sb.append(check);
        return sb.toString();
    }

    private static String pnr(ThreadLocalRandom rng) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }
}
