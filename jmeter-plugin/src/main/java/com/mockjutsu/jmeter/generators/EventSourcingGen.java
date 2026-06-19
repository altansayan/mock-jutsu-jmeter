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

    private static final String[] AGG_TYPES = {"User","Order","Payment","Account","Session","Transaction"};

    private static String eventStream(ThreadLocalRandom rng) {
        // Return JSON array mirroring event_sourcing.py (multiple events per stream)
        int count = 3 + rng.nextInt(5);
        String aggId   = UUID.randomUUID().toString();
        String aggType = "User";
        String corrId  = UUID.randomUUID().toString();
        String sessId  = UUID.randomUUID().toString();
        String userId  = UUID.randomUUID().toString();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            String eventType = EVENT_TYPES[rng.nextInt(EVENT_TYPES.length)];
            String eventId   = UUID.randomUUID().toString();
            sb.append("{\"event_id\":\"").append(eventId).append("\"");
            sb.append(",\"aggregate_id\":\"").append(aggId).append("\"");
            sb.append(",\"aggregate_type\":\"").append(aggType).append("\"");
            sb.append(",\"correlation_id\":\"").append(corrId).append("\"");
            sb.append(",\"session_id\":\"").append(sessId).append("\"");
            sb.append(",\"user_id\":\"").append(userId).append("\"");
            sb.append(",\"timestamp\":\"").append(java.time.Instant.now()).append("\"");
            sb.append(",\"event_type\":\"").append(eventType).append("\"");
            sb.append(",\"payload\":{\"id\":\"").append(aggId).append("\",\"status\":\"active\"}}");
        }
        sb.append("]");
        return sb.toString();
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
