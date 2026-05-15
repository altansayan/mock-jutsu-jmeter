package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class AviationGen {
    private AviationGen() {}
    private static final String[] AIRPORTS = {"IST","SAW","ESB","ADB","LHR","CDG","FRA","JFK","SVO","DXB","NRT"};
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
        // IATA ticket: 3-digit numeric airline code (001-999) + 10-digit serial = 13 chars total
        // Check digit: last digit of serial = sum-of-first-9-digits mod 7
        String airlineCode = String.format("%03d", rng.nextInt(1, 1000));
        int[] serial = new int[9];
        for (int i = 0; i < 9; i++) serial[i] = rng.nextInt(0, 10);
        int sum = 0;
        for (int d : serial) sum += d;
        int checkDigit = sum % 7;
        StringBuilder sb = new StringBuilder(airlineCode);
        for (int d : serial) sb.append(d);
        sb.append(checkDigit);
        return sb.toString();
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
