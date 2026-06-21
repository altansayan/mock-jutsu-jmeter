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

    private static final String[] DEFI_PROTOCOLS  = {"Uniswap","Aave","Compound","Curve","Maker","Balancer","Yearn","Synthetix","dYdX","Lido","Convex","GMX"};
    private static final String[] BLOCKCHAIN_NETS = {"Ethereum","Bitcoin","Polygon","Arbitrum","Optimism","Solana","Avalanche","BNB Chain","Base","zkSync Era","Starknet","Sui"};
    private static final String[] WALLET_LABELS   = {"Main Wallet","Trading","Savings","Cold Storage","DeFi","NFT Vault","Operations","Reserve"};
    private static final String[] DEFI_POSITIONS  = {"Liquidity Provider","Lending","Borrowing","Staking","Yield Farming","Vault","Perpetual"};
    private static final String[] CRYPTO_NAMES    = {"Bitcoin","Ethereum","Tether","BNB","XRP","USD Coin","Cardano","Dogecoin","Solana","TRON","Polkadot","Polygon"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "btc_address"          -> btcAddress(rng);
            case "eth_address"          -> ethAddress();
            case "crypto_address"       -> rng.nextBoolean() ? btcAddress(rng) : ethAddress();
            case "tx_hash"              -> "0x" + randomSecHex(32);
            case "block_hash"           -> "0x" + randomSecHex(32);
            case "mnemonic"             -> mnemonic(rng, 12);
            case "nft_token_id"         -> String.valueOf(rng.nextLong(1_000_000_000L, 999_999_999_999_999_999L));
            case "gas_price"            -> String.valueOf(rng.nextInt(1, 200));
            case "gas_limit"            -> String.valueOf(21000 + rng.nextInt(479000));
            case "defi_protocol_name"   -> DEFI_PROTOCOLS[rng.nextInt(DEFI_PROTOCOLS.length)];
            case "blockchain_network"   -> BLOCKCHAIN_NETS[rng.nextInt(BLOCKCHAIN_NETS.length)];
            case "wallet_label"         -> WALLET_LABELS[rng.nextInt(WALLET_LABELS.length)];
            case "defi_position_type"   -> DEFI_POSITIONS[rng.nextInt(DEFI_POSITIONS.length)];
            case "cryptocurrency_name"  -> CRYPTO_NAMES[rng.nextInt(CRYPTO_NAMES.length)];
            case "liquidity_pool_id"    -> "0x" + randomSecHex(20);
            case "liquidity_pool_id_masked" -> "0x" + "****" + randomSecHex(6);
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

    // ── ETH address (0x + 40 hex, mock EIP-55 checksum) ──────────────────────
    // EIP-55: each hex letter's case is determined by the Keccak-256 hash of the
    // lowercase address. As a mock implementation without a Keccak library we use
    // a pseudo-deterministic rule: uppercase if character index is even, lowercase
    // if odd. This preserves the mixed-case checksum appearance required by EIP-55.

    static String ethAddress() {
        byte[] bytes = new byte[20];
        SEC.nextBytes(bytes);
        // Build raw lowercase hex
        char[] hex = new char[40];
        for (int i = 0; i < 20; i++) {
            int hi = (bytes[i] >> 4) & 0xF;
            int lo = bytes[i] & 0xF;
            hex[i * 2]     = "0123456789abcdef".charAt(hi);
            hex[i * 2 + 1] = "0123456789abcdef".charAt(lo);
        }
        // Apply mock EIP-55: even index → uppercase, odd index → lowercase
        StringBuilder sb = new StringBuilder("0x");
        for (int i = 0; i < 40; i++) {
            char c = hex[i];
            if (c >= 'a' && c <= 'f') {
                sb.append((i % 2 == 0) ? Character.toUpperCase(c) : c);
            } else {
                sb.append(c); // digits stay as-is
            }
        }
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
