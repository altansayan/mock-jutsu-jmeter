package com.mockjutsu.jmeter.generators;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/** Identity generator — mirrors identity.py algorithms. */
public final class IdentityGen {

    private IdentityGen() {}

    // ── Name pools ───────────────────────────────────────────────────────────

    private static final String[] TR_FIRST_M = {
        "Ahmet","Mehmet","Mustafa","Ali","Hasan","Hüseyin","İbrahim","İsmail",
        "Ömer","Yusuf","Murat","Fatih","Burak","Emre","Sercan","Kerem","Okan","Altan"
    };
    private static final String[] TR_FIRST_F = {
        "Fatma","Ayşe","Emine","Hatice","Zeynep","Elif","Selin","Merve","Büşra","Ceren",
        "Duygu","Esra","Gizem","Hande","İrem","Kübra","Neslihan","Özge"
    };
    private static final String[] TR_LAST = {
        "Yılmaz","Kaya","Demir","Şahin","Çelik","Yıldız","Yıldırım","Öztürk","Arslan",
        "Doğan","Kılıç","Aslan","Çetin","Koç","Kurt","Özcan","Polat","Erdoğan","Ayan"
    };
    private static final String[] US_FIRST_M = {
        "James","John","Robert","Michael","William","David","Richard","Joseph",
        "Thomas","Charles","Christopher","Daniel","Matthew","Anthony","Mark"
    };
    private static final String[] US_FIRST_F = {
        "Mary","Patricia","Jennifer","Linda","Barbara","Elizabeth","Susan","Jessica",
        "Sarah","Karen","Lisa","Nancy","Betty","Margaret","Sandra"
    };
    private static final String[] US_LAST = {
        "Smith","Johnson","Williams","Brown","Jones","Garcia","Miller","Davis",
        "Rodriguez","Martinez","Hernandez","Lopez","Gonzalez","Wilson","Anderson"
    };
    private static final String[] DE_FIRST_M = {
        "Thomas","Michael","Andreas","Stefan","Christian","Peter","Klaus","Werner",
        "Hans","Jürgen","Markus","Tobias","Florian","Sebastian","Felix"
    };
    private static final String[] DE_FIRST_F = {
        "Maria","Sabine","Petra","Claudia","Anna","Monika","Ursula","Christine",
        "Sandra","Martina","Julia","Laura","Jana","Sophie","Lena"
    };
    private static final String[] DE_LAST = {
        "Müller","Schmidt","Schneider","Fischer","Weber","Meyer","Wagner","Becker",
        "Schulz","Hoffmann","Schäfer","Koch","Bauer","Richter","Klein"
    };
    private static final String[] FR_FIRST_M = {
        "Jean","Pierre","Michel","Philippe","Alain","Nicolas","François","Patrick",
        "Laurent","Christophe","Julien","Thomas","Alexandre","Antoine","Mathieu"
    };
    private static final String[] FR_FIRST_F = {
        "Marie","Nathalie","Isabelle","Catherine","Sylvie","Christine","Anne","Sophie",
        "Céline","Julie","Laure","Amélie","Camille","Emma","Lucie"
    };
    private static final String[] FR_LAST = {
        "Martin","Bernard","Dubois","Thomas","Robert","Richard","Petit","Durand",
        "Leroy","Moreau","Simon","Laurent","Lefebvre","Michel","Garcia"
    };
    private static final String[] UK_FIRST_M = {
        "Oliver","Jack","Harry","George","Noah","Charlie","Jacob","Alfie",
        "Freddie","Oscar","William","James","Thomas","Henry","Leo"
    };
    private static final String[] UK_FIRST_F = {
        "Olivia","Amelia","Isla","Ava","Emily","Isabella","Mia","Poppy",
        "Ella","Lily","Jessica","Sophie","Grace","Charlotte","Alice"
    };
    private static final String[] UK_LAST = {
        "Smith","Jones","Williams","Taylor","Brown","Davies","Evans","Wilson",
        "Thomas","Roberts","Johnson","Lewis","Walker","Robinson","Wood"
    };
    private static final String[] RU_FIRST_M = {
        "Александр","Сергей","Михаил","Андрей","Дмитрий","Алексей","Иван","Николай",
        "Владимир","Артём","Максим","Павел","Евгений","Виктор","Роман"
    };
    private static final String[] RU_FIRST_F = {
        "Анастасия","Наталья","Ирина","Татьяна","Елена","Мария","Ольга","Екатерина",
        "Светлана","Людмила","Юлия","Дарья","Оксана","Алина","Виктория"
    };
    private static final String[] RU_LAST = {
        "Иванов","Смирнов","Кузнецов","Попов","Соколов","Лебедев","Козлов","Новиков",
        "Морозов","Петров","Волков","Соловьёв","Васильев","Зайцев","Павлов"
    };
    private static final String[] RU_PATRONYMIC_M = {
        "Александрович","Сергеевич","Михайлович","Андреевич","Дмитриевич",
        "Алексеевич","Иванович","Николаевич","Владимирович","Артёмович"
    };
    private static final String[] RU_PATRONYMIC_F = {
        "Александровна","Сергеевна","Михайловна","Андреевна","Дмитриевна",
        "Алексеевна","Ивановна","Николаевна","Владимировна","Артёмовна"
    };

    private static final String[] NATIONALITIES = {
        "TUR","USA","GBR","DEU","FRA","RUS","ITA","ESP","POL","NLD",
        "CHN","IND","BRA","JPN","KOR","AUS","CAN","MEX","ARG","ZAF"
    };

    // ── NIN helpers ──────────────────────────────────────────────────────────

    private static final char[] NIN_VALID = {
        'A','B','C','E','G','H','J','K','L','M','N','P','R','S','T','W','X','Y','Z'
    };
    private static final String[] NIN_FORBIDDEN_PAIRS = {
        "BG","GB","NK","KN","NT","TN","ZZ"
    };

    // ── Public API ───────────────────────────────────────────────────────────

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "tckn"         -> tckn(rng);
            case "tckn_masked"  -> tckn(rng).replaceAll("(?<=.{3}).(?=.{4})", "*");
            case "ykn"          -> ykn(rng);
            case "taxid","vkn"  -> vkn(rng);
            case "nationalid"   -> nationalid(rng, locale);
            case "ssn"          -> ssn(rng);
            case "ssn_masked"   -> ssnMasked(rng);
            case "nin"          -> nin(rng);
            case "inn"          -> inn(rng);
            case "inn_individual" -> innIndividual(rng);
            case "snils"        -> snils(rng);
            case "sgk"          -> sgk(rng);
            case "mersis"       -> mersis(rng);
            case "ein"          -> ein(rng);
            case "utr"          -> utr(rng);
            case "crn"          -> crn(rng);
            case "paye"         -> paye(rng);
            case "ust_id","ustid" -> ustId(rng);
            case "hrb"          -> hrb(rng);
            case "rvn"          -> rvn(rng);
            case "siren"        -> siren(rng);
            case "siret"        -> siret(rng);
            case "tva"          -> tva(rng);
            case "ogrn"         -> ogrn(rng);
            case "kpp"          -> kpp(rng);
            case "employer_id"  -> employerId(rng, locale);
            case "insurance_id" -> insuranceId(rng, locale);
            case "firstname"    -> firstname(rng, locale, "");
            case "lastname"     -> lastname(rng, locale, "");
            case "fullname"     -> fullname(rng, locale);
            case "patronymic"   -> patronymic(rng, locale, "");
            case "passport"     -> passport(rng, locale);
            case "license"      -> license(rng, locale);
            case "age"          -> String.valueOf(rng.nextInt(18, 80));
            case "gender"       -> rng.nextBoolean() ? "Male" : "Female";
            case "birthdate"    -> birthdate(rng);
            case "nationality"  -> NATIONALITIES[rng.nextInt(NATIONALITIES.length)];
            case "vat_number"   -> vatNumber(rng, locale);
            default             -> "ERROR: Unknown identity type '" + type + "'";
        };
    }

    // ── TCKN — Turkish Citizen ID (11 digits) ────────────────────────────────

    static String tckn(ThreadLocalRandom rng) {
        int[] d = new int[11];
        d[0] = rng.nextInt(1, 10);
        for (int i = 1; i < 9; i++) d[i] = rng.nextInt(0, 10);

        int odd  = d[0] + d[2] + d[4] + d[6] + d[8];
        int even = d[1] + d[3] + d[5] + d[7];
        d[9] = ((7 * odd) - even + 100) % 10;
        d[10] = 0;
        for (int i = 0; i < 10; i++) d[10] = (d[10] + d[i]) % 10;

        StringBuilder sb = new StringBuilder(11);
        for (int v : d) sb.append(v);
        return sb.toString();
    }

    // ── YKN — Foreign Resident ID (11 digits, starts with 9) ─────────────────

    private static String ykn(ThreadLocalRandom rng) {
        int[] d = new int[11];
        d[0] = 9;
        for (int i = 1; i < 9; i++) d[i] = rng.nextInt(0, 10);
        int odd  = d[0] + d[2] + d[4] + d[6] + d[8];
        int even = d[1] + d[3] + d[5] + d[7];
        d[9] = ((7 * odd) - even + 100) % 10;
        d[10] = 0;
        for (int i = 0; i < 10; i++) d[10] = (d[10] + d[i]) % 10;
        StringBuilder sb = new StringBuilder(11);
        for (int v : d) sb.append(v);
        return sb.toString();
    }

    // ── VKN — Turkish Tax Number (10 digits) ─────────────────────────────────

    private static String vkn(ThreadLocalRandom rng) {
        int[] d = new int[10];
        d[0] = rng.nextInt(1, 10);
        for (int i = 1; i < 9; i++) d[i] = rng.nextInt(0, 10);
        // VKN check digit algorithm (Turkish Tax ID):
        // for i=0..8: v = (d[i] + (9-i)) % 10
        //             if v != 0: c = (v * 2^(9-i)) % 9; if c == 0: c = 9
        //             else: c = 0
        // check = (10 - sum(c) % 10) % 10
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int v = (d[i] + (9 - i)) % 10;
            int c;
            if (v != 0) {
                int pow = 1;
                for (int p = 0; p < (9 - i); p++) pow *= 2;
                c = (v * pow) % 9;
                if (c == 0) c = 9;
            } else {
                c = 0;
            }
            sum += c;
        }
        d[9] = (10 - (sum % 10)) % 10;
        StringBuilder sb = new StringBuilder(10);
        for (int v : d) sb.append(v);
        return sb.toString();
    }

    // ── SSN — US Social Security Number ──────────────────────────────────────

    static String ssn(ThreadLocalRandom rng) {
        int area;
        do { area = rng.nextInt(1, 900); } while (area == 666);
        int group  = rng.nextInt(1, 100);
        int serial = rng.nextInt(1, 10000);
        return String.format("%03d-%02d-%04d", area, group, serial);
    }

    private static String ssnMasked(ThreadLocalRandom rng) {
        int serial = rng.nextInt(1, 10000);
        return String.format("***-**-%04d", serial);
    }

    // ── NIN — UK National Insurance Number ───────────────────────────────────

    static String nin(ThreadLocalRandom rng) {
        char p1, p2;
        String pair;
        do {
            p1 = NIN_VALID[rng.nextInt(NIN_VALID.length)];
            p2 = NIN_VALID[rng.nextInt(NIN_VALID.length)];
            pair = "" + p1 + p2;
        } while (isForbiddenNinPair(pair));

        StringBuilder digits = new StringBuilder(6);
        for (int i = 0; i < 6; i++) digits.append(rng.nextInt(0, 10));
        char suffix = (char) ('A' + rng.nextInt(4));
        return pair + digits + suffix;
    }

    private static boolean isForbiddenNinPair(String pair) {
        for (String fp : NIN_FORBIDDEN_PAIRS) {
            if (fp.equals(pair)) return true;
        }
        return false;
    }

    // ── Russian INN (organisation) — 10 digits ───────────────────────────────

    private static String inn(ThreadLocalRandom rng) {
        int[] d = new int[10];
        for (int i = 0; i < 9; i++) d[i] = rng.nextInt(0, 10);
        int[] w = {2, 4, 10, 3, 5, 9, 4, 6, 8};
        int sum = 0;
        for (int i = 0; i < 9; i++) sum += d[i] * w[i];
        d[9] = sum % 11 % 10;
        StringBuilder sb = new StringBuilder(10);
        for (int v : d) sb.append(v);
        return sb.toString();
    }

    // ── Russian INN individual — 12 digits ───────────────────────────────────

    private static String innIndividual(ThreadLocalRandom rng) {
        int[] d = new int[12];
        for (int i = 0; i < 10; i++) d[i] = rng.nextInt(0, 10);
        int[] w1 = {7, 2, 4, 10, 3, 5, 9, 4, 6, 8};
        int[] w2 = {3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8};
        int s1 = 0;
        for (int i = 0; i < 10; i++) s1 += d[i] * w1[i];
        d[10] = s1 % 11 % 10;
        int s2 = 0;
        for (int i = 0; i < 11; i++) s2 += d[i] * w2[i];
        d[11] = s2 % 11 % 10;
        StringBuilder sb = new StringBuilder(12);
        for (int v : d) sb.append(v);
        return sb.toString();
    }

    // ── Russian SNILS — 11 digits with check ─────────────────────────────────

    private static String snils(ThreadLocalRandom rng) {
        int[] d = new int[9];
        for (int i = 0; i < 9; i++) d[i] = rng.nextInt(0, 10);
        int sum = 0;
        for (int i = 0; i < 9; i++) sum += d[i] * (9 - i);
        int check = sum % 101 % 100;
        return String.format("%03d-%03d-%03d %02d",
            d[0]*100 + d[1]*10 + d[2],
            d[3]*100 + d[4]*10 + d[5],
            d[6]*100 + d[7]*10 + d[8],
            check);
    }

    // ── Turkish SGK — Social Security (il-seq-unit.sub-sube) ─────────────────

    private static String sgk(ThreadLocalRandom rng) {
        int il   = rng.nextInt(1, 82);          // province 01-81
        int seq  = rng.nextInt(1, 10_000_000);  // sequence 1-9999999
        int unit = rng.nextInt(1, 10);           // unit 1-9
        int sub  = rng.nextInt(1, 100);          // sub 01-99
        int sube = rng.nextInt(1, 100);          // sube 01-99
        return String.format("%02d-%07d-%d.%02d-%02d", il, seq, unit, sub, sube);
    }

    // ── Turkish MERSIS — company registration: VKN(10) + "0" + 5digits ──────

    private static String mersis(ThreadLocalRandom rng) {
        String vknStr = vkn(rng);
        String suffix = String.format("%05d", rng.nextInt(0, 100_000));
        return vknStr + "0" + suffix;  // 10 + 1 + 5 = 16 chars
    }

    // ── US EIN — Employer Identification Number ───────────────────────────────

    static String ein(ThreadLocalRandom rng) {
        return String.format("%02d-%07d", rng.nextInt(10, 99), rng.nextInt(1000000, 9999999));
    }

    // ── UK UTR — Unique Taxpayer Reference (10 digits) ───────────────────────

    private static String utr(ThreadLocalRandom rng) {
        StringBuilder sb = new StringBuilder(10);
        sb.append(rng.nextInt(1, 10));
        for (int i = 1; i < 10; i++) sb.append(rng.nextInt(0, 10));
        return sb.toString();
    }

    // ── UK CRN — Company Registration Number ─────────────────────────────────

    private static String crn(ThreadLocalRandom rng) {
        String prefix = new String[]{"","SC","NI"}[rng.nextInt(3)];
        return String.format("%s%08d", prefix, rng.nextInt(1, 99999999));
    }

    // ── UK PAYE — Employer Pay Reference ─────────────────────────────────────

    private static String paye(ThreadLocalRandom rng) {
        return String.format("%03d/%c%06d", rng.nextInt(100, 999),
            (char)('A' + rng.nextInt(26)), rng.nextInt(100000, 999999));
    }

    // ── German USt-IdNr ───────────────────────────────────────────────────────

    private static String ustId(ThreadLocalRandom rng) {
        return String.format("DE%09d", rng.nextInt(100000000, 999999999));
    }

    // ── German HRB ───────────────────────────────────────────────────────────

    private static String hrb(ThreadLocalRandom rng) {
        return String.format("HRB %d", rng.nextInt(10000, 999999));
    }

    // ── German RVN ────────────────────────────────────────────────────────────

    private static String rvn(ThreadLocalRandom rng) {
        // Format: 2 area digits + 6 birthdate digits (DDMMYY) + 1 letter + 3 serial digits = 12 chars
        // Check digit appended as 13th character.
        // Each char → numeric value (digit=digit, letter=position 1-26 as two digits).
        // Weight: i%2==0 → ×2, i%2==1 → ×1; take digit-sum of each product.
        // checksum = total % 10
        String area   = String.format("%02d", rng.nextInt(10, 99));
        String birth  = String.format("%02d%02d%02d",
            rng.nextInt(1, 32), rng.nextInt(1, 13), rng.nextInt(0, 100));
        char   letter = (char)('A' + rng.nextInt(26));
        String serial = String.format("%03d", rng.nextInt(0, 1000));
        String body   = area + birth + letter + serial;   // 12 chars

        // Expand to digit string: letter → two-digit position (A=01 .. Z=26)
        StringBuilder expanded = new StringBuilder();
        for (char c : body.toCharArray()) {
            if (Character.isLetter(c)) {
                int pos = c - 'A' + 1;
                expanded.append(String.format("%02d", pos));
            } else {
                expanded.append(c);
            }
        }
        String digits = expanded.toString();

        int total = 0;
        for (int i = 0; i < digits.length(); i++) {
            int d = digits.charAt(i) - '0';
            int product = (i % 2 == 0) ? d * 2 : d * 1;
            // digit sum of product
            total += product / 10 + product % 10;
        }
        int check = total % 10;
        return body + check;
    }

    // ── French SIREN (9 digits, Luhn) ────────────────────────────────────────

    private static String siren(ThreadLocalRandom rng) {
        StringBuilder sb = new StringBuilder(8);
        sb.append(rng.nextInt(1, 10));
        for (int i = 1; i < 8; i++) sb.append(rng.nextInt(0, 10));
        String partial = sb.toString();
        return partial + luhnCheckDigit(partial);
    }

    // ── French SIRET (SIREN + 5 digits) ──────────────────────────────────────

    private static String siret(ThreadLocalRandom rng) {
        String sir = siren(rng);
        StringBuilder sb = new StringBuilder(sir);
        for (int i = 0; i < 4; i++) sb.append(rng.nextInt(0, 10));
        String partial = sb.toString();
        return partial + luhnCheckDigit(partial);
    }

    // ── French TVA ───────────────────────────────────────────────────────────

    private static String tva(ThreadLocalRandom rng) {
        String siren = siren(rng);
        int key = (12 + 3 * (Integer.parseInt(siren) % 97)) % 97;
        return String.format("FR%02d%s", key, siren);
    }

    // ── Russian OGRN (13 digits) ──────────────────────────────────────────────

    private static String ogrn(ThreadLocalRandom rng) {
        StringBuilder sb = new StringBuilder(12);
        sb.append(rng.nextInt(1, 4)); // first digit 1-3
        for (int i = 1; i < 12; i++) sb.append(rng.nextInt(0, 10));
        String body = sb.toString();
        long val = Long.parseLong(body);
        int check = (int)((val % 11) % 10);
        return body + check;
    }

    // ── Russian KPP (9 chars) ─────────────────────────────────────────────────

    private static String kpp(ThreadLocalRandom rng) {
        return String.format("%04d%02d%03d",
            rng.nextInt(1000, 9999),
            rng.nextInt(1, 99),
            rng.nextInt(100, 999));
    }

    // ── Employer ID & Insurance ID ────────────────────────────────────────────

    private static String employerId(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "US"       -> ein(rng);
            case "TR"       -> sgk(rng);
            case "DE"       -> ustId(rng);
            case "FR"       -> siren(rng);
            case "UK"       -> paye(rng);
            case "RU"       -> inn(rng);
            default         -> ein(rng);
        };
    }

    private static String insuranceId(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR"       -> sgk(rng);
            case "US"       -> ssn(rng);
            case "UK"       -> nin(rng);
            case "RU"       -> snils(rng);
            case "FR"       -> inseeNumber(rng);
            default         -> snils(rng);
        };
    }

    private static String inseeNumber(ThreadLocalRandom rng) {
        int gender  = rng.nextInt(1, 3);
        int yy      = rng.nextInt(0, 100);
        int month   = rng.nextInt(1, 13);
        int dep     = rng.nextInt(1, 96);
        int commune = rng.nextInt(1, 999);
        int order   = rng.nextInt(1, 999);
        int key     = rng.nextInt(1, 98);
        return String.format("%d%02d%02d%02d%03d%03d%02d", gender, yy, month, dep, commune, order, key);
    }

    // ── Names ────────────────────────────────────────────────────────────────

    static String firstname(ThreadLocalRandom rng, String locale, String gender) {
        boolean male = !"F".equalsIgnoreCase(gender) && (gender.isEmpty() ? rng.nextBoolean() : true);
        return switch (locale) {
            case "TR" -> male ? pick(rng, TR_FIRST_M) : pick(rng, TR_FIRST_F);
            case "DE" -> male ? pick(rng, DE_FIRST_M) : pick(rng, DE_FIRST_F);
            case "FR" -> male ? pick(rng, FR_FIRST_M) : pick(rng, FR_FIRST_F);
            case "UK" -> male ? pick(rng, UK_FIRST_M) : pick(rng, UK_FIRST_F);
            case "RU" -> male ? pick(rng, RU_FIRST_M) : pick(rng, RU_FIRST_F);
            default   -> male ? pick(rng, US_FIRST_M) : pick(rng, US_FIRST_F);
        };
    }

    static String lastname(ThreadLocalRandom rng, String locale, String gender) {
        return switch (locale) {
            case "TR" -> pick(rng, TR_LAST);
            case "DE" -> pick(rng, DE_LAST);
            case "FR" -> pick(rng, FR_LAST);
            case "UK" -> pick(rng, UK_LAST);
            case "RU" -> {
                String base = pick(rng, RU_LAST);
                yield "F".equalsIgnoreCase(gender) ? base + "а" : base;
            }
            default   -> pick(rng, US_LAST);
        };
    }

    static String fullname(ThreadLocalRandom rng, String locale) {
        boolean male = rng.nextBoolean();
        String gender = male ? "M" : "F";
        return firstname(rng, locale, gender) + " " + lastname(rng, locale, gender);
    }

    static String patronymic(ThreadLocalRandom rng, String locale, String gender) {
        if (!"RU".equals(locale)) return "-";
        boolean male = !"F".equalsIgnoreCase(gender) && (gender.isEmpty() ? rng.nextBoolean() : true);
        return male ? pick(rng, RU_PATRONYMIC_M) : pick(rng, RU_PATRONYMIC_F);
    }

    // ── Passport ──────────────────────────────────────────────────────────────

    private static String passport(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> String.format("%c%07d", (char)('A'+rng.nextInt(26)), rng.nextInt(1000000,9999999));
            case "US" -> String.format("%09d", rng.nextInt(100000000, 999999999));
            case "DE" -> {
                String alpha = randomAlpha(rng, 2);
                yield alpha + String.format("%07d", rng.nextInt(1000000, 9999999));
            }
            case "RU" -> String.format("%02d%02d%06d", rng.nextInt(10,99), rng.nextInt(10,99), rng.nextInt(100000,999999));
            default   -> String.format("%c%08d", (char)('A'+rng.nextInt(26)), rng.nextInt(10000000,99999999));
        };
    }

    // ── Driver's license ──────────────────────────────────────────────────────

    private static String license(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> randomAlpha(rng,2) + String.format("%05d", rng.nextInt(10000,99999));
            case "US" -> String.format("%c%08d", (char)('A'+rng.nextInt(26)), rng.nextInt(10000000,99999999));
            case "UK" -> randomAlpha(rng,5) + String.format("%06d", rng.nextInt(100000,999999)) + randomAlpha(rng,2);
            case "DE" -> String.format("%c%07d", (char)('A'+rng.nextInt(26)), rng.nextInt(1000000,9999999));
            default   -> String.format("%c%08d", (char)('A'+rng.nextInt(26)), rng.nextInt(10000000,99999999));
        };
    }

    // ── Birthdate ─────────────────────────────────────────────────────────────

    private static String birthdate(ThreadLocalRandom rng) {
        int year  = LocalDate.now().getYear() - rng.nextInt(18, 80);
        int month = rng.nextInt(1, 13);
        int day   = rng.nextInt(1, 29);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    // ── NationalID — routes by locale ────────────────────────────────────────

    private static String nationalid(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> tckn(rng);
            case "US" -> ssn(rng);
            case "UK" -> nin(rng);
            case "RU" -> snils(rng);
            case "DE" -> ustId(rng);
            case "FR" -> inseeNumber(rng);
            default   -> tckn(rng);
        };
    }

    // ── VAT number ───────────────────────────────────────────────────────────

    private static String vatNumber(ThreadLocalRandom rng, String locale) {
        return switch (locale) {
            case "TR" -> vkn(rng);
            case "DE" -> ustId(rng);
            case "FR" -> tva(rng);
            case "UK" -> String.format("GB%09d", rng.nextInt(100000000, 999999999));
            case "US" -> ein(rng);
            case "RU" -> inn(rng);
            default   -> vkn(rng);
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }

    private static String randomAlpha(ThreadLocalRandom rng, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append((char)('A' + rng.nextInt(26)));
        return sb.toString();
    }

    static int luhnCheckDigit(String partial) {
        int sum = 0;
        boolean alt = true;
        for (int i = partial.length() - 1; i >= 0; i--) {
            int d = partial.charAt(i) - '0';
            if (alt) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
            alt = !alt;
        }
        return (10 - (sum % 10)) % 10;
    }
}
