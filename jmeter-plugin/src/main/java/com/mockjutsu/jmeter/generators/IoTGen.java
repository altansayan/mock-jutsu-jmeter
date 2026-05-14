package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** IoT — RFID, NFC, IR, MQTT, LoRa. Mirrors iot.py. */
public final class IoTGen {
    private IoTGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "rfid_uid","rfid_tag" -> rfidUid(rng);
            case "epc"                 -> epc(rng);
            case "nfc_uid"             -> nfcUid(rng);
            case "nfc_atqa"            -> nfcAtqa(rng);
            case "nfc_sak"             -> nfcSak(rng);
            case "ndef_uri"            -> ndefUri(rng);
            case "ndef_text"           -> ndefText(rng);
            case "apdu"                -> apdu(rng);
            case "nfc_tag"             -> nfcTag(rng);
            case "ir_nec"              -> irNec(rng);
            case "ir_rc5"              -> irRc5(rng);
            case "ir_pronto"           -> irPronto(rng);
            case "ir_raw"              -> irRaw(rng);
            case "mqtt_payload"        -> mqttPayload(rng);
            case "lora_packet"         -> loraPacket(rng);
            default -> "ERROR: Unknown IoT type '" + type + "'";
        };
    }

    private static String rfidUid(ThreadLocalRandom rng) {
        // 4-byte (MIFARE Classic) or 7-byte UID
        int len = rng.nextBoolean() ? 4 : 7;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(':');
            sb.append(String.format("%02X", rng.nextInt(0, 256)));
        }
        return sb.toString();
    }

    private static String epc(ThreadLocalRandom rng) {
        // EPC SGTIN-96: header(8)+filter(3)+partition(3)+company(20)+item(24)+serial(38) bits → hex
        return String.format("30%014X%010X", rng.nextLong(0, 0xFFFFFFFFFFFFFFL), rng.nextLong(0, 0xFFFFFFFFFFL));
    }

    private static String nfcUid(ThreadLocalRandom rng) { return rfidUid(rng); }
    private static String nfcAtqa(ThreadLocalRandom rng) { return String.format("0x%04X", rng.nextInt(0, 65536)); }
    private static String nfcSak(ThreadLocalRandom rng) {
        int[] saks = {0x08, 0x18, 0x20, 0x28, 0x60};
        return String.format("0x%02X", saks[rng.nextInt(saks.length)]);
    }

    private static String ndefUri(ThreadLocalRandom rng) {
        String[] uris = {"https://example.com","https://mock-jutsu.io","https://api.test.com/v1/resource"};
        return uris[rng.nextInt(uris.length)];
    }

    private static String ndefText(ThreadLocalRandom rng) {
        String[] texts = {"MOCKJUTSU TEST TAG","Mock Data NFC","Test Record 001","NDEF Mock Text"};
        return texts[rng.nextInt(texts.length)];
    }

    private static String apdu(ThreadLocalRandom rng) {
        // SELECT FILE command: CLA INS P1 P2 Lc Data Le
        String[] cmds = {
            String.format("00A40000%02X%s00", 7, "A0000000041010"),
            String.format("00B0%02X%02X%02X", rng.nextInt(0,256), 0, rng.nextInt(1,32)),
            String.format("00D6%02X%02X%02X%s", 0, 0, 4, String.format("%08X", rng.nextInt()))
        };
        return cmds[rng.nextInt(cmds.length)];
    }

    private static String nfcTag(ThreadLocalRandom rng) {
        return "{\"uid\":\"" + rfidUid(rng) + "\",\"type\":\"MIFARE Classic 1K\",\"atqa\":\"" +
               nfcAtqa(rng) + "\",\"sak\":\"" + nfcSak(rng) + "\"}";
    }

    private static String irNec(ThreadLocalRandom rng) {
        int addr = rng.nextInt(0, 256);
        int cmd  = rng.nextInt(0, 256);
        return String.format("NEC:0x%02X%02X", addr, cmd);
    }

    private static String irRc5(ThreadLocalRandom rng) {
        return String.format("RC5:0x%04X", rng.nextInt(0, 4096));
    }

    private static String irPronto(ThreadLocalRandom rng) {
        return String.format("0000 006C 0022 0000 %04X %04X %04X %04X",
            rng.nextInt(0, 65536), rng.nextInt(0, 65536), rng.nextInt(0, 65536), rng.nextInt(0, 65536));
    }

    private static String irRaw(ThreadLocalRandom rng) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 12; i++) {
            if (i > 0) sb.append(",");
            sb.append(rng.nextInt(200, 2000));
        }
        return sb.append("]").toString();
    }

    private static String mqttPayload(ThreadLocalRandom rng) {
        String[] topics = {"sensors/temp","devices/status","iot/data","telemetry/raw"};
        double temp = 15 + rng.nextDouble(25);
        return "{\"topic\":\"" + topics[rng.nextInt(topics.length)] + "\",\"temperature\":" +
               String.format("%.1f", temp) + ",\"humidity\":" + rng.nextInt(30,90) + ",\"ts\":" + System.currentTimeMillis() + "}";
    }

    private static String loraPacket(ThreadLocalRandom rng) {
        return String.format("{\"devEUI\":\"%016X\",\"fPort\":%d,\"fCnt\":%d,\"data\":\"%s\",\"rssi\":%d,\"snr\":%.1f}",
            rng.nextLong(), rng.nextInt(1,224), rng.nextInt(0,65536),
            MetaGen.randomHex(rng, 8).toUpperCase(), -120 + rng.nextInt(80), -5 + rng.nextDouble(25));
    }
}
