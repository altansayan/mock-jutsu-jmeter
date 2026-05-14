package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** Communication — phone, email, address, plate. Mirrors communication.py. */
public final class CommunicationGen {

    private CommunicationGen() {}

    private static final String[] TR_CITIES   = {"İstanbul","Ankara","İzmir","Bursa","Antalya","Konya","Gaziantep","Mersin"};
    private static final String[] DE_CITIES   = {"Berlin","München","Hamburg","Köln","Frankfurt","Stuttgart","Düsseldorf"};
    private static final String[] FR_CITIES   = {"Paris","Lyon","Marseille","Toulouse","Nice","Nantes","Strasbourg"};
    private static final String[] UK_CITIES   = {"London","Birmingham","Leeds","Glasgow","Sheffield","Bradford","Edinburgh"};
    private static final String[] US_CITIES   = {"New York","Los Angeles","Chicago","Houston","Phoenix","Philadelphia"};
    private static final String[] RU_CITIES   = {"Москва","Санкт-Петербург","Новосибирск","Екатеринбург","Казань"};

    private static final String[] TR_STREETS  = {"Atatürk Cad.","İstiklal Cad.","Bağdat Cad.","Cumhuriyet Cad.","Mevlana Cad."};
    private static final String[] DE_STREETS  = {"Hauptstraße","Bahnhofstraße","Bergstraße","Dorfstraße","Kirchstraße"};
    private static final String[] FR_STREETS  = {"Rue de la Paix","Avenue Victor Hugo","Boulevard Haussmann","Rue Rivoli"};
    private static final String[] UK_STREETS  = {"High Street","Station Road","Church Lane","Park Avenue","Victoria Road"};
    private static final String[] US_STREETS  = {"Main St","Oak Ave","Maple Dr","Cedar Ln","Elm St","Pine Rd"};
    private static final String[] RU_STREETS  = {"ул. Ленина","ул. Мира","Советская ул.","Пушкинская ул."};

    private static final String[] EMAIL_DOMAINS_TR = {"gmail.com","hotmail.com","yahoo.com","outlook.com","yandex.com"};
    private static final String[] EMAIL_DOMAINS_US = {"gmail.com","yahoo.com","outlook.com","hotmail.com","icloud.com"};
    private static final String[] EMAIL_DOMAINS_DE = {"gmail.de","web.de","gmx.de","t-online.de","freenet.de"};
    private static final String[] EMAIL_DOMAINS_FR = {"gmail.com","laposte.net","orange.fr","free.fr","sfr.fr"};
    private static final String[] EMAIL_DOMAINS_UK = {"gmail.com","yahoo.co.uk","hotmail.co.uk","outlook.com","btinternet.com"};
    private static final String[] EMAIL_DOMAINS_RU = {"gmail.com","mail.ru","yandex.ru","rambler.ru","bk.ru"};

    private static final String[] TR_PLATES_PREFIXES;
    static {
        TR_PLATES_PREFIXES = new String[81];
        for (int i = 1; i <= 81; i++) TR_PLATES_PREFIXES[i-1] = String.format("%02d", i);
    }

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "phone"         -> phone(rng, locale);
            case "phone_country" -> phoneCountry(locale);
            case "phone_area"    -> phoneArea(rng, locale);
            case "phone_local"   -> phoneLocal(rng, locale);
            case "address_city"  -> city(rng, locale);
            case "address_street"-> street(rng, locale);
            case "address_full"  -> addressFull(rng, locale);
            case "postalcode"    -> postalCode(rng, locale);
            case "plate"         -> plate(rng, locale);
            case "email"         -> email(rng, locale);
            default              -> "ERROR: Unknown comm type '" + type + "'";
        };
    }

    static String phone(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> "+90 5" + rng.nextInt(0,10) + rng.nextInt(0,10) + " " +
                         String.format("%03d", rng.nextInt(100,999)) + " " + String.format("%04d", rng.nextInt(1000,9999));
            case "US" -> "+1 " + rng.nextInt(200,999) + " " + rng.nextInt(200,999) + " " + String.format("%04d", rng.nextInt(1000,9999));
            case "DE" -> "+49 " + rng.nextInt(151,179) + " " + String.format("%07d", rng.nextInt(1000000,9999999));
            case "FR" -> "+33 " + rng.nextInt(6,8) + " " + String.format("%08d", rng.nextInt(10000000,99999999));
            case "UK" -> "+44 " + rng.nextInt(7400,7999) + " " + String.format("%06d", rng.nextInt(100000,999999));
            case "RU" -> "+7 9" + rng.nextInt(0,10) + rng.nextInt(0,10) + " " +
                         String.format("%03d", rng.nextInt(100,999)) + " " + String.format("%04d", rng.nextInt(1000,9999));
            default   -> phone(rng, "TR");
        };
    }

    private static String phoneCountry(String locale) {
        return switch (locale) { case "US" -> "+1"; case "DE" -> "+49"; case "FR" -> "+33"; case "UK" -> "+44"; case "RU" -> "+7"; default -> "+90"; };
    }

    private static String phoneArea(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> "5" + rng.nextInt(0,10) + rng.nextInt(0,10);
            case "US" -> String.valueOf(rng.nextInt(200,999));
            default   -> String.valueOf(rng.nextInt(100,999));
        };
    }

    private static String phoneLocal(ThreadLocalRandom rng, String locale) {
        return String.format("%07d", rng.nextInt(1000000,9999999));
    }

    static String city(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> pick(rng, TR_CITIES);
            case "DE" -> pick(rng, DE_CITIES);
            case "FR" -> pick(rng, FR_CITIES);
            case "UK" -> pick(rng, UK_CITIES);
            case "US" -> pick(rng, US_CITIES);
            case "RU" -> pick(rng, RU_CITIES);
            default   -> pick(rng, TR_CITIES);
        };
    }

    private static String street(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> rng.nextInt(1,200) + " " + pick(rng, TR_STREETS);
            case "DE" -> pick(rng, DE_STREETS) + " " + rng.nextInt(1,150);
            case "FR" -> rng.nextInt(1,200) + " " + pick(rng, FR_STREETS);
            case "UK" -> rng.nextInt(1,200) + " " + pick(rng, UK_STREETS);
            case "US" -> rng.nextInt(100,9999) + " " + pick(rng, US_STREETS);
            case "RU" -> pick(rng, RU_STREETS) + ", д." + rng.nextInt(1,200);
            default   -> rng.nextInt(1,200) + " " + pick(rng, TR_STREETS);
        };
    }

    private static String addressFull(ThreadLocalRandom rng, String locale) {
        return street(rng, locale) + ", " + city(rng, locale) + " " + postalCode(rng, locale);
    }

    static String postalCode(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> String.format("%05d", rng.nextInt(10000, 99999));
            case "US" -> String.format("%05d", rng.nextInt(10000, 99999));
            case "DE" -> String.format("%05d", rng.nextInt(10000, 99999));
            case "FR" -> String.format("%05d", rng.nextInt(10000, 99999));
            case "UK" -> randomAlpha(rng, 2) + rng.nextInt(1,9) + " " + rng.nextInt(1,9) + randomAlpha(rng, 2);
            case "RU" -> String.format("%06d", rng.nextInt(100000, 999999));
            default   -> String.format("%05d", rng.nextInt(10000, 99999));
        };
    }

    private static String plate(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> TR_PLATES_PREFIXES[rng.nextInt(81)] + " " + randomAlpha(rng, 1 + rng.nextInt(3)) + " " + String.format("%04d", rng.nextInt(10, 9999));
            case "US" -> randomAlpha(rng, 3) + rng.nextInt(1000, 9999);
            case "DE" -> randomAlpha(rng, 2) + " " + randomAlpha(rng, 2) + " " + rng.nextInt(100, 9999);
            case "FR" -> String.format("%02d%c%c-%03d-%02c%c", rng.nextInt(10,99), (char)('A'+rng.nextInt(26)),
                         (char)('A'+rng.nextInt(26)), rng.nextInt(100,999), (char)('A'+rng.nextInt(26)),(char)('A'+rng.nextInt(26)));
            case "UK" -> randomAlpha(rng, 2) + rng.nextInt(10,69) + " " + randomAlpha(rng, 3);
            case "RU" -> randomCyrillicPlate(rng);
            default   -> TR_PLATES_PREFIXES[rng.nextInt(81)] + " " + randomAlpha(rng, 2) + " " + String.format("%04d", rng.nextInt(1000,9999));
        };
    }

    private static String randomCyrillicPlate(ThreadLocalRandom rng) {
        String[] cyr = {"А","В","Е","К","М","Н","О","Р","С","Т","У","Х"};
        return cyr[rng.nextInt(cyr.length)] + String.format("%03d", rng.nextInt(100,999)) +
               cyr[rng.nextInt(cyr.length)] + cyr[rng.nextInt(cyr.length)] + " " +
               rng.nextInt(10,199);
    }

    static String email(ThreadLocalRandom rng, String locale) {
        String[] domains = switch (locale) {
            case "US" -> EMAIL_DOMAINS_US;
            case "DE" -> EMAIL_DOMAINS_DE;
            case "FR" -> EMAIL_DOMAINS_FR;
            case "UK" -> EMAIL_DOMAINS_UK;
            case "RU" -> EMAIL_DOMAINS_RU;
            default   -> EMAIL_DOMAINS_TR;
        };
        String name = randomAlpha(rng, 5).toLowerCase() + rng.nextInt(10, 999);
        return name + "@" + pick(rng, domains);
    }

    private static String randomAlpha(ThreadLocalRandom rng, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append((char)('A' + rng.nextInt(26)));
        return sb.toString();
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }
}
