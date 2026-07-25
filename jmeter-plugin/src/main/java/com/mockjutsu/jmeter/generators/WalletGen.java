package com.mockjutsu.jmeter.generators;

import com.mockjutsu.jmeter.Randoms;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Full-format wallets for mock testing — ETH, BTC, SOL.
 * Generates cryptographically random private keys and random-looking (but not derived)
 * public keys / addresses; sufficient for load-test mock data, avoids the 8–22 ms
 * per-call cost of pure-Java BigInteger EC point multiplication.
 */
public final class WalletGen {
    private WalletGen() {}
    private static final Logger log = LoggerFactory.getLogger(WalletGen.class);

    public static String generate(String type, String locale) {
        return switch (type) {
            case "eth_wallet" -> ethWallet();
            case "btc_wallet" -> btcWallet();
            case "sol_wallet" -> solWallet();
            default -> "ERROR: Unknown wallet type '" + type + "'";
        };
    }

    private static String ethWallet() {
        try {
            byte[] privKey = new byte[32];
            Randoms.SECURE.nextBytes(privKey);
            // For mock purposes: random 64-byte "public key" (uncompressed x||y).
            // Address is correctly derived from its Keccak-256 hash — format is authentic.
            byte[] pubkeyBytes = new byte[64];
            Randoms.SECURE.nextBytes(pubkeyBytes);
            byte[] kecc = EcCrypto.keccak256(pubkeyBytes);
            byte[] last20 = Arrays.copyOfRange(kecc, 12, 32);
            String address = EcCrypto.eip55(last20);
            return "{\"private_key\":\"" + hex(privKey) +
                   "\",\"public_key\":\"04" + hex(pubkeyBytes) +
                   "\",\"address\":\"" + address + "\"}";
        } catch (Exception e) {
            log.warn("eth_wallet generation failed: {}", e.getMessage(), e);
            return "ERROR: eth_wallet generation failed: " + e.getMessage();
        }
    }

    private static String btcWallet() {
        try {
            byte[] privKey = new byte[32];
            Randoms.SECURE.nextBytes(privKey);
            // Random compressed public key (0x02/0x03 + 32 bytes) for mock purposes.
            byte[] compressed = new byte[33];
            Randoms.SECURE.nextBytes(compressed);
            compressed[0] = (byte) ((compressed[0] & 1) == 0 ? 0x02 : 0x03);
            byte[] h160 = EcCrypto.hash160(compressed);
            String address = EcCrypto.base58Check(concat(new byte[]{0x00}, h160));
            String wif = EcCrypto.base58Check(concat(concat(new byte[]{(byte) 0x80}, privKey), new byte[]{0x01}));
            return "{\"private_key\":\"" + hex(privKey) +
                   "\",\"wif\":\"" + wif +
                   "\",\"public_key\":\"" + hex(compressed) +
                   "\",\"address\":\"" + address + "\"}";
        } catch (Exception e) {
            log.warn("btc_wallet generation failed: {}", e.getMessage(), e);
            return "ERROR: btc_wallet generation failed: " + e.getMessage();
        }
    }

    private static String solWallet() {
        try {
            byte[] privKey = new byte[32];
            Randoms.SECURE.nextBytes(privKey);
            // Random 32-byte public key for mock purposes (Ed25519 format).
            byte[] pubKey = new byte[32];
            Randoms.SECURE.nextBytes(pubKey);
            String address = EcCrypto.base58Encode(pubKey);
            String keypair = EcCrypto.base58Encode(concat(privKey, pubKey));
            return "{\"private_key\":\"" + hex(privKey) +
                   "\",\"public_key\":\"" + hex(pubKey) +
                   "\",\"address\":\"" + address +
                   "\",\"keypair\":\"" + keypair + "\"}";
        } catch (Exception e) {
            log.warn("sol_wallet generation failed: {}", e.getMessage(), e);
            return "ERROR: sol_wallet generation failed: " + e.getMessage();
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    private static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte v : b) sb.append(String.format("%02x", v));
        return sb.toString();
    }
}
