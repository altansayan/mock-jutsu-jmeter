package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/** FIDO2/WebAuthn — credential and assertion. Mirrors fido2.py. */
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

    // webauthn_credential → nested structure matching Python fido2.py
    // {"id":b64url,"rawId":b64url,"type":"public-key",
    //  "response":{"clientDataJSON":b64url,"attestationObject":b64url},
    //  "clientExtensionResults":{}}
    private static String webauthnCredential() {
        byte[] credId = new byte[32]; SEC.nextBytes(credId);
        String b64Id  = b64url(credId);

        // clientDataJSON: JSON bytes base64url-encoded
        String clientData = String.format(
            "{\"type\":\"webauthn.create\",\"challenge\":\"%s\",\"origin\":\"https://mock-jutsu.io\"}",
            randomB64url(16));
        String clientDataJSON = b64url(clientData.getBytes());

        // attestationObject: CBOR-like — we generate realistic-length opaque bytes
        byte[] attObj = new byte[200]; SEC.nextBytes(attObj);
        // Set first byte to 0xA3 (CBOR map, 3 items) to look plausible
        attObj[0] = (byte) 0xA3;
        String attestationObject = b64url(attObj);

        return String.format(
            "{\"id\":\"%s\",\"rawId\":\"%s\",\"type\":\"public-key\"," +
            "\"response\":{\"clientDataJSON\":\"%s\",\"attestationObject\":\"%s\"}," +
            "\"clientExtensionResults\":{}}",
            b64Id, b64Id, clientDataJSON, attestationObject);
    }

    // fido2_assertion → nested structure matching Python fido2.py
    // {"id":b64url,"rawId":b64url,"type":"public-key",
    //  "response":{"clientDataJSON":b64url,"authenticatorData":b64url,
    //              "signature":b64url,"userHandle":b64url},
    //  "clientExtensionResults":{}}
    private static String fido2Assertion() {
        byte[] credId = new byte[32]; SEC.nextBytes(credId);
        String b64Id  = b64url(credId);

        // clientDataJSON
        String clientData = String.format(
            "{\"type\":\"webauthn.get\",\"challenge\":\"%s\",\"origin\":\"https://mock-jutsu.io\"}",
            randomB64url(16));
        String clientDataJSON = b64url(clientData.getBytes());

        // authenticatorData: rpIdHash(32) + flags(1) + counter(4) + ... = 37 bytes min
        byte[] authData = new byte[37]; SEC.nextBytes(authData);
        authData[32] = 0x05; // flags: UP=1, UV=1
        String authenticatorData = b64url(authData);

        // signature: DER ECDSA P-256 (typically 70-72 bytes)
        byte[] sig = new byte[71]; SEC.nextBytes(sig);
        sig[0] = 0x30; sig[1] = 0x45; // DER sequence header
        String signature = b64url(sig);

        // userHandle: random 16 bytes
        byte[] userHandle = new byte[16]; SEC.nextBytes(userHandle);
        String userHandleB64 = b64url(userHandle);

        return String.format(
            "{\"id\":\"%s\",\"rawId\":\"%s\",\"type\":\"public-key\"," +
            "\"response\":{\"clientDataJSON\":\"%s\",\"authenticatorData\":\"%s\"," +
            "\"signature\":\"%s\",\"userHandle\":\"%s\"}," +
            "\"clientExtensionResults\":{}}",
            b64Id, b64Id, clientDataJSON, authenticatorData, signature, userHandleB64);
    }

    private static String b64url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static String randomB64url(int bytes) {
        byte[] b = new byte[bytes]; SEC.nextBytes(b);
        return b64url(b);
    }
}
