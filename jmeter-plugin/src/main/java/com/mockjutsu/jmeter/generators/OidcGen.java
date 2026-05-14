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
        String access  = MetaGen.jwt(rng);
        String refresh = MetaGen.jwt(rng);
        String id      = MetaGen.jwt(rng);
        return "{\"access_token\":\"" + access + "\",\"refresh_token\":\"" + refresh + "\"," +
               "\"id_token\":\"" + id + "\",\"token_type\":\"Bearer\",\"expires_in\":900}";
    }

    private static String jwks() {
        byte[] n = new byte[256]; SEC.nextBytes(n);
        byte[] e = {0x01, 0x00, 0x01}; // 65537
        String kid = UUID.randomUUID().toString().substring(0, 8);
        String nB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(n);
        String eB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(e);
        return "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\",\"kid\":\"" + kid + "\"," +
               "\"n\":\"" + nB64 + "\",\"e\":\"" + eB64 + "\"}]}";
    }
}
