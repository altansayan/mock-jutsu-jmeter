package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

public final class WalletGen {
    private WalletGen() {}
    private static final SecureRandom SEC = new SecureRandom();

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "eth_wallet" -> ethWallet();
            case "btc_wallet" -> btcWallet(rng);
            case "sol_wallet" -> solWallet();
            default -> "ERROR: Unknown wallet type '" + type + "'";
        };
    }

    private static String ethWallet() {
        // Mirrors wallet.py: {address, private_key, public_key}
        byte[] privKey = new byte[32]; SEC.nextBytes(privKey);
        byte[] pubKey  = new byte[65]; SEC.nextBytes(pubKey); pubKey[0] = 0x04;
        byte[] addr    = new byte[20]; SEC.nextBytes(addr);
        StringBuilder addrSb = new StringBuilder("0x");
        for (byte v : addr) addrSb.append(String.format("%02x", v));
        return "{\"address\":\"" + addrSb + "\",\"private_key\":\"" + bytesToHex(privKey) +
               "\",\"public_key\":\"04" + bytesToHex(pubKey).substring(2) + "\"}";
    }

    private static String btcWallet(ThreadLocalRandom rng) {
        // Mirrors wallet.py: {address, private_key, public_key, wif}
        byte[] privKey = new byte[32]; SEC.nextBytes(privKey);
        byte[] pubKey  = new byte[33]; SEC.nextBytes(pubKey); pubKey[0] = (byte)(rng.nextBoolean() ? 0x02 : 0x03);
        String addr    = CryptoGen.btcAddress(rng);
        String wif     = base58Check(privKey, 0x80);
        return "{\"address\":\"" + addr + "\",\"private_key\":\"" + bytesToHex(privKey) +
               "\",\"public_key\":\"" + bytesToHex(pubKey) + "\",\"wif\":\"" + wif + "\"}";
    }

    private static String solWallet() {
        // Mirrors wallet.py: {address, private_key, public_key, keypair}
        String b58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        byte[] privKey = new byte[32]; SEC.nextBytes(privKey);
        byte[] pubKey  = new byte[32]; SEC.nextBytes(pubKey);
        StringBuilder addr = new StringBuilder(44);
        for (int i = 0; i < 44; i++) addr.append(b58.charAt(rng.nextInt(b58.length())));
        String keypairB64 = java.util.Base64.getEncoder().encodeToString(concat(privKey, pubKey));
        return "{\"address\":\"" + addr + "\",\"private_key\":\"" + bytesToHex(privKey) +
               "\",\"public_key\":\"" + bytesToHex(pubKey) + "\",\"keypair\":\"" + keypairB64 + "\"}";
    }

    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(String.format("%02x", v));
        return sb.toString();
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    private static String base58Check(byte[] payload, int version) {
        // Simplified base58check (not real Bitcoin derivation — same struct)
        String alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(52);
        for (int i = 0; i < 52; i++) sb.append(alphabet.charAt(rng.nextInt(alphabet.length())));
        return sb.toString();
    }
}
