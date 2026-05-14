package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class CorporateGen {
    private CorporateGen() {}

    private static final String[] SUFFIXES_TR = {"A.Ş.","Ltd. Şti.","Holding","Grup","Teknoloji"};
    private static final String[] WORDS_TR    = {"Novex","Apex","Orbit","Zircon","Vertex","Atlas","Crest","Ridge"};
    private static final String[] SUFFIXES_DE = {"GmbH","AG","KG","UG","GmbH & Co. KG"};
    private static final String[] WORDS_DE    = {"Rhine","Baltic","Elbe","Spree","Mosel","Alpen"};
    private static final String[] SUFFIXES_FR = {"SARL","SA","SAS","SNC","EURL"};
    private static final String[] WORDS_FR    = {"Loire","Seine","Rhône","Garonne","Alsace","Bretagne"};
    private static final String[] SUFFIXES_UK = {"Ltd","PLC","LLP","Group","Holdings"};
    private static final String[] WORDS_UK    = {"Thames","Severn","Tyne","Mersey","Avon","Eden"};
    private static final String[] SUFFIXES_US = {"Inc.","LLC","Corp.","LP","Group"};
    private static final String[] WORDS_US    = {"Atlas","Summit","Crest","Pinnacle","Ridge","Peak"};
    private static final String[] SUFFIXES_RU = {"ООО","АО","ПАО","ГК","Холдинг"};
    private static final String[] WORDS_RU    = {"Волга","Нева","Урал","Обь","Лена","Байкал"};

    private static final String[] JOBS = {
        "Software Engineer","Product Manager","QA Manager","Data Scientist","DevOps Engineer",
        "Security Analyst","Backend Developer","Frontend Developer","Scrum Master","CTO",
        "Financial Analyst","Risk Manager","Compliance Officer","Business Analyst","Architect"
    };

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "company_name"       -> companyName(rng, locale);
            case "job_title","jobtitle","occupation" -> pick(rng, JOBS);
            default -> "ERROR: Unknown corporate type '" + type + "'";
        };
    }

    private static String companyName(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> pick(rng, WORDS_TR) + " " + pick(rng, SUFFIXES_TR);
            case "DE" -> pick(rng, WORDS_DE) + " " + pick(rng, SUFFIXES_DE);
            case "FR" -> pick(rng, WORDS_FR) + " " + pick(rng, SUFFIXES_FR);
            case "UK" -> pick(rng, WORDS_UK) + " " + pick(rng, SUFFIXES_UK);
            case "US" -> pick(rng, WORDS_US) + " " + pick(rng, SUFFIXES_US);
            case "RU" -> pick(rng, WORDS_RU) + " " + pick(rng, SUFFIXES_RU);
            default   -> pick(rng, WORDS_TR) + " " + pick(rng, SUFFIXES_TR);
        };
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) { return arr[rng.nextInt(arr.length)]; }
}
