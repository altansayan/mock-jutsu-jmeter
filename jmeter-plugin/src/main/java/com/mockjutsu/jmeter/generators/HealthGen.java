package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** Health — NHS Modulo 11, ICD-10, NPI Luhn, HL7, FHIR. Mirrors health.py. */
public final class HealthGen {

    private HealthGen() {}

    private static final String[] BLOOD_TYPES = {"A+","A-","B+","B-","AB+","AB-","O+","O-"};

    private static final String[] ICD10_CHAPTERS = {
        "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T"
    };

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "blood_type","bloodtype" -> pick(rng, BLOOD_TYPES);
            case "nhs_number","nhsnumber" -> nhsNumber(rng);
            case "icd10"                  -> icd10(rng);
            case "height"                 -> String.format("%.1f cm", 150.0 + rng.nextDouble(0, 60));
            case "weight"                 -> String.format("%.1f kg", 50.0 + rng.nextDouble(0, 80));
            case "bmi"                    -> String.format("%.1f", 18.5 + rng.nextDouble(0, 22));
            case "npi"                    -> npi(rng);
            case "hl7_message"            -> hl7Message(rng);
            case "fhir_patient"           -> fhirPatient(rng, locale);
            case "dicom_uid"              -> dicomUid(rng);
            default                       -> "ERROR: Unknown health type '" + type + "'";
        };
    }

    // ── NHS Number — Modulo 11 checksum ──────────────────────────────────────

    static String nhsNumber(ThreadLocalRandom rng) {
        int[] d = new int[10];
        while (true) {
            for (int i = 0; i < 9; i++) d[i] = rng.nextInt(0, 10);
            int[] weights = {10, 9, 8, 7, 6, 5, 4, 3, 2};
            int total = 0;
            for (int i = 0; i < 9; i++) total += d[i] * weights[i];
            int rem   = total % 11;
            int check = 11 - rem;
            if (check == 10) continue;
            if (check == 11) check = 0;
            d[9] = check;
            break;
        }
        return String.format("%d%d%d %d%d%d %d%d%d%d",
            d[0],d[1],d[2], d[3],d[4],d[5], d[6],d[7],d[8],d[9]);
    }

    // ── ICD-10 code ───────────────────────────────────────────────────────────

    private static String icd10(ThreadLocalRandom rng) {
        String letter = ICD10_CHAPTERS[rng.nextInt(ICD10_CHAPTERS.length)];
        int num = rng.nextInt(0, 100);
        if (rng.nextBoolean()) {
            return String.format("%s%02d.%d", letter, num, rng.nextInt(0, 10));
        }
        return String.format("%s%02d", letter, num);
    }

    // ── NPI — US National Provider Identifier (10 digits, Luhn on prefix) ────

    static String npi(ThreadLocalRandom rng) {
        // NPI: 10 digits where Luhn applied to "80840" + first 9 NPI digits
        int[] d = new int[9];
        d[0] = rng.nextInt(1, 10);
        for (int i = 1; i < 9; i++) d[i] = rng.nextInt(0, 10);
        StringBuilder sb = new StringBuilder("80840");
        for (int v : d) sb.append(v);
        int check = IdentityGen.luhnCheckDigit(sb.toString());
        sb = new StringBuilder();
        for (int v : d) sb.append(v);
        sb.append(check);
        return sb.toString();
    }

    // ── HL7 v2.x message skeleton ─────────────────────────────────────────────

    private static String hl7Message(ThreadLocalRandom rng) {
        String msgId   = "MOCKJ" + String.format("%011d", rng.nextLong(10000000000L, 99999999999L));
        String ts      = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String patId   = String.format("%08d", rng.nextInt(10000000, 99999999));
        return "MSH|^~\\&|MOCKJUTSU|HOSPITAL|EHR|MOCKLAB|" + ts + "||ADT^A01|" + msgId + "|P|2.5\r" +
               "EVN|A01|" + ts + "\r" +
               "PID|1||" + patId + "^^^MOCKJUTSU||MOCK^PATIENT||19800101|M|||123 MOCK ST^^MOCK CITY^MC^12345||+1-555-0100\r" +
               "PV1|1|I|WARD^301^A|||||||||||||||" + patId + "||||||||||||||||||||||||||20240101120000";
    }

    // ── FHIR Patient resource ─────────────────────────────────────────────────

    private static String fhirPatient(ThreadLocalRandom rng, String locale) {
        String id   = java.util.UUID.randomUUID().toString();
        String fn   = IdentityGen.firstname(rng, locale, "");
        String ln   = IdentityGen.lastname(rng, locale, "");
        String dob  = (1950 + rng.nextInt(50)) + "-" + String.format("%02d", rng.nextInt(1,13)) + "-" + String.format("%02d", rng.nextInt(1,29));
        return "{\"resourceType\":\"Patient\",\"id\":\"" + id + "\"," +
               "\"name\":[{\"use\":\"official\",\"family\":\"" + ln + "\",\"given\":[\"" + fn + "\"]}]," +
               "\"gender\":\"" + (rng.nextBoolean() ? "male" : "female") + "\"," +
               "\"birthDate\":\"" + dob + "\"}";
    }

    // ── DICOM UID ─────────────────────────────────────────────────────────────
    // Root 2.25 = ISO/IEC 9834-8 UUID-based synthetic UID (mirrors health.py _DICOM_ROOT)

    private static String dicomUid(ThreadLocalRandom rng) {
        // Generate a large random integer in [0, 2^128) to fill the UUID numeric space
        long hi = rng.nextLong(0, Long.MAX_VALUE);
        long lo = rng.nextLong(0, Long.MAX_VALUE);
        java.math.BigInteger uid = java.math.BigInteger.valueOf(hi)
                .shiftLeft(63)
                .add(java.math.BigInteger.valueOf(lo).abs());
        return "2.25." + uid.toString();
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }
}
