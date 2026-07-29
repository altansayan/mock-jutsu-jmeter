package com.mockjutsu.jmeter.generators;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** CryptoFuzz — JWT attack patterns, ASN.1 fuzz payloads. Mirrors crypto_fuzz.py. */
public final class CryptoFuzzGen {
    private CryptoFuzzGen() {}

    private static final Logger log = LoggerFactory.getLogger(CryptoFuzzGen.class);

    private static final ThreadLocal<MessageDigest> MD_SHA256 = ThreadLocal.withInitial(() -> {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (java.security.NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    });

    private static final byte[] SECRET_KEY = "mock-jutsu-default-secret-key-2025".getBytes(StandardCharsets.UTF_8);
    private static final byte[] FAKE_PUBLIC_KEY =
        "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\n-----END PUBLIC KEY-----"
            .getBytes(StandardCharsets.UTF_8);

    private static final String[] KID_INJECTION_VALUES = {
        "../../dev/null",
        "' OR 1=1 --",
        "../../../../etc/passwd",
        "/dev/null\u0000",  // null-byte injection: terminates path in C-based validators
        "'; DROP TABLE keys; --",
        "../keys/public.pem"
    };

    private static final String[] JWT_ATTACK_TYPES = {
        "none_alg", "expired", "invalid_signature", "alg_confusion", "kid_injection", "empty_password"
    };

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "jwt_attack"  -> jwtAttack(rng);
            case "asn1_fuzz"   -> asn1Fuzz(rng);
            default -> "ERROR: Unknown crypto fuzz type '" + type + "'";
        };
    }

    // ── JWT Attack ────────────────────────────────────────────────────────────

    private static String jwtAttack(ThreadLocalRandom rng) {
        try {
            String attackType = JWT_ATTACK_TYPES[rng.nextInt(JWT_ATTACK_TYPES.length)];
            String token = switch (attackType) {
                case "none_alg" -> attackNoneAlg(rng);
                case "expired" -> attackExpired(rng);
                case "invalid_signature" -> attackInvalidSignature(rng);
                case "alg_confusion" -> attackAlgConfusion(rng);
                case "kid_injection" -> attackKidInjection(rng);
                default -> attackEmptyPassword(rng);
            };
            String description = switch (attackType) {
                case "none_alg" -> "CVE bypass: JWT 'alg: none' — server accepts unsigned token";
                case "expired" -> "Replay attack: token with exp in the past (1 hour ago)";
                case "invalid_signature" -> "Signature bit-flip: last 4 bytes XOR 0xFF — should fail HMAC verify";
                case "alg_confusion" -> "Algorithm confusion: RS256 header but HMAC-HS256 signed with public key";
                case "kid_injection" -> "Key ID injection: path traversal / SQL in 'kid' header field";
                default -> "HMAC empty key: signed with b'' — common misconfiguration";
            };
            return "{\"token\": \"" + token + "\", \"attack_type\": \"" + attackType + "\", \"description\": \"" + jsonEscape(description) + "\"}";
        } catch (Exception e) {
            log.warn("jwt_attack generation failed: {}", e.getMessage(), e);
            return "ERROR: jwt_attack generation failed: " + e.getMessage();
        }
    }

    private static String basePayload(ThreadLocalRandom rng) {
        long now = System.currentTimeMillis() / 1000;
        String[] roles = {"user", "admin", "service"};
        return "{\"sub\":\"" + UUID.randomUUID() + "\",\"iat\":" + now + ",\"exp\":" + (now + 3600) +
            ",\"jti\":\"" + UUID.randomUUID() + "\",\"role\":\"" + roles[rng.nextInt(roles.length)] + "\"}";
    }

    private static String makeJwt(String headerJson, String payloadJson, byte[] key) throws Exception {
        String h = b64url(headerJson.getBytes(StandardCharsets.UTF_8));
        String p = b64url(payloadJson.getBytes(StandardCharsets.UTF_8));
        byte[] sig = hmacSha256(key, (h + "." + p).getBytes(StandardCharsets.UTF_8));
        return h + "." + p + "." + b64url(sig);
    }

    private static String attackNoneAlg(ThreadLocalRandom rng) {
        String header = "{\"alg\":\"none\",\"typ\":\"JWT\"}";
        String payload = basePayload(rng);
        String h = b64url(header.getBytes(StandardCharsets.UTF_8));
        String p = b64url(payload.getBytes(StandardCharsets.UTF_8));
        return h + "." + p + ".";
    }

    private static String attackExpired(ThreadLocalRandom rng) throws Exception {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        long now = System.currentTimeMillis() / 1000;
        long exp = now - rng.nextInt(3600, 86401);
        UUID sub = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        String[] roles = {"user", "admin", "service"};
        String payload = "{\"sub\":\"" + sub + "\",\"iat\":" + now + ",\"exp\":" + exp +
            ",\"jti\":\"" + jti + "\",\"role\":\"" + roles[rng.nextInt(roles.length)] + "\"}";
        return makeJwt(header, payload, SECRET_KEY);
    }

    private static String attackInvalidSignature(ThreadLocalRandom rng) throws Exception {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = basePayload(rng);
        String token = makeJwt(header, payload, SECRET_KEY);
        String[] parts = token.split("\\.", -1);
        byte[] sigBytes = b64urlDecode(parts[2]);
        int flip = Math.min(4, sigBytes.length);
        for (int i = 1; i <= flip; i++) {
            sigBytes[sigBytes.length - i] ^= (byte) 0xFF;
        }
        parts[2] = b64url(sigBytes);
        return String.join(".", parts);
    }

    private static String attackAlgConfusion(ThreadLocalRandom rng) throws Exception {
        String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String payload = basePayload(rng);
        return makeJwt(header, payload, FAKE_PUBLIC_KEY);
    }

    private static String attackKidInjection(ThreadLocalRandom rng) throws Exception {
        String kid = KID_INJECTION_VALUES[rng.nextInt(KID_INJECTION_VALUES.length)];
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\",\"kid\":\"" + jsonEscape(kid) + "\"}";
        String payload = basePayload(rng);
        return makeJwt(header, payload, SECRET_KEY);
    }

    private static String attackEmptyPassword(ThreadLocalRandom rng) throws Exception {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = basePayload(rng);
        return makeJwt(header, payload, new byte[0]);
    }

    // ── ASN.1 Fuzz ────────────────────────────────────────────────────────────

    private static final String[] FUZZ_TYPES = {
        "truncated", "overflow_length", "wrong_tag", "nested_mismatch", "zero_length", "random_bytes"
    };

    private static String asn1Fuzz(ThreadLocalRandom rng) {
        String fuzzType = FUZZ_TYPES[rng.nextInt(FUZZ_TYPES.length)];
        return switch (fuzzType) {
            case "truncated" -> asn1Truncated(rng);
            case "overflow_length" -> asn1OverflowLength(rng);
            case "wrong_tag" -> asn1WrongTag(rng);
            case "nested_mismatch" -> asn1NestedMismatch(rng);
            case "zero_length" -> asn1ZeroLength(rng);
            default -> asn1RandomBytes(rng);
        };
    }

    private static String asn1Truncated(ThreadLocalRandom rng) {
        int n = rng.nextInt(8, 33);
        int[] tags = {0x30, 0x02, 0x04, 0x06};
        int tag = tags[rng.nextInt(tags.length)];
        byte[] payload = randomBytes(rng, n / 2);
        byte[] data = concat(new byte[]{(byte) tag, (byte) n}, payload);
        return "{\"hex\": \"" + bytesToHexLower(data) + "\", \"fuzz_type\": \"truncated\", " +
            "\"declared_length\": " + n + ", \"actual_bytes\": " + payload.length + "}";
    }

    private static String asn1OverflowLength(ThreadLocalRandom rng) {
        int[] tags = {0x30, 0x02};
        int tag = tags[rng.nextInt(tags.length)];
        byte[] lengthBytes = {(byte) 0x82, (byte) 0xFF, (byte) 0xFF};
        byte[] tinyPayload = randomBytes(rng, 4);
        byte[] data = concat(new byte[]{(byte) tag}, lengthBytes, tinyPayload);
        return "{\"hex\": \"" + bytesToHexLower(data) + "\", \"fuzz_type\": \"overflow_length\", \"claimed_length\": 65535}";
    }

    private static String asn1WrongTag(ThreadLocalRandom rng) {
        int[] badTags = {0x00, 0x0F, 0x1F, 0xFF, 0x3F, 0x7F, 0xBF};
        int tag = badTags[rng.nextInt(badTags.length)];
        int n = rng.nextInt(2, 13);
        byte[] payload = randomBytes(rng, n);
        byte[] data = concat(new byte[]{(byte) tag, (byte) n}, payload);
        return "{\"hex\": \"" + bytesToHexLower(data) + "\", \"fuzz_type\": \"wrong_tag\", \"tag_byte\": \"0x" + Integer.toHexString(tag) + "\"}";
    }

    private static String asn1NestedMismatch(ThreadLocalRandom rng) {
        byte[] innerActual = randomBytes(rng, 4);
        byte[] inner = concat(new byte[]{0x02, 8}, innerActual);
        byte[] outer = concat(new byte[]{0x30, (byte) inner.length}, inner);
        return "{\"hex\": \"" + bytesToHexLower(outer) + "\", \"fuzz_type\": \"nested_mismatch\", " +
            "\"inner_claimed\": 8, \"inner_actual\": 4}";
    }

    private static String asn1ZeroLength(ThreadLocalRandom rng) {
        int[] tags = {0x30, 0x02, 0x04, 0x05, 0x13};
        int tag = tags[rng.nextInt(tags.length)];
        byte[] data = {(byte) tag, 0x00};
        return "{\"hex\": \"" + bytesToHexLower(data) + "\", \"fuzz_type\": \"zero_length\", \"tag_byte\": \"0x" + Integer.toHexString(tag) + "\"}";
    }

    private static String asn1RandomBytes(ThreadLocalRandom rng) {
        int n = rng.nextInt(4, 25);
        byte[] payload = randomBytes(rng, n);
        byte[] data = concat(new byte[]{0x30, (byte) n}, payload);
        return "{\"hex\": \"" + bytesToHexLower(data) + "\", \"fuzz_type\": \"random_bytes\", \"length\": " + n + "}";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    static byte[] hmacSha256(byte[] key, byte[] msg) throws Exception {
        int blockSize = 64;
        MessageDigest sha256 = MD_SHA256.get();
        byte[] k = key.length > blockSize ? sha256.digest(key) : key;
        byte[] keyPadded = new byte[blockSize];
        System.arraycopy(k, 0, keyPadded, 0, k.length);
        byte[] ipad = new byte[blockSize];
        byte[] opad = new byte[blockSize];
        for (int i = 0; i < blockSize; i++) {
            ipad[i] = (byte) (keyPadded[i] ^ 0x36);
            opad[i] = (byte) (keyPadded[i] ^ 0x5c);
        }
        sha256.reset();
        sha256.update(ipad);
        sha256.update(msg);
        byte[] inner = sha256.digest();
        sha256.reset();
        sha256.update(opad);
        sha256.update(inner);
        return sha256.digest();
    }

    private static byte[] randomBytes(ThreadLocalRandom rng, int n) {
        byte[] b = new byte[n];
        for (int i = 0; i < n; i++) b[i] = (byte) rng.nextInt(256);
        return b;
    }

    private static byte[] concat(byte[]... parts) {
        int len = 0;
        for (byte[] p : parts) len += p.length;
        byte[] out = new byte[len];
        int pos = 0;
        for (byte[] p : parts) { System.arraycopy(p, 0, out, pos, p.length); pos += p.length; }
        return out;
    }

    private static String bytesToHexLower(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(String.format("%02x", v));
        return sb.toString();
    }

    private static String b64url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static byte[] b64urlDecode(String s) {
        int pad = (4 - s.length() % 4) % 4;
        StringBuilder padded = new StringBuilder(s);
        for (int i = 0; i < pad; i++) padded.append('=');
        return Base64.getUrlDecoder().decode(padded.toString());
    }

    private static String jsonEscape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> { if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c); }
            }
        }
        return sb.toString();
    }
}
