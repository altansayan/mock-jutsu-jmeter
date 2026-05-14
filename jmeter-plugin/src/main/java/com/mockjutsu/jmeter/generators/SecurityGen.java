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

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "cef_log"  -> cefLog(rng);
            case "x509_cert"-> x509Cert();
            case "pcap_hex" -> pcapHex(rng);
            default -> "ERROR: Unknown security type '" + type + "'";
        };
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
        // PEM stub — not a real X.509 cert
        byte[] b = new byte[256];
        SEC.nextBytes(b);
        String b64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(b);
        return "-----BEGIN CERTIFICATE-----\n" + b64 + "\n-----END CERTIFICATE-----";
    }

    private static String pcapHex(ThreadLocalRandom rng) {
        // Ethernet frame stub: dst MAC + src MAC + EtherType(0800=IPv4) + IP + payload
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++) sb.append(String.format("%02X", rng.nextInt(256)));
        return sb.toString();
    }
}
