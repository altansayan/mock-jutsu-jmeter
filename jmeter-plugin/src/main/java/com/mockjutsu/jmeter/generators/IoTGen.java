package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** IoT — RFID, NFC, IR, MQTT, LoRa. Mirrors iot.py. */
public final class IoTGen {
    private IoTGen() {}

    // RFID OUI-style first bytes (ISO 14443 / NXP)
    private static final int[] RFID_OUI = {0x04, 0x02, 0xE0, 0x80, 0x01};

    // NFC profiles: {atqa, sak, type, capacity}
    private static final String[][] NFC_PROFILES = {
        {"00:44", "00", "MIFARE Ultralight", "64"},
        {"00:04", "08", "MIFARE Classic 1K", "716"},
        {"00:02", "18", "MIFARE Classic 4K", "3440"},
        {"03:44", "20", "MIFARE DESFire EV1", "7680"},
        {"00:44", "60", "MIFARE Plus SL1", "2048"}
    };

    // IR NEC device address pool: {address, system_name}
    private static final String[][] NEC_DEVICES = {
        {"0x00", "Generic TV"}, {"0x01", "Cable Box"}, {"0x04", "DVD Player"},
        {"0x08", "Amplifier"},  {"0x10", "Projector"}, {"0x20", "Set-Top Box"}
    };

    // IR RC5 system pool
    private static final String[][] RC5_SYSTEMS = {
        {"0",  "TV set"},         {"1",  "TV monitor"},
        {"5",  "VCR"},            {"16", "Pre-amplifier"},
        {"17", "Tuner"},          {"18", "Tape deck"},
        {"20", "CD player"},      {"21", "Phono"}
    };

    // MQTT sensor types
    private static final String[] SENSOR_TYPES = {
        "temperature_humidity","air_quality","motion","pressure","vibration","light","gps"
    };

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "rfid_uid","rfid_tag" -> rfidUid(rng);
            case "epc"                 -> epc(rng);
            case "nfc_uid"             -> rfidUid(rng);
            case "nfc_atqa"            -> nfcProfile(rng)[0];
            case "nfc_sak"             -> nfcProfile(rng)[1];
            case "ndef_uri"            -> ndefUri(rng);
            case "ndef_text"           -> ndefText(rng, locale);
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

    // ── RFID UID — OUI first byte + random rest ───────────────────────────────

    private static String rfidUid(ThreadLocalRandom rng) {
        int len = rng.nextBoolean() ? 4 : 7;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X", RFID_OUI[rng.nextInt(RFID_OUI.length)]));
        for (int i = 1; i < len; i++) sb.append(':').append(String.format("%02X", rng.nextInt(256)));
        return sb.toString();
    }

    // ── EPC SGTIN-96 — 24 uppercase hex chars (96 bits) ──────────────────────

    private static String epc(ThreadLocalRandom rng) {
        // header=0x30 (SGTIN-96), rest random
        StringBuilder sb = new StringBuilder("30");
        for (int i = 0; i < 11; i++) sb.append(String.format("%02X", rng.nextInt(256)));
        return sb.toString(); // 24 hex chars
    }

    // ── NFC profile helper ────────────────────────────────────────────────────

    private static String[] nfcProfile(ThreadLocalRandom rng) {
        return NFC_PROFILES[rng.nextInt(NFC_PROFILES.length)];
    }

    // ── NDEF URI — JSON dict {raw_hex, decoded, tnf, type, prefix_code} ──────

    private static String ndefUri(ThreadLocalRandom rng) {
        String[] uris = {
            "https://example.com","https://mock-jutsu.io",
            "https://api.test.com/v1/resource","http://www.mockjutsu.dev/test"
        };
        String[] prefixCodes = {"01","02","03","04"}; // https://, https://www., http://, http://www.
        String uri = uris[rng.nextInt(uris.length)];
        String prefix = prefixCodes[rng.nextInt(prefixCodes.length)];

        // Minimal NDEF URI record hex
        byte[] uriBytes = uri.getBytes();
        String payloadHex = prefix + toHex(uriBytes);
        String rawHex = String.format("D10%02X55%s", uriBytes.length + 1, payloadHex);

        return String.format(
            "{\"raw_hex\":\"%s\",\"decoded\":\"%s\",\"tnf\":1,\"type\":\"U\",\"prefix_code\":\"%s\"}",
            rawHex, uri.replace("\"", "\\\""), prefix);
    }

    // ── NDEF Text — JSON dict {raw_hex, decoded, lang, tnf, type, encoding} ──

    private static String ndefText(ThreadLocalRandom rng, String locale) {
        String[] texts = {"MOCKJUTSU TEST TAG","Mock Data NFC","Test Record 001","NDEF Mock Text"};
        String text = texts[rng.nextInt(texts.length)];
        String lang = switch (locale) {
            case "TR" -> "tr"; case "DE" -> "de"; case "FR" -> "fr";
            case "UK","US" -> "en"; case "RU" -> "ru"; default -> "en";
        };
        String encoding = "UTF-8";

        // Status byte: bit7=0 (UTF-8), bits5-0 = lang length
        String statusHex = String.format("%02X", lang.length());
        byte[] textBytes = text.getBytes();
        String payloadHex = statusHex + toHex(lang.getBytes()) + toHex(textBytes);
        String rawHex = String.format("D10%02X54%s", lang.length() + textBytes.length + 1, payloadHex);

        return String.format(
            "{\"raw_hex\":\"%s\",\"decoded\":\"%s\",\"lang\":\"%s\",\"tnf\":1,\"type\":\"T\",\"encoding\":\"%s\"}",
            rawHex, text.replace("\"", "\\\""), lang, encoding);
    }

    // ── APDU — JSON dict {cla, ins, p1, p2, lc, data, le, hex, description} ──

    private static String apdu(ThreadLocalRandom rng) {
        // Three command types
        int t = rng.nextInt(3);
        String cla, ins, p1, p2, lc, data, le, description;
        if (t == 0) {
            // SELECT by AID
            cla = "00"; ins = "A4"; p1 = "04"; p2 = "00"; lc = "07";
            data = "A0000000041010"; le = "00";
            description = "SELECT APPLICATION A0000000041010";
        } else if (t == 1) {
            // READ BINARY
            int offset = rng.nextInt(128);
            int length = 1 + rng.nextInt(32);
            cla = "00"; ins = "B0";
            p1 = String.format("%02X", offset >> 8);
            p2 = String.format("%02X", offset & 0xFF);
            lc = ""; data = "";
            le = String.format("%02X", length);
            description = "READ BINARY offset=" + offset;
        } else {
            // UPDATE BINARY
            int offset = rng.nextInt(64);
            String d = randomHexUpper(rng, 8);
            cla = "00"; ins = "D6";
            p1 = String.format("%02X", offset >> 8);
            p2 = String.format("%02X", offset & 0xFF);
            lc = "04"; data = d; le = "";
            description = "UPDATE BINARY";
        }
        String hexParts = cla + ins + p1 + p2 + (lc.isEmpty() ? "" : lc) + data + (le.isEmpty() ? "" : le);
        return String.format(
            "{\"cla\":\"%s\",\"ins\":\"%s\",\"p1\":\"%s\",\"p2\":\"%s\",\"lc\":\"%s\",\"data\":\"%s\",\"le\":\"%s\",\"hex\":\"%s\",\"description\":\"%s\"}",
            cla, ins, p1, p2, lc, data, le, hexParts, description);
    }

    // ── NFC Tag — JSON dict {uid, atqa, sak, type, capacity_bytes, ndef_message, ndef_decoded} ──

    private static String nfcTag(ThreadLocalRandom rng) {
        String[] profile = nfcProfile(rng);
        String uid = rfidUid(rng);
        // Embed a simple NDEF URI message
        String ndefMsg = "D101" + String.format("%02X", 16) + "55" + "01" + toHex("example.com".getBytes());
        String ndefDecoded = "https://example.com";
        return String.format(
            "{\"uid\":\"%s\",\"atqa\":\"%s\",\"sak\":\"%s\",\"type\":\"%s\",\"capacity_bytes\":%s,\"ndef_message\":\"%s\",\"ndef_decoded\":\"%s\"}",
            uid, profile[0], profile[1], profile[2], profile[3], ndefMsg, ndefDecoded);
    }

    // ── IR NEC — JSON dict ────────────────────────────────────────────────────

    private static String irNec(ThreadLocalRandom rng) {
        String[] dev = NEC_DEVICES[rng.nextInt(NEC_DEVICES.length)];
        String address = dev[0];
        String sysName = dev[1];
        int addr = Integer.parseInt(address.substring(2), 16);
        int cmd  = rng.nextInt(256);
        int invAddr = (~addr) & 0xFF;
        int invCmd  = (~cmd)  & 0xFF;
        String hex = String.format("%02X%02X%02X%02X", addr, invAddr, cmd, invCmd);
        return String.format(
            "{\"address\":\"%s\",\"command\":\"0x%02X\",\"inv_address\":\"0x%02X\",\"inv_command\":\"0x%02X\"," +
            "\"hex\":\"%s\",\"checksum_valid\":true,\"carrier_hz\":38000,\"protocol\":\"NEC\"}",
            address, cmd, invAddr, invCmd, hex);
    }

    // ── IR RC5 — JSON dict ────────────────────────────────────────────────────

    private static String irRc5(ThreadLocalRandom rng) {
        String[] sys = RC5_SYSTEMS[rng.nextInt(RC5_SYSTEMS.length)];
        int system  = Integer.parseInt(sys[0]);
        int command = rng.nextInt(64); // 6-bit command
        int toggle  = rng.nextInt(2);
        // RC5 frame: start(2) + toggle(1) + system(5) + command(6) = 14 bits
        int frame = 0x3000 | (toggle << 11) | (system << 6) | command;
        return String.format(
            "{\"system\":%d,\"system_name\":\"%s\",\"command\":%d,\"toggle\":%d," +
            "\"frame_bits\":\"0x%04X\",\"carrier_hz\":36000,\"protocol\":\"RC5\"}",
            system, sys[1], command, toggle, frame);
    }

    // ── IR Pronto Hex — NEC timing format ────────────────────────────────────
    // Pronto NEC: 0000 006C 0022 0000 {leader_pair} {data_pairs...} {stop}

    private static String irPronto(ThreadLocalRandom rng) {
        int addr = rng.nextInt(256);
        int cmd  = rng.nextInt(256);
        // Standard NEC Pronto: frequency=006C (38kHz), pair_count=0022 (34 pairs)
        StringBuilder sb = new StringBuilder("0000 006C 0022 0000 ");
        // Leader: 342 173
        sb.append("0156 00AB ");
        // Encode 8 bits address + 8 bits ~address + 8 bits command + 8 bits ~command
        int[] bytes = {addr, (~addr)&0xFF, cmd, (~cmd)&0xFF};
        for (int b : bytes) {
            for (int bit = 0; bit < 8; bit++) {
                if (((b >> (7-bit)) & 1) == 1) {
                    sb.append("0016 0041 "); // 1 bit
                } else {
                    sb.append("0016 0016 "); // 0 bit
                }
            }
        }
        // Stop
        sb.append("0016 05F0");
        return sb.toString().trim();
    }

    // ── IR Raw — JSON dict ────────────────────────────────────────────────────

    private static String irRaw(ThreadLocalRandom rng) {
        int addr = rng.nextInt(256);
        int cmd  = rng.nextInt(256);
        // NEC-style pulses: leader + 32 data bits + stop
        // Leader mark=9000µs, space=4500µs; bit1=560+1690, bit0=560+560, stop=560
        int[] pulses = buildNecPulses(addr, cmd);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < pulses.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(pulses[i]);
        }
        sb.append(']');
        return String.format(
            "{\"carrier_hz\":38000,\"address\":\"0x%02X\",\"command\":\"0x%02X\",\"pulses\":%s,\"pulse_count\":%d}",
            addr, cmd, sb, pulses.length);
    }

    private static int[] buildNecPulses(int addr, int cmd) {
        int[] bytes = {addr, (~addr)&0xFF, cmd, (~cmd)&0xFF};
        int[] p = new int[2 + 32*2 + 1]; // leader(2) + data(64) + stop(1)
        p[0] = 9000; p[1] = 4500;
        int idx = 2;
        for (int b : bytes) {
            for (int bit = 7; bit >= 0; bit--) {
                p[idx++] = 560;
                p[idx++] = ((b >> bit) & 1) == 1 ? 1690 : 560;
            }
        }
        p[idx] = 560;
        return p;
    }

    // ── MQTT Payload — JSON dict with sensor readings ─────────────────────────

    private static String mqttPayload(ThreadLocalRandom rng) {
        String sensorType = SENSOR_TYPES[rng.nextInt(SENSOR_TYPES.length)];
        String deviceId = "MOCKJ-" + randomHexUpper(rng, 4);
        long ts = System.currentTimeMillis();
        int rssi = -120 + rng.nextInt(80);
        double snr = -5.0 + rng.nextDouble(25.0);
        int batt = 1 + rng.nextInt(100);

        String readings;
        switch (sensorType) {
            case "temperature_humidity":
                readings = String.format("{\"temperature\":%.1f,\"humidity\":%d}",
                    15.0 + rng.nextDouble(25.0), 30 + rng.nextInt(60));
                break;
            case "air_quality":
                readings = String.format("{\"co2\":%d,\"pm25\":%.1f,\"pm10\":%.1f}",
                    400 + rng.nextInt(1600), rng.nextDouble(100.0), rng.nextDouble(150.0));
                break;
            case "motion":
                readings = String.format("{\"motion\":%s,\"count\":%d}",
                    rng.nextBoolean() ? "true" : "false", rng.nextInt(100));
                break;
            case "pressure":
                readings = String.format("{\"pressure\":%.1f,\"altitude\":%.1f}",
                    950.0 + rng.nextDouble(100.0), rng.nextDouble(500.0));
                break;
            case "light":
                readings = String.format("{\"lux\":%d,\"uv_index\":%.1f}",
                    rng.nextInt(10000), rng.nextDouble(11.0));
                break;
            case "gps":
                readings = String.format("{\"lat\":%.6f,\"lon\":%.6f,\"alt\":%.1f}",
                    -90.0 + rng.nextDouble(180.0), -180.0 + rng.nextDouble(360.0), rng.nextDouble(500.0));
                break;
            default:
                readings = String.format("{\"value\":%.2f}", rng.nextDouble(100.0));
        }

        return String.format(
            "{\"device_id\":\"%s\",\"timestamp\":%d,\"sensor_type\":\"%s\",\"readings\":%s,\"rssi\":%d,\"snr\":%.1f,\"battery_pct\":%d}",
            deviceId, ts, sensorType, readings, rssi, snr, batt);
    }

    // ── LoRa Packet — space-separated lowercase hex (LoRaWAN frame bytes) ─────

    private static String loraPacket(ThreadLocalRandom rng) {
        // LoRaWAN uplink: MHDR(1) + FHDR(7min) + FPort(1) + FRMPayload(N) + MIC(4)
        // MHDR: 0x40 = unconfirmed data up (MType=010, RFU=0, Major=00)
        int mhdr = 0x40;
        // DevAddr 4 bytes LE
        int devAddr = rng.nextInt();
        // FCtrl, FCnt, FOpts
        int fctrl = 0x00;
        int fcnt  = rng.nextInt(65536);
        // FPort
        int fport = 1 + rng.nextInt(223);
        // Payload 4-12 bytes
        int payloadLen = 4 + rng.nextInt(9);
        byte[] payload = new byte[payloadLen];
        for (int i = 0; i < payloadLen; i++) payload[i] = (byte) rng.nextInt(256);
        // MIC 4 bytes
        byte[] mic = new byte[4];
        for (int i = 0; i < 4; i++) mic[i] = (byte) rng.nextInt(256);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02x", mhdr));
        // DevAddr LE
        for (int i = 0; i < 4; i++) sb.append(String.format(" %02x", (devAddr >> (i*8)) & 0xFF));
        sb.append(String.format(" %02x", fctrl));
        sb.append(String.format(" %02x %02x", fcnt & 0xFF, (fcnt >> 8) & 0xFF));
        sb.append(String.format(" %02x", fport));
        for (byte b : payload) sb.append(String.format(" %02x", b & 0xFF));
        for (byte b : mic) sb.append(String.format(" %02x", b & 0xFF));
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b & 0xFF));
        return sb.toString();
    }

    private static String randomHexUpper(ThreadLocalRandom rng, int chars) {
        StringBuilder sb = new StringBuilder(chars);
        String hex = "0123456789ABCDEF";
        for (int i = 0; i < chars; i++) sb.append(hex.charAt(rng.nextInt(16)));
        return sb.toString();
    }
}
