package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class CorporateGen {
    private CorporateGen() {}

    private static final String[] ADJ_TR  = {"Novex","Apex","Zircon","Orbit","Vertex","Atlas","Crest","Ridge","Peak","Titan"};
    private static final String[] NOUN_TR = {"Teknoloji","Finans","Yazılım","Sistem","Çözüm","Grup","Holding","Yatırım"};
    private static final String[] SFX_TR  = {"A.Ş.","Ltd. Şti.","Holding","Grup","Teknoloji A.Ş."};

    private static final String[] ADJ_DE  = {"Rhine","Baltic","Elbe","Spree","Mosel","Alpen","Nord","Süd"};
    private static final String[] NOUN_DE = {"Kapital","Systeme","Lösungen","Technik","Dienste","Gruppe","Werk","Logistik"};
    private static final String[] SFX_DE  = {"GmbH","AG","KG","UG","GmbH & Co. KG"};

    private static final String[] ADJ_FR  = {"Loire","Seine","Rhône","Garonne","Alsace","Bretagne","Nord","Sud"};
    private static final String[] NOUN_FR = {"Capital","Systèmes","Solutions","Technologie","Services","Groupe","Logistique","Finance"};
    private static final String[] SFX_FR  = {"SARL","SA","SAS","SNC","EURL"};

    private static final String[] ADJ_UK  = {"Thames","Severn","Tyne","Mersey","Avon","Eden","Celtic","Royal"};
    private static final String[] NOUN_UK = {"Capital","Systems","Solutions","Technology","Services","Group","Logistics","Finance"};
    private static final String[] SFX_UK  = {"Ltd","PLC","LLP","Group","Holdings"};

    private static final String[] ADJ_US  = {"Atlas","Summit","Crest","Pinnacle","Ridge","Peak","Apex","Nexus"};
    private static final String[] NOUN_US = {"Capital","Systems","Solutions","Technology","Services","Group","Logistics","Financial"};
    private static final String[] SFX_US  = {"Inc.","LLC","Corp.","LP","Group"};

    private static final String[] ADJ_RU  = {"Волга","Нева","Урал","Обь","Лена","Байкал","Север","Восток"};
    private static final String[] NOUN_RU = {"Капитал","Системы","Решения","Технологии","Сервис","Группа","Логистика","Финанс"};
    private static final String[] SFX_RU  = {"ООО","АО","ПАО","ГК","Холдинг"};

    private static final String[] JOBS_TR = {
        "Yazılım Mühendisi","Ürün Yöneticisi","QA Yöneticisi","Veri Bilimcisi","DevOps Mühendisi",
        "Güvenlik Analisti","Backend Geliştirici","Frontend Geliştirici","Scrum Master","CTO",
        "Finansal Analist","Risk Yöneticisi","Uyum Görevlisi","İş Analisti","Sistem Mimarı"
    };
    private static final String[] JOBS_US = {
        "Software Engineer","Product Manager","QA Manager","Data Scientist","DevOps Engineer",
        "Security Analyst","Backend Developer","Frontend Developer","Scrum Master","CTO",
        "Financial Analyst","Risk Manager","Compliance Officer","Business Analyst","Architect"
    };
    private static final String[] JOBS_DE = {
        "Softwareentwickler","Produktmanager","QA-Manager","Datenwissenschaftler","DevOps-Ingenieur",
        "Sicherheitsanalyst","Backend-Entwickler","Frontend-Entwickler","Scrum Master","CTO",
        "Finanzanalyst","Risikomanager","Compliance Officer","Business Analyst","Architekt"
    };
    private static final String[] JOBS_FR = {
        "Ingénieur logiciel","Chef de produit","Responsable QA","Data Scientist","Ingénieur DevOps",
        "Analyste sécurité","Développeur backend","Développeur frontend","Scrum Master","DSI",
        "Analyste financier","Responsable risques","Responsable conformité","Analyste métier","Architecte"
    };
    private static final String[] JOBS_UK = {
        "Software Engineer","Product Manager","QA Manager","Data Scientist","DevOps Engineer",
        "Security Analyst","Backend Developer","Frontend Developer","Scrum Master","CTO",
        "Financial Analyst","Risk Manager","Compliance Officer","Business Analyst","Solutions Architect"
    };
    private static final String[] JOBS_RU = {
        "Программный инженер","Менеджер продукта","QA-менеджер","Учёный по данным","DevOps-инженер",
        "Аналитик безопасности","Backend-разработчик","Frontend-разработчик","Scrum Master","CTO",
        "Финансовый аналитик","Риск-менеджер","Офицер по комплаенсу","Бизнес-аналитик","Архитектор"
    };

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "company_name"                  -> companyName(rng, locale);
            case "job_title","jobtitle","occupation" -> jobTitle(rng, locale);
            default -> "ERROR: Unknown corporate type '" + type + "'";
        };
    }

    private static String companyName(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> pick(rng, ADJ_TR) + " " + pick(rng, NOUN_TR) + " " + pick(rng, SFX_TR);
            case "DE" -> pick(rng, ADJ_DE) + " " + pick(rng, NOUN_DE) + " " + pick(rng, SFX_DE);
            case "FR" -> pick(rng, ADJ_FR) + " " + pick(rng, NOUN_FR) + " " + pick(rng, SFX_FR);
            case "UK" -> pick(rng, ADJ_UK) + " " + pick(rng, NOUN_UK) + " " + pick(rng, SFX_UK);
            case "US" -> pick(rng, ADJ_US) + " " + pick(rng, NOUN_US) + " " + pick(rng, SFX_US);
            case "RU" -> pick(rng, SFX_RU) + " «" + pick(rng, ADJ_RU) + " " + pick(rng, NOUN_RU) + "»";
            default   -> pick(rng, ADJ_TR) + " " + pick(rng, NOUN_TR) + " " + pick(rng, SFX_TR);
        };
    }

    private static String jobTitle(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> pick(rng, JOBS_TR);
            case "US" -> pick(rng, JOBS_US);
            case "DE" -> pick(rng, JOBS_DE);
            case "FR" -> pick(rng, JOBS_FR);
            case "UK" -> pick(rng, JOBS_UK);
            case "RU" -> pick(rng, JOBS_RU);
            default   -> pick(rng, JOBS_TR);
        };
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) { return arr[rng.nextInt(arr.length)]; }
}
