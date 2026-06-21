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
        return generate(type, locale, "");
    }

    public static String generate(String type, String locale, String qualifier) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "uuid"              -> UUID.randomUUID().toString();
            case "requestid"         -> UUID.randomUUID().toString();
            case "correlationid"     -> UUID.randomUUID().toString();
            case "sessionid"         -> UUID.randomUUID().toString();
            case "idempotencykey"    -> UUID.randomUUID().toString();
            case "deviceid"          -> UUID.randomUUID().toString().toUpperCase();
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
            case "hash"              -> hash(qualifier);
            case "mac_address"       -> macAddress(rng);
            case "domain"            -> domain(rng);
            case "url"               -> url(rng);
            case "color"             -> color(rng, qualifier);
            case "api_key"           -> apiKey(rng);
            case "totp_code"         -> String.format("%06d", rng.nextInt(0, 1000000));
            case "webhook_signature" -> "sha256=" + hash("");
            case "transaction_id"    -> "TXN" + randomHex(rng, 8).toUpperCase();
            case "slug"              -> slug(rng);
            case "http_method"       -> pick(rng, new String[]{"GET","POST","PUT","DELETE","PATCH","HEAD","OPTIONS"});
            case "http_status_code"  -> pick(rng, new String[]{"200","201","204","301","302","400","401","403","404","409","422","429","500","502","503"});
            case "port_number"       -> String.valueOf(rng.nextInt(1, 65536));
            case "hostname"          -> hostname(rng);
            case "tld"               -> pick(rng, TLDS);
            case "uri_path"          -> uriPath(rng);
            default                  -> "ERROR: Unknown meta type '" + type + "'";
        };
    }

    // ── IPv4 ──────────────────────────────────────────────────────────────────

    static String ipv4(ThreadLocalRandom rng) {
        return rng.nextInt(1,256) + "." + rng.nextInt(0,256) + "." +
               rng.nextInt(0,256) + "." + rng.nextInt(1,255);
    }

    private static String ipv4Public(ThreadLocalRandom rng) {
        // Avoid private/reserved ranges (RFC 1918, RFC 5735)
        int a, b;
        do {
            a = rng.nextInt(1, 256);
            b = rng.nextInt(0, 256);
        } while (
            a == 10                          // 10.0.0.0/8
            || a == 127                      // 127.0.0.0/8 loopback
            || (a == 172 && b >= 16 && b <= 31) // 172.16.0.0/12
            || (a == 192 && b == 168)        // 192.168.0.0/16
        );
        return a + "." + b + "." + rng.nextInt(0,256) + "." + rng.nextInt(1,255);
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

    private static String hash(String algorithm) {
        int bytes = switch (algorithm.toLowerCase()) {
            case "md5"    -> 16;
            case "sha1"   -> 20;
            case "sha384" -> 48;
            case "sha512" -> 64;
            default       -> 32; // sha256
        };
        byte[] b = new byte[bytes];
        SEC.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    private static final String[] COLOR_NAMES = {
        "red","blue","green","yellow","purple","orange","pink","cyan","magenta","black","white","gray","indigo","teal","lime"
    };

    private static String color(ThreadLocalRandom rng, String format) {
        int r = rng.nextInt(256), g = rng.nextInt(256), b = rng.nextInt(256);
        return switch (format.toLowerCase()) {
            case "rgb"  -> String.format("rgb(%d, %d, %d)", r, g, b);
            case "hsl"  -> String.format("hsl(%d, %d%%, %d%%)", rng.nextInt(360), 20 + rng.nextInt(81), 20 + rng.nextInt(61));
            case "name" -> COLOR_NAMES[rng.nextInt(COLOR_NAMES.length)];
            default     -> String.format("#%02X%02X%02X", r, g, b); // hex (default)
        };
    }

    // ── Signature ─────────────────────────────────────────────────────────────

    private static String signature() {
        byte[] bytes = new byte[64];
        SEC.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    // ── App Password — 6-digit PIN, no consecutive repeats, no sequential run of 3+ ──

    private static String appPassword(ThreadLocalRandom rng) {
        int[] d;
        do {
            d = new int[6];
            for (int i = 0; i < 6; i++) d[i] = rng.nextInt(10);
        } while (hasConsecutiveRepeat(d) || hasSequentialRun(d));
        StringBuilder sb = new StringBuilder(6);
        for (int v : d) sb.append(v);
        return sb.toString();
    }

    private static boolean hasConsecutiveRepeat(int[] d) {
        for (int i = 0; i < d.length - 1; i++) if (d[i] == d[i + 1]) return true;
        return false;
    }

    private static boolean hasSequentialRun(int[] d) {
        for (int i = 0; i < d.length - 2; i++)
            if (d[i + 1] == d[i] + 1 && d[i + 2] == d[i] + 2) return true;
        return false;
    }

    // ── MAC address (IEEE OUI prefix + 3 random bytes) ───────────────────────

    private static final String[] OUI_PREFIXES = {
        "A4:C3:F0","3C:22:FB","B8:27:EB","DC:2C:6E","00:50:56","08:00:27",
        "D8:BB:2C","28:6F:7F","F0:18:98","00:1C:42","00:23:AE","AC:BC:32",
        "F4:5C:89","70:F0:96","CC:46:D6","00:0C:29","44:38:39","2C:F0:5D"
    };

    private static String macAddress(ThreadLocalRandom rng) {
        return OUI_PREFIXES[rng.nextInt(OUI_PREFIXES.length)] + ":" +
               String.format("%02X:%02X:%02X",
                   rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
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

    // ── Slug ──────────────────────────────────────────────────────────────────

    private static String slug(ThreadLocalRandom rng) {
        String[] nouns = {"upload","product","order","user","report","event","session","record","item","ticket"};
        String[] adjs  = {"public","private","new","active","pending","archived","shared","draft"};
        int year = java.time.LocalDate.now().getYear();
        return adjs[rng.nextInt(adjs.length)] + "-" + nouns[rng.nextInt(nouns.length)] + "-" + year;
    }

    // ── Hostname ──────────────────────────────────────────────────────────────

    private static String hostname(ThreadLocalRandom rng) {
        String[] prefixes = {"api","web","app","srv","db","cache","worker","proxy","gateway","node"};
        return prefixes[rng.nextInt(prefixes.length)] + "-" + rng.nextInt(1, 100) + "." + domain(rng);
    }

    // ── URI Path ──────────────────────────────────────────────────────────────

    private static String uriPath(ThreadLocalRandom rng) {
        String[] resources = {"users","orders","products","accounts","payments","invoices","reports","events"};
        String   res = resources[rng.nextInt(resources.length)];
        int      id  = rng.nextInt(1, 10000);
        return "/api/v" + rng.nextInt(1, 4) + "/" + res + "/" + id;
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
