package com.mockjutsu.jmeter.generators;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/** Security — CEF log, X.509 stub, pcap hex. Mirrors security.py. */
public final class SecurityGen {
    private SecurityGen() {}
    private static final SecureRandom SEC = new SecureRandom();
    private static final String[] SEVERITIES = {"Low","Medium","High","Critical","Unknown"};
    private static final String[] VENDORS = {"MockVendor","TestSecurity","FakeFW","SimAV"};
    private static final String[] EVENT_NAMES = {"PortScan","BruteForce","SQLInjection","XSSAttempt","DDoS","Malware","Phishing"};

    private static final String PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}|;:,.<>?";

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "cef_log"      -> cefLog(rng);
            case "x509_cert"    -> x509Cert();
            case "pcap_hex"     -> pcapHex(rng);
            case "password"     -> password(rng);
            case "password_hash" -> passwordHash();
            case "cve_id"       -> cveId(rng);
            default -> "ERROR: Unknown security type '" + type + "'";
        };
    }

    private static String password(ThreadLocalRandom rng) {
        int len = 12 + rng.nextInt(8); // 12-19 chars
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(PASSWORD_CHARS.charAt(rng.nextInt(PASSWORD_CHARS.length())));
        return sb.toString();
    }

    private static String passwordHash() {
        // bcrypt-style hash prefix + 53 chars
        byte[] bytes = new byte[20];
        SEC.nextBytes(bytes);
        String hex = bytesToHex(bytes);
        return "$2b$12$" + hex.substring(0, 22) + hex.substring(0, 31);
    }

    private static String cveId(ThreadLocalRandom rng) {
        int year = 2015 + rng.nextInt(11); // 2015-2025
        int num  = 1000 + rng.nextInt(98999);
        return "CVE-" + year + "-" + num;
    }

    private static String cefLog(ThreadLocalRandom rng) {
        String src   = MetaGen.ipv4(rng);
        String dst   = MetaGen.ipv4(rng);
        int    sport = rng.nextInt(1024, 65536);
        int    dport = rng.nextInt(1, 1024);
        String sev   = SEVERITIES[rng.nextInt(SEVERITIES.length)];
        String ename = EVENT_NAMES[rng.nextInt(EVENT_NAMES.length)];
        String vendor = VENDORS[rng.nextInt(VENDORS.length)];
        return String.format("CEF:0|%s|MockIDS|1.0|%s|%s Detected|%d|src=%s spt=%d dst=%s dpt=%d",
            vendor, "MOCK-" + rng.nextInt(1000,9999), ename, rng.nextInt(1,10), src, sport, dst, dport);
    }

    private static String x509Cert() {
        // Return JSON dict mirroring security.py x509_cert
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        byte[] serial = new byte[16]; SEC.nextBytes(serial);
        String serialHex = bytesToHex(serial);
        String[] algos = {"sha256WithRSAEncryption","sha384WithRSAEncryption","sha512WithRSAEncryption","ecdsa-with-SHA256"};
        int[] keySizes = {2048, 3072, 4096, 256};
        int idx = rng.nextInt(algos.length);
        String algo = algos[idx];
        int keySize = keySizes[idx];
        String notBefore = java.time.LocalDate.now().minusYears(1).toString();
        String notAfter  = java.time.LocalDate.now().plusYears(2).toString();
        String org = "MOCKJ-ORG-" + String.format("%04d", rng.nextInt(10000));
        return "{\"version\":3,\"serial\":\"" + serialHex + "\",\"algorithm\":\"" + algo + "\"," +
               "\"key_size\":" + keySize + ",\"subject\":\"CN=mockjutsu.test,O=" + org + ",C=TR\"," +
               "\"issuer\":\"CN=MOCKJ-CA,O=MOCKJUTSU CA,C=TR\"," +
               "\"not_before\":\"" + notBefore + "\",\"not_after\":\"" + notAfter + "\"," +
               "\"san\":[\"mockjutsu.test\",\"www.mockjutsu.test\"]," +
               "\"fingerprint\":\"" + serialHex.substring(0, 20) + "\"}";
    }

    private static String bytesToHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte v : b) sb.append(String.format("%02x", v));
        return sb.toString();
    }

    private static String pcapHex(ThreadLocalRandom rng) {
        // Ethernet frame stub: dst MAC + src MAC + EtherType(0800=IPv4) + IP + payload
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++) sb.append(String.format("%02X", rng.nextInt(256)));
        return sb.toString();
    }
}
