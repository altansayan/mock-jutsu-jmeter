package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Meta / infrastructure types — mirrors meta.py. */
public final class MetaGen {

    private MetaGen() {}

    private static final SecureRandom SEC = new SecureRandom();

    private static final String[] BROWSER_NAMES    = {"Chrome","Firefox","Safari","Edge","Opera"};
    private static final String[] BROWSER_ENGINES  = {"Blink","Gecko","WebKit","Blink","Blink"};
    private static final String[] TLDS             = {"com","net","org","io","co","app","dev"};
    private static final String[] COLORS           = {
        "#FF5733","#33FF57","#3357FF","#FF33F5","#F5FF33",
        "#33F5FF","#FF8333","#8333FF","#33FF83","#FF3383"
    };
    private static final String[] PROTOCOLS        = {"https://","http://"};
    private static final String[] UA_CHROME_VER    = {"120.0.0","121.0.0","122.0.0","123.0.0","124.0.0"};

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "uuid"              -> UUID.randomUUID().toString();
            case "requestid"         -> "req-" + UUID.randomUUID().toString().substring(0, 18);
            case "correlationid"     -> "corr-" + UUID.randomUUID().toString().substring(0, 18);
            case "sessionid"         -> "sess-" + randomHex(rng, 16);
            case "idempotencykey"    -> UUID.randomUUID().toString();
            case "deviceid"          -> "dev-" + randomHex(rng, 12);
            case "ipv4"              -> ipv4(rng);
            case "ipv6"              -> ipv6(rng);
            case "public_ip"         -> ipv4Public(rng);
            case "private_ip"        -> ipv4Private(rng);
            case "browser_name"      -> pick(rng, BROWSER_NAMES);
            case "browser_version"   -> browserVersion(rng);
            case "browser_engine"    -> pick(rng, BROWSER_ENGINES);
            case "useragent"         -> userAgent(rng);
            case "timestamp"         -> String.valueOf(System.currentTimeMillis());
            case "timestamp_iso"     -> Instant.now().toString();
            case "clientversion"     -> String.format("%d.%d.%d", rng.nextInt(1,5), rng.nextInt(0,20), rng.nextInt(0,100));
            case "bearertoken"       -> "Bearer " + jwt(rng);
            case "signature"         -> signature();
            case "apppassword"       -> appPassword(rng);
            case "jwt"               -> jwt(rng);
            case "hash"              -> hash();
            case "mac_address"       -> macAddress(rng);
            case "domain"            -> domain(rng);
            case "url"               -> url(rng);
            case "color"             -> pick(rng, COLORS);
            case "api_key"           -> apiKey(rng);
            case "totp_code"         -> String.format("%06d", rng.nextInt(0, 1000000));
            case "webhook_signature" -> "sha256=" + hash();
            case "transaction_id"    -> "txn-" + UUID.randomUUID().toString().replace("-","").substring(0, 20);
            default                  -> "ERROR: Unknown meta type '" + type + "'";
        };
    }

    // ── IPv4 ──────────────────────────────────────────────────────────────────

    static String ipv4(ThreadLocalRandom rng) {
        return rng.nextInt(1,256) + "." + rng.nextInt(0,256) + "." +
               rng.nextInt(0,256) + "." + rng.nextInt(1,255);
    }

    private static String ipv4Public(ThreadLocalRandom rng) {
        // Avoid private/reserved ranges — simplified
        int a;
        do { a = rng.nextInt(1, 256); }
        while (a == 10 || a == 127 || a == 172 || a == 192);
        return a + "." + rng.nextInt(0,256) + "." + rng.nextInt(0,256) + "." + rng.nextInt(1,255);
    }

    private static String ipv4Private(ThreadLocalRandom rng) {
        return switch (rng.nextInt(3)) {
            case 0  -> "10." + rng.nextInt(0,256) + "." + rng.nextInt(0,256) + "." + rng.nextInt(1,255);
            case 1  -> "172." + rng.nextInt(16,32) + "." + rng.nextInt(0,256) + "." + rng.nextInt(1,255);
            default -> "192.168." + rng.nextInt(0,256) + "." + rng.nextInt(1,255);
        };
    }

    // ── IPv6 ──────────────────────────────────────────────────────────────────

    static String ipv6(ThreadLocalRandom rng) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i > 0) sb.append(':');
            sb.append(String.format("%04x", rng.nextInt(0, 65536)));
        }
        return sb.toString();
    }

    // ── Browser ───────────────────────────────────────────────────────────────

    private static String browserVersion(ThreadLocalRandom rng) {
        return String.format("%d.%d.%d.%d",
            rng.nextInt(80,130), rng.nextInt(0,10), rng.nextInt(0,10000), rng.nextInt(0,200));
    }

    private static String userAgent(ThreadLocalRandom rng) {
        String os = switch (rng.nextInt(4)) {
            case 0 -> "Windows NT 10.0; Win64; x64";
            case 1 -> "Macintosh; Intel Mac OS X 10_15_7";
            case 2 -> "X11; Linux x86_64";
            default -> "iPhone; CPU iPhone OS 17_0 like Mac OS X";
        };
        String ver = pick(rng, UA_CHROME_VER);
        return String.format("Mozilla/5.0 (%s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%s Safari/537.36", os, ver);
    }

    // ── JWT ───────────────────────────────────────────────────────────────────

    static String jwt(ThreadLocalRandom rng) {
        // Header
        String header  = b64url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        // Payload
        long iat = System.currentTimeMillis() / 1000;
        long exp = iat + 900; // 15 minutes
        String sub = UUID.randomUUID().toString();
        String payload = b64url(String.format(
            "{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d,\"jti\":\"%s\"}",
            sub, iat, exp, randomHex(rng, 8)));
        // Signature (random bytes — not verifiable, correct structure)
        byte[] sig = new byte[32];
        SEC.nextBytes(sig);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        return header + "." + payload + "." + signature;
    }

    private static String b64url(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes());
    }

    // ── Hash ──────────────────────────────────────────────────────────────────

    private static String hash() {
        byte[] bytes = new byte[32];
        SEC.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    // ── Signature ─────────────────────────────────────────────────────────────

    private static String signature() {
        byte[] bytes = new byte[64];
        SEC.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    // ── App Password (16 chars, groups of 4) ──────────────────────────────────

    private static String appPassword(ThreadLocalRandom rng) {
        StringBuilder sb = new StringBuilder(19);
        for (int g = 0; g < 4; g++) {
            if (g > 0) sb.append('-');
            for (int i = 0; i < 4; i++) sb.append(BASE62.charAt(rng.nextInt(BASE62.length())));
        }
        return sb.toString();
    }

    // ── MAC address ───────────────────────────────────────────────────────────

    private static String macAddress(ThreadLocalRandom rng) {
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(':');
            sb.append(String.format("%02X", rng.nextInt(0, 256)));
        }
        return sb.toString();
    }

    // ── Domain & URL ─────────────────────────────────────────────────────────

    private static String domain(ThreadLocalRandom rng) {
        String[] words = {"mock","test","demo","sample","example","data","api","dev"};
        return words[rng.nextInt(words.length)] + rng.nextInt(10, 999) + "." + pick(rng, TLDS);
    }

    private static String url(ThreadLocalRandom rng) {
        String d    = domain(rng);
        String path = "/api/v" + rng.nextInt(1, 4) + "/" + new String[]{"users","orders","products","accounts"}[rng.nextInt(4)];
        return pick(rng, PROTOCOLS) + d + path;
    }

    // ── API key ───────────────────────────────────────────────────────────────

    private static String apiKey(ThreadLocalRandom rng) {
        return "mk_" + (rng.nextBoolean() ? "live" : "test") + "_" + randomHex(rng, 32);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    static String randomHex(ThreadLocalRandom rng, int chars) {
        StringBuilder sb = new StringBuilder(chars);
        for (int i = 0; i < chars; i++) sb.append("0123456789abcdef".charAt(rng.nextInt(16)));
        return sb.toString();
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }
}
