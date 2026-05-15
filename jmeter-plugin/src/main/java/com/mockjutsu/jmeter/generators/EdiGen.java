package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class EdiGen {
    private EdiGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "edi_850"       -> edi850(rng);
            case "edifact_orders"-> edifact(rng);
            default -> "ERROR: Unknown EDI type '" + type + "'";
        };
    }

    private static String edi850(ThreadLocalRandom rng) {
        String ctrl = String.format("%09d", rng.nextInt(100000000, 999999999));
        String po   = "PO-" + rng.nextInt(100000, 999999);
        // ISA segment: 106 karakter (ISA01–ISA16 + segment terminator)
        // ISA06 sender ID: 15 karakter (sag doldurulmus), ISA08 receiver ID: 15 karakter
        // ISA16: component element separator (>), ardindan segment terminator (|)
        // Segment sayisi: ST(1) + BEG(1) + PO1(1) + CTT(1) + SE(1) = 5
        String sender   = String.format("%-15s", "MOCKJUTSU");
        String receiver = String.format("%-15s", "PARTNER");
        return "ISA*00*          *00*          *ZZ*" + sender + "*ZZ*" + receiver + "*240101*1200*^*00401*" + ctrl + "*0*T*>|\n" +
               "GS*PO*MOCKJUTSU*PARTNER*20240101*120000*1*X*004010|\n" +
               "ST*850*0001|\n" +
               "BEG*00*SA*" + po + "**20240101|\n" +
               "PO1*1*100*EA*" + String.format("%.2f", rng.nextDouble(10,1000)) + "*PE*IT*MOCK-SKU-001|\n" +
               "CTT*1|\n" +
               "SE*5*0001|\n" +
               "GE*1*1|\n" +
               "IEA*1*" + ctrl + "|";
    }

    private static String edifact(ThreadLocalRandom rng) {
        String ctrl = String.format("%09d", rng.nextInt(0, 1000000000));
        String po   = "PO" + rng.nextInt(100000, 999999);
        return "UNB+UNOC:3+MOCKJUTSU+PARTNER+240101:1200+" + ctrl + "++ORDERS'\n" +
               "UNH+" + ctrl + "+ORDERS:D:96A:UN'\n" +
               "BGM+220+" + po + "+9'\n" +
               "DTM+137:20240101:102'\n" +
               "LIN+1++MOCK-ITEM-001:SA'\n" +
               "QTY+21:" + rng.nextInt(1,1000) + ":PCE'\n" +
               "PRI+AAA:" + String.format("%.2f", rng.nextDouble(5,500)) + ":CA'\n" +
               "UNT+8+" + ctrl + "'\n" +
               "UNZ+1+" + ctrl + "'";
    }
}
