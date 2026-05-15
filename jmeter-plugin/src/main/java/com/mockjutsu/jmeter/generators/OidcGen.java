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
