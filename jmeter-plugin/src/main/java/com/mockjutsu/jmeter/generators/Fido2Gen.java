package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public final class Fido2Gen {
    private Fido2Gen() {}
    private static final SecureRandom SEC = new SecureRandom();

    public static String generate(String type, String locale) {
        return switch (type) {
            case "webauthn_credential" -> webauthnCredential();
            case "fido2_assertion"     -> fido2Assertion();
            default -> "ERROR: Unknown FIDO2 type '" + type + "'";
        };
    }

    private static String webauthnCredential() {
        byte[] id  = new byte[32]; SEC.nextBytes(id);
        byte[] pub = new byte[65]; SEC.nextBytes(pub); pub[0] = 0x04;
        return "{\"id\":\"" + b64url(id) + "\",\"type\":\"public-key\"," +
               "\"publicKey\":\"" + b64url(pub) + "\",\"algorithm\":-7,\"transport\":[\"internal\"]}";
    }

    private static String fido2Assertion() {
        byte[] sig    = new byte[64]; SEC.nextBytes(sig);
        byte[] authData = new byte[37]; SEC.nextBytes(authData);
        return "{\"signature\":\"" + b64url(sig) + "\",\"authenticatorData\":\"" + b64url(authData) + "\"," +
               "\"userHandle\":null,\"clientDataJSON\":\"" + b64url("{\"type\":\"webauthn.get\"}".getBytes()) + "\"}";
    }

    private static String b64url(byte[] b) { return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }
}
