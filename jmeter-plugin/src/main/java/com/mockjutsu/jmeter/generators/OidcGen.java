package com.mockjutsu.jmeter.generators;

import com.mockjutsu.jmeter.Randoms;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** OIDC / JWT — ES256 (P-256 via JDK native EC) + HS256. Mirrors oidc.py. */
public final class OidcGen {
    private OidcGen() {}
    private static final Logger log = LoggerFactory.getLogger(OidcGen.class);

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        try {
            return switch (type) {
                case "oidc_token_set" -> tokenSet(rng);
                case "jwks"           -> jwks();
                case "oidc_token"     -> oidcToken(rng);
                default -> "ERROR: Unknown OIDC type '" + type + "'";
            };
        } catch (Exception e) {
            log.warn("OIDC generation failed: {}", e.getMessage(), e);
            return "ERROR: OIDC generation failed: " + e.getMessage();
        }
    }

    private static String oidcClaimsJson(ThreadLocalRandom rng) {
        long now = System.currentTimeMillis() / 1000;
        return "{\"iss\":\"https://mock-issuer.example.com\",\"sub\":\"user-" + UUID.randomUUID() +
            "\",\"aud\":\"mock-client\",\"exp\":" + (now + 3600) + ",\"iat\":" + now +
            ",\"jti\":\"" + UUID.randomUUID() + "\",\"email\":\"user" + rng.nextInt(1, 10000) +
            "@example.com\",\"name\":\"Mock User " + rng.nextInt(1, 1000) + "\"}";
    }

    private static KeyPair genP256KeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"), Randoms.SECURE);
        return kpg.generateKeyPair();
    }

    private static String tokenSet(ThreadLocalRandom rng) throws Exception {
        KeyPair kp = genP256KeyPair();
        ECPublicKey pub = (ECPublicKey) kp.getPublic();
        String kid = UUID.randomUUID().toString().substring(0, 8);

        String xB64 = b64uBigInt(pub.getW().getAffineX());
        String yB64 = b64uBigInt(pub.getW().getAffineY());
        String jwk = "{\"kty\":\"EC\",\"crv\":\"P-256\",\"x\":\"" + xB64 + "\",\"y\":\"" + yB64 +
            "\",\"kid\":\"" + kid + "\",\"use\":\"sig\",\"alg\":\"ES256\"}";

        String claims = oidcClaimsJson(rng);
        String header = b64u(("{\"alg\":\"ES256\",\"typ\":\"JWT\",\"kid\":\"" + kid + "\"}").getBytes(StandardCharsets.UTF_8));
        String payload = b64u(claims.getBytes(StandardCharsets.UTF_8));
        byte[] signing = (header + "." + payload).getBytes(StandardCharsets.UTF_8);

        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(kp.getPrivate(), Randoms.SECURE);
        signer.update(signing);
        byte[] rawSig = derToRaw(signer.sign());

        String token = header + "." + payload + "." + b64u(rawSig);
        return "{\"token\":\"" + token + "\",\"jwks\":{\"keys\":[" + jwk + "]},\"kid\":\"" + kid +
            "\",\"claims\":" + claims + "}";
    }

    private static String jwks() throws Exception {
        KeyPair kp = genP256KeyPair();
        ECPublicKey pub = (ECPublicKey) kp.getPublic();
        String kid = UUID.randomUUID().toString().substring(0, 8);
        String xB64 = b64uBigInt(pub.getW().getAffineX());
        String yB64 = b64uBigInt(pub.getW().getAffineY());
        return "{\"keys\":[{\"kty\":\"EC\",\"crv\":\"P-256\",\"x\":\"" + xB64 + "\",\"y\":\"" + yB64 +
            "\",\"kid\":\"" + kid + "\",\"use\":\"sig\",\"alg\":\"ES256\"}]}";
    }

    private static String oidcToken(ThreadLocalRandom rng) throws Exception {
        byte[] secret = new byte[32];
        Randoms.SECURE.nextBytes(secret);
        String claims = oidcClaimsJson(rng);
        String header = b64u("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = b64u(claims.getBytes(StandardCharsets.UTF_8));
        byte[] signing = (header + "." + payload).getBytes(StandardCharsets.UTF_8);
        byte[] sig = CryptoFuzzGen.hmacSha256(secret, signing);
        return header + "." + payload + "." + b64u(sig);
    }

    /** Convert DER-encoded ECDSA signature to raw R||S (64 bytes) for JWT ES256. */
    private static byte[] derToRaw(byte[] der) {
        // DER: 0x30 <totalLen> 0x02 <rLen> <r...> 0x02 <sLen> <s...>
        int pos = 2;
        int rLen = der[pos + 1] & 0xFF;
        byte[] r = Arrays.copyOfRange(der, pos + 2, pos + 2 + rLen);
        pos = pos + 2 + rLen;
        int sLen = der[pos + 1] & 0xFF;
        byte[] s = Arrays.copyOfRange(der, pos + 2, pos + 2 + sLen);

        byte[] out = new byte[64];
        // R and S may have a leading 0x00 byte (positive BigInteger encoding)
        if (r.length > 32) r = Arrays.copyOfRange(r, r.length - 32, r.length);
        if (s.length > 32) s = Arrays.copyOfRange(s, s.length - 32, s.length);
        System.arraycopy(r, 0, out, 32 - r.length, r.length);
        System.arraycopy(s, 0, out, 64 - s.length, s.length);
        return out;
    }

    private static String b64u(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static String b64uBigInt(BigInteger v) {
        byte[] raw = v.toByteArray();
        byte[] out = new byte[32];
        if (raw.length >= 32) {
            System.arraycopy(raw, raw.length - 32, out, 0, 32);
        } else {
            System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
        }
        return b64u(out);
    }
}
