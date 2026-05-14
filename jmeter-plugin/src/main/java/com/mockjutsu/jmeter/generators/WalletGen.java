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
        byte[] b = new byte[20]; SEC.nextBytes(b);
        StringBuilder sb = new StringBuilder("0x");
        for (byte v : b) sb.append(String.format("%02x", v));
        return "{\"address\":\"" + sb + "\",\"network\":\"ethereum\",\"type\":\"EOA\"}";
    }

    private static String btcWallet(ThreadLocalRandom rng) {
        String addr = CryptoGen.btcAddress(rng);
        return "{\"address\":\"" + addr + "\",\"network\":\"bitcoin\",\"type\":\"P2PKH\"}";
    }

    private static String solWallet() {
        // Solana: base58 address ~44 chars
        String b58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(44);
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < 44; i++) sb.append(b58.charAt(rng.nextInt(b58.length())));
        return "{\"address\":\"" + sb + "\",\"network\":\"solana\",\"type\":\"native\"}";
    }
}
