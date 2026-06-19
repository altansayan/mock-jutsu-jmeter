package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class OidcGen {
    private OidcGen() {}
    private static final SecureRandom SEC = new SecureRandom();

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "oidc_token_set" -> tokenSet(rng, locale);
            case "jwks"           -> jwks();
            case "oidc_token"     -> MetaGen.jwt(rng);
            default -> "ERROR: Unknown OIDC type '" + type + "'";
        };
    }

    private static String tokenSet(ThreadLocalRandom rng, String locale) {
        // Mirrors oidc.py: {token, claims, jwks, kid}
        String token = MetaGen.jwt(rng);
        String kid   = UUID.randomUUID().toString().substring(0, 8);
        String claims = "{\\\"iss\\\":\\\"https://mockjutsu.test\\\",\\\"sub\\\":\\\"MOCKJ-" +
                        String.format("%06d", rng.nextInt(1000000)) + "\\\",\\\"aud\\\":\\\"mockjutsu-client\\\"," +
                        "\\\"exp\\\":" + (System.currentTimeMillis()/1000 + 900) + ",\\\"iat\\\":" +
                        (System.currentTimeMillis()/1000) + "}";
        byte[] x = new byte[32]; SEC.nextBytes(x);
        byte[] y = new byte[32]; SEC.nextBytes(y);
        String xB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(x);
        String yB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(y);
        String jwks = "{\\\"keys\\\":[{\\\"kty\\\":\\\"EC\\\",\\\"use\\\":\\\"sig\\\",\\\"kid\\\":\\\"" + kid + "\\\"," +
                      "\\\"crv\\\":\\\"P-256\\\",\\\"x\\\":\\\"" + xB64 + "\\\",\\\"y\\\":\\\"" + yB64 + "\\\"}]}";
        return "{\"token\":\"" + token + "\",\"claims\":\"" + claims + "\",\"jwks\":\"" + jwks + "\",\"kid\":\"" + kid + "\"}";
    }

    private static String jwks() {
        // EC P-256 key: x and y are 32-byte uncompressed point coordinates
        byte[] x = new byte[32]; SEC.nextBytes(x);
        byte[] y = new byte[32]; SEC.nextBytes(y);
        String kid = UUID.randomUUID().toString().substring(0, 8);
        String xB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(x);
        String yB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(y);
        return "{\"keys\":[{\"kty\":\"EC\",\"use\":\"sig\",\"alg\":\"ES256\",\"kid\":\"" + kid + "\"," +
               "\"crv\":\"P-256\",\"x\":\"" + xB64 + "\",\"y\":\"" + yB64 + "\"}]}";
    }
}
