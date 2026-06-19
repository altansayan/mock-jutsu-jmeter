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

    private static final String[] INTERVALS = {"1m","5m","15m","1h","4h","1d"};

    private static String ohlcvCandles(ThreadLocalRandom rng) {
        // Mirrors ohlcv.py: {symbol, interval, candles: [{t, o, h, l, c, v}]}
        String sym      = SYMBOLS[rng.nextInt(SYMBOLS.length)];
        String interval = INTERVALS[rng.nextInt(INTERVALS.length)];
        int    bars     = 5 + rng.nextInt(11);
        double prevClose = 100 + rng.nextDouble(49900);
        StringBuilder candlesSb = new StringBuilder("[");
        for (int i = 0; i < bars; i++) {
            if (i > 0) candlesSb.append(",");
            double o = prevClose;
            double c = o * (1 + rng.nextDouble(-0.04, 0.04));
            double h = Math.max(o, c) * (1 + rng.nextDouble(0.001, 0.02));
            double l = Math.min(o, c) * (1 - rng.nextDouble(0.001, 0.02));
            long   v = rng.nextLong(100000, 10000000);
            String t = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes((bars - i) * 60L).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            candlesSb.append(String.format(java.util.Locale.US,
                "{\"t\":\"%s\",\"o\":%.4f,\"h\":%.4f,\"l\":%.4f,\"c\":%.4f,\"v\":%d}", t, o, h, l, c, v));
            prevClose = c;
        }
        candlesSb.append("]");
        return "{\"symbol\":\"" + sym + "\",\"interval\":\"" + interval + "\",\"candles\":" + candlesSb + "}";
    }

    private static final String[] SIDES = {"buy","sell"};

    private static String marketTick(ThreadLocalRandom rng) {
        // Mirrors ohlcv.py: {symbol, timestamp, price, size, bid, ask, side, seq}
        String sym  = SYMBOLS[rng.nextInt(SYMBOLS.length)];
        double price = 100 + rng.nextDouble(49900);
        long   size  = rng.nextLong(1, 1000);
        String ts    = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String side  = SIDES[rng.nextInt(SIDES.length)];
        long   seq   = rng.nextLong(100000, 9999999999L);
        return String.format(java.util.Locale.US,
            "{\"symbol\":\"%s\",\"timestamp\":\"%s\",\"price\":%.2f,\"size\":%d," +
            "\"bid\":%.2f,\"ask\":%.2f,\"side\":\"%s\",\"seq\":%d}",
            sym, ts, price, size, price * 0.9999, price * 1.0001, side, seq);
    }
}
