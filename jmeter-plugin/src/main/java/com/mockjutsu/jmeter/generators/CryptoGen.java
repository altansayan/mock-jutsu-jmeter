package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

/** Crypto — BTC/ETH addresses, tx hash, mnemonic. Mirrors crypto.py. */
public final class CryptoGen {
    private CryptoGen() {}

    private static final SecureRandom SEC = new SecureRandom();
    private static final String BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final String[] BIP39_WORDS = {
        "abandon","ability","able","about","above","absent","absorb","abstract","absurd","abuse",
        "access","accident","account","accuse","achieve","acid","acoustic","acquire","across","act",
        "action","actor","actress","actual","adapt","add","addict","address","adjust","admit",
        "adult","advance","advice","aerobic","afford","afraid","again","age","agent","agree",
        "ahead","aim","air","airport","aisle","alarm","album","alcohol","alert","alien",
        "all","alley","allow","almost","alone","alpha","already","also","alter","always",
        "amateur","amazing","among","amount","amused","analyst","anchor","ancient","anger","angle",
        "angry","animal","ankle","announce","annual","another","answer","antenna","antique","anxiety",
        "any","apart","apology","appear","apple","approve","april","arch","arctic","area",
        "arena","argue","arm","armed","armor","army","around","arrange","arrest","arrive",
        "arrow","art","artefact","artist","artwork","ask","aspect","assault","asset","assist",
        "assume","asthma","athlete","atom","attack","attend","attitude","attract","auction","audit",
        "august","aunt","author","auto","autumn","average","avocado","avoid","awake","aware",
        "away","awesome","awful","awkward","axis","baby","balance","bamboo","banana","banner"
    };

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "btc_address"    -> btcAddress(rng);
            case "eth_address"    -> ethAddress();
            case "crypto_address" -> rng.nextBoolean() ? btcAddress(rng) : ethAddress();
            case "tx_hash"        -> "0x" + randomSecHex(32);
            case "block_hash"     -> "0x" + randomSecHex(32);
            case "mnemonic"       -> mnemonic(rng, 12);
            default -> "ERROR: Unknown crypto type '" + type + "'";
        };
    }

    // ── BTC P2PKH address (Base58, starts with 1) ─────────────────────────────

    static String btcAddress(ThreadLocalRandom rng) {
        int len = 25 + rng.nextInt(10);
        StringBuilder sb = new StringBuilder("1");
        for (int i = 1; i < len; i++) sb.append(BASE58.charAt(rng.nextInt(BASE58.length())));
        return sb.toString();
    }

    // ── ETH address (0x + 40 hex) ─────────────────────────────────────────────

    static String ethAddress() {
        byte[] bytes = new byte[20];
        SEC.nextBytes(bytes);
        StringBuilder sb = new StringBuilder("0x");
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // ── BIP-39 mnemonic ───────────────────────────────────────────────────────

    private static String mnemonic(ThreadLocalRandom rng, int wordCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) sb.append(' ');
            sb.append(BIP39_WORDS[rng.nextInt(BIP39_WORDS.length)]);
        }
        return sb.toString();
    }

    private static String randomSecHex(int bytes) {
        byte[] b = new byte[bytes];
        SEC.nextBytes(b);
        StringBuilder sb = new StringBuilder(bytes * 2);
        for (byte v : b) sb.append(String.format("%02x", v));
        return sb.toString();
    }
}
