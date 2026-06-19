package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/** CryptoFuzz — JWT attack patterns, ASN.1 fuzz payloads. Mirrors crypto_fuzz.py. */
public final class CryptoFuzzGen {
    private CryptoFuzzGen() {}
    private static final SecureRandom SEC = new SecureRandom();

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "jwt_attack"  -> jwtAttack(rng);
            case "asn1_fuzz"   -> asn1Fuzz(rng);
            default -> "ERROR: Unknown crypto fuzz type '" + type + "'";
        };
    }

    private static String jwtAttack(ThreadLocalRandom rng) {
        String[] attacks = {"none_alg", "alg_confusion", "empty_sig", "kid_injection"};
        String[] descs   = {
            "Algorithm set to none to bypass signature verification",
            "Algorithm confusion attack using public key as HMAC secret",
            "Empty signature to test signature validation bypass",
            "Key ID injection via path traversal"
        };
        int idx = rng.nextInt(attacks.length);
        String attack = attacks[idx];
        String desc   = descs[idx];
        String header = switch (attack) {
            case "none_alg"      -> b64url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
            case "alg_confusion" -> b64url("{\"alg\":\"HS256\",\"typ\":\"JWT\",\"x5u\":\"http://attacker.test/key\"}");
            case "kid_injection" -> b64url("{\"alg\":\"HS256\",\"kid\":\"../../dev/null\",\"typ\":\"JWT\"}");
            default              -> b64url("{\"alg\":\"RS256\",\"typ\":\"JWT\"}");
        };
        String payload = b64url("{\"sub\":\"mock_user\",\"iat\":" + (System.currentTimeMillis()/1000) + "}");
        String sig     = attack.equals("empty_sig") ? "" : b64url(new byte[32]);
        String token   = header + "." + payload + "." + sig;
        return "{\"attack_type\":\"" + attack + "\",\"description\":\"" + desc + "\",\"token\":\"" + token + "\"}";
    }

    private static final String[] FUZZ_TYPES = {"length_overflow","length_underflow","wrong_tag","nested_overflow","truncated"};

    private static String asn1Fuzz(ThreadLocalRandom rng) {
        byte[] payload = new byte[32];
        SEC.nextBytes(payload);
        payload[0] = 0x30; // SEQUENCE
        String fuzzType = FUZZ_TYPES[rng.nextInt(FUZZ_TYPES.length)];
        String hex = bytesToHex(payload);
        return switch (fuzzType) {
            case "length_overflow"  -> "{\"fuzz_type\":\"length_overflow\",\"hex\":\"" + hex + "\",\"declared_length\":" + (payload.length + 100) + ",\"actual_bytes\":" + payload.length + "}";
            case "length_underflow" -> "{\"fuzz_type\":\"length_underflow\",\"hex\":\"" + hex + "\",\"claimed_length\":" + rng.nextInt(4) + "}";
            case "wrong_tag"        -> "{\"fuzz_type\":\"wrong_tag\",\"hex\":\"" + hex + "\",\"tag_byte\":\"0x" + String.format("%02X", rng.nextInt(256)) + "\"}";
            case "nested_overflow"  -> "{\"fuzz_type\":\"nested_overflow\",\"hex\":\"" + hex + "\",\"inner_claimed\":" + (payload.length + 50) + ",\"inner_actual\":" + payload.length + "}";
            default                 -> "{\"fuzz_type\":\"truncated\",\"hex\":\"" + hex + "\",\"length\":" + rng.nextInt(1, payload.length) + "}";
        };
    }

    private static String b64url(String s) { return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes()); }
    private static String b64url(byte[] b)  { return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }

    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(String.format("%02X", v));
        return sb.toString();
    }
}
