package com.mockjutsu.jmeter.generators;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
        String sym   = SYMBOLS[rng.nextInt(SYMBOLS.length)];
        int    bars  = 1 + rng.nextInt(5); // generate a short series, return last bar
        double prevClose = 100 + rng.nextDouble(49900);
        double open = prevClose, high = prevClose, low = prevClose, close = prevClose;
        long   vol  = rng.nextLong(100000, 10000000);
        for (int i = 0; i < bars; i++) {
            open  = prevClose;
            close = open * (1 + rng.nextDouble(-0.04, 0.04));
            high  = Math.max(open, close) * (1 + rng.nextDouble(0.001, 0.02));
            low   = Math.min(open, close) * (1 - rng.nextDouble(0.001, 0.02));
            vol   = rng.nextLong(100000, 10000000);
            prevClose = close;
        }
        String ts = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return String.format("{\"symbol\":\"%s\",\"interval\":\"1h\",\"open\":%.4f,\"high\":%.4f,\"low\":%.4f,\"close\":%.4f,\"volume\":%d,\"ts\":\"%s\"}",
            sym, open, high, low, close, vol, ts);
    }

    private static String marketTick(ThreadLocalRandom rng) {
        String sym  = SYMBOLS[rng.nextInt(SYMBOLS.length)];
        double price = 100 + rng.nextDouble(49900);
        double change = rng.nextDouble(-5, 5);
        long   vol   = rng.nextLong(1000, 1000000);
        String ts = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return String.format("{\"symbol\":\"%s\",\"price\":%.4f,\"change_pct\":%.2f,\"bid\":%.4f,\"ask\":%.4f,\"volume\":%d,\"ts\":\"%s\"}",
            sym, price, change, price * 0.9999, price * 1.0001, vol, ts);
    }
}
