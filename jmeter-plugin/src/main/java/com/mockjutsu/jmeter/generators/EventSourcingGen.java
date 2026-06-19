package com.mockjutsu.jmeter.generators;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class EventSourcingGen {
    private EventSourcingGen() {}
    private static final String[] EVENT_TYPES = {"UserCreated","OrderPlaced","PaymentProcessed","AccountUpdated","SessionStarted","TransactionCompleted"};
    private static final String[] CDC_OPS = {"INSERT","UPDATE","DELETE"};
    private static final String[] CDC_TABLES = {"users","orders","payments","accounts","sessions","transactions"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "event_stream" -> eventStream(rng);
            case "cdc_event"    -> cdcEvent(rng);
            default -> "ERROR: Unknown event sourcing type '" + type + "'";
        };
    }

    private static String eventStream(ThreadLocalRandom rng) {
        String eventType = EVENT_TYPES[rng.nextInt(EVENT_TYPES.length)];
        String eventId   = UUID.randomUUID().toString();
        String aggId     = UUID.randomUUID().toString();
        return "{\"eventId\":\"" + eventId + "\",\"eventType\":\"" + eventType + "\"," +
               "\"aggregate_id\":\"" + aggId + "\",\"aggregate_type\":\"User\",\"version\":" + rng.nextInt(1,100) + "," +
               "\"timestamp\":\"" + java.time.Instant.now() + "\"," +
               "\"payload\":{\"id\":\"" + aggId + "\",\"status\":\"active\"}}";
    }

    private static String cdcEvent(ThreadLocalRandom rng) {
        String op    = CDC_OPS[rng.nextInt(CDC_OPS.length)];
        String table = CDC_TABLES[rng.nextInt(CDC_TABLES.length)];
        String id    = UUID.randomUUID().toString();
        return "{\"op\":\"" + op + "\",\"ts_ms\":" + System.currentTimeMillis() + "," +
               "\"source\":{\"db\":\"mockdb\",\"table\":\"" + table + "\"}," +
               "\"before\":" + (op.equals("INSERT") ? "null" : "{\"id\":\"" + id + "\"}") + "," +
               "\"after\":" + (op.equals("DELETE") ? "null" : "{\"id\":\"" + id + "\",\"updated_at\":\"" + java.time.Instant.now() + "\"}") + "}";
    }
}
