package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class OhlcvGen {
    private OhlcvGen() {}
    private static final String[] SYMBOLS = {"BTCUSDT","ETHUSDT","AAPL","TSLA","MSFT","GOOGL","AMZN","META"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "ohlcv_candles" -> ohlcvCandles(rng);
            case "market_tick"   -> marketTick(rng);
            default -> "ERROR: Unknown OHLCV type '" + type + "'";
        };
    }

    private static String ohlcvCandles(ThreadLocalRandom rng) {
        String sym  = SYMBOLS[rng.nextInt(SYMBOLS.length)];
        double base = 100 + rng.nextDouble(49900);
        double open  = base;
        double high  = open  * (1 + rng.nextDouble(0.05));
        double low   = open  * (1 - rng.nextDouble(0.05));
        double close = low   + rng.nextDouble(high - low);
        long   vol   = rng.nextLong(100000, 10000000);
        return String.format("{\"symbol\":\"%s\",\"interval\":\"1h\",\"open\":%.4f,\"high\":%.4f,\"low\":%.4f,\"close\":%.4f,\"volume\":%d,\"ts\":%d}",
            sym, open, high, low, close, vol, System.currentTimeMillis());
    }

    private static String marketTick(ThreadLocalRandom rng) {
        String sym  = SYMBOLS[rng.nextInt(SYMBOLS.length)];
        double price = 100 + rng.nextDouble(49900);
        double change = rng.nextDouble(-5, 5);
        long   vol   = rng.nextLong(1000, 1000000);
        return String.format("{\"symbol\":\"%s\",\"price\":%.4f,\"change_pct\":%.2f,\"bid\":%.4f,\"ask\":%.4f,\"volume\":%d,\"ts\":%d}",
            sym, price, change, price * 0.9999, price * 1.0001, vol, System.currentTimeMillis());
    }
}
