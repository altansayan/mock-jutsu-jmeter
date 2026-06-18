package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** International ID generator — mirrors intl_ids.py algorithms. */
public final class IntlIdsGen {

    private IntlIdsGen() {}

    private static ThreadLocalRandom rng() { return ThreadLocalRandom.current(); }

    // ── Verhoeff tables (Aadhaar) ─────────────────────────────────────────────
    private static final int[][] V_D = {
        {0,1,2,3,4,5,6,7,8,9},{1,2,3,4,0,6,7,8,9,5},{2,3,4,0,1,7,8,9,5,6},
        {3,4,0,1,2,8,9,5,6,7},{4,0,1,2,3,9,5,6,7,8},{5,9,8,7,6,0,4,3,2,1},
        {6,5,9,8,7,1,0,4,3,2},{7,6,5,9,8,2,1,0,4,3},{8,7,6,5,9,3,2,1,0,4},
        {9,8,7,6,5,4,3,2,1,0}
    };
    private static final int[][] V_P = {
        {0,1,2,3,4,5,6,7,8,9},{1,5,7,6,2,8,3,0,9,4},{5,8,0,3,7,9,6,1,4,2},
        {8,9,1,6,0,4,3,5,2,7},{9,4,5,3,1,2,6,8,7,0},{4,2,8,6,5,7,3,9,0,1},
        {2,7,9,3,8,0,6,4,1,5},{7,0,4,6,9,1,3,2,5,8}
    };
    private static final int[] V_INV = {0,4,3,2,1,5,6,7,8,9};

    private static int verhoeffCheck(int[] baseDigits) {
        int[] full = new int[baseDigits.length + 1];
        System.arraycopy(baseDigits, 0, full, 0, baseDigits.length);
        for (int check = 0; check <= 9; check++) {
            full[baseDigits.length] = check;
            int c = 0;
            for (int i = full.length - 1; i >= 0; i--) {
                c = V_D[c][V_P[(full.length - 1 - i) % 8][full[i]]];
            }
            if (c == 0) return check;
        }
        return 0;
    }

    // ── Luhn ──────────────────────────────────────────────────────────────────
    private static int luhnCheck(String partial) {
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

    // ── ISO 7064 MOD 11,10 ────────────────────────────────────────────────────
    private static int mod1110Check(String partial) {
        int p = 10;
        for (char c : partial.toCharArray()) {
            int s = (p + (c - '0')) % 10;
            if (s == 0) s = 10;
            p = (2 * s) % 11;
        }
        return (11 - p) % 10;
    }

    // ── ISO 7064 MOD 11,2 ─────────────────────────────────────────────────────
    private static char mod112Check(String partial) {
        int s = 0, w = 2;
        for (int i = partial.length() - 1; i >= 0; i--) {
            s = (s + (partial.charAt(i) - '0') * w) % 11;
            w = w == 6 ? 2 : w + 1;
        }
        int r = (12 - s) % 11;
        return r == 10 ? 'X' : (char)('0' + r);
    }

    // ── Luhn mod-36 (GSTIN) ───────────────────────────────────────────────────
    private static final String GSTIN_ALPHA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static char luhn36Check(String partial) {
        int n = GSTIN_ALPHA.length();
        for (int ci = 0; ci < n; ci++) {
            char cand = GSTIN_ALPHA.charAt(ci);
            String full = partial + cand;
            int sum = 0;
            boolean dbl = false;
            for (int i = full.length() - 1; i >= 0; i--) {
                int v = GSTIN_ALPHA.indexOf(full.charAt(i));
                if (dbl) { v *= 2; if (v >= n) v = v / n + v % n; }
                sum += v;
                dbl = !dbl;
            }
            if (sum % n == 0) return cand;
        }
        return '0';
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static String randDigits(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(rng().nextInt(10));
        return sb.toString();
    }
    private static String randDigitsRange(int lo, int hi) {
        return String.valueOf(rng().nextInt(lo, hi + 1));
    }
    private static char randAlpha() { return (char)('A' + rng().nextInt(26)); }
    private static String randAlphas(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(randAlpha());
        return sb.toString();
    }
    private static char randAlphaNum() {
        int r = rng().nextInt(36);
        return r < 26 ? (char)('A' + r) : (char)('0' + r - 26);
    }
    private static String padLeft(int v, int width) {
        return String.format("%0" + width + "d", v);
    }

    // ── DNI/NIE letter table ──────────────────────────────────────────────────
    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    // ── BR CPF ────────────────────────────────────────────────────────────────
    private static String genBrCpf() {
        int[] d = new int[9];
        for (int i = 0; i < 9; i++) d[i] = rng().nextInt(10);
        int s1 = 0; for (int i = 0; i < 9; i++) s1 += d[i] * (10 - i);
        int c1 = 11 - s1 % 11; if (c1 >= 10) c1 = 0;
        int s2 = 0; for (int i = 0; i < 9; i++) s2 += d[i] * (11 - i); s2 += c1 * 2;
        int c2 = 11 - s2 % 11; if (c2 >= 10) c2 = 0;
        StringBuilder b = new StringBuilder();
        for (int x : d) b.append(x);
        return b.append(c1).append(c2).toString();
    }

    // ── BR CNPJ ───────────────────────────────────────────────────────────────
    private static String genBrCnpj() {
        int[] d = new int[12];
        for (int i = 0; i < 8; i++) d[i] = rng().nextInt(10);
        d[8] = 0; d[9] = 0; d[10] = 0; d[11] = 1;
        int[] w1 = {5,4,3,2,9,8,7,6,5,4,3,2};
        int s1 = 0; for (int i = 0; i < 12; i++) s1 += d[i] * w1[i];
        int c1 = 11 - s1 % 11; if (c1 >= 10) c1 = 0;
        int[] w2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
        int s2 = 0; for (int i = 0; i < 12; i++) s2 += d[i] * w2[i]; s2 += c1 * w2[12];
        int c2 = 11 - s2 % 11; if (c2 >= 10) c2 = 0;
        StringBuilder b = new StringBuilder();
        for (int x : d) b.append(x);
        return b.append(c1).append(c2).toString();
    }

    // ── IN PAN ────────────────────────────────────────────────────────────────
    private static String genInPan() {
        return "" + randAlphas(3) + "P" + randAlpha()
             + (char)('0' + rng().nextInt(1, 10))
             + randDigits(3) + randAlpha();
    }

    // ── IN Aadhaar ────────────────────────────────────────────────────────────
    private static String genInAadhaar() {
        while (true) {
            int first = rng().nextInt(2, 10);
            int[] base = new int[11];
            base[0] = first;
            for (int i = 1; i < 11; i++) base[i] = rng().nextInt(10);
            // palindrome check
            int[] all = new int[12];
            System.arraycopy(base, 0, all, 0, 11);
            all[11] = verhoeffCheck(base);
            boolean pal = true;
            for (int i = 0; i < 6; i++) { if (all[i] != all[11-i]) { pal = false; break; } }
            if (!pal) {
                StringBuilder b = new StringBuilder();
                for (int x : all) b.append(x);
                return b.toString();
            }
        }
    }

    // ── IN GSTIN ──────────────────────────────────────────────────────────────
    private static final String[] GSTIN_STATES = {
        "01","02","03","04","05","06","07","08","09","10",
        "11","12","13","14","15","16","17","18","19","20",
        "21","22","23","24","25","26","27","28","29","30",
        "31","32","33","34","35","36","37"
    };
    private static String genInGstin() {
        String state = GSTIN_STATES[rng().nextInt(GSTIN_STATES.length)];
        String pan = genInPan();
        String entity = String.valueOf(rng().nextInt(1, 10));
        String z = "Z";
        String partial = state + pan + entity + z;
        char check = luhn36Check(partial);
        return partial + check;
    }

    // ── IN EPIC ───────────────────────────────────────────────────────────────
    private static String genInEpic() {
        return randAlphas(3) + randDigits(7);
    }

    // ── CN RIC ────────────────────────────────────────────────────────────────
    private static final String[] CN_AREAS = {
        "110000","120000","130000","140000","150000","210000","220000","230000",
        "310000","320000","330000","340000","350000","360000","370000","410000",
        "420000","430000","440000","450000","460000","510000","520000","530000",
        "540000","610000","620000","630000","640000","650000"
    };
    private static String genCnRic() {
        String area = CN_AREAS[rng().nextInt(CN_AREAS.length)];
        int year = rng().nextInt(1960, 2006);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        String seq = padLeft(rng().nextInt(1, 1000), 3);
        String partial = area + String.format("%04d%02d%02d", year, month, day) + seq;
        return partial + mod112Check(partial);
    }

    // ── MX CURP ───────────────────────────────────────────────────────────────
    private static final String[] MX_STATES = {
        "AS","BC","BS","CC","CL","CM","CS","CH","DF","DG",
        "GT","GR","HG","JC","MC","MN","MS","NT","NL","OC",
        "PL","QT","QR","SP","SL","SR","TC","TL","TS","VZ",
        "YN","ZS","NE"
    };
    private static final String CURP_ALPH = "0123456789ABCDEFGHIJKLMN&OPQRSTUVWXYZ";
    private static final String[] CURP_CONSONANTS_POOL = {"B","C","D","F","G","H","J","K","L","M","N","P","Q","R","S","T","V","W","X","Y","Z"};
    private static final String[] CURP_VOWELS = {"A","E","I","O","U"};
    private static final String[] CURP_BLOCKED = {"BACA","BAKA","BUEI","BUEY","CACA","CACO","CAGA","CAGO","CAKA","CAKO","COGE","COGI","COJA","COJE","COJI","COJO","COLA","CULO","FALO","FETO","GETA","GUEI","GUEY","JETA","JOTO","KACA","KACO","KAGA","KAGO","KAKA","KAKO","KOGE","KOGI","KOJA","KOJE","KOJI","KOJO","KOLA","KULO","LELO","LOCA","LOCO","LOKA","LOKO","MAME","MAMO","MEAR","MEAS","MEON","MIAR","MION","MOCO","MOKO","MULA","MULO","NACA","NACO","PEDA","PEDO","PENE","PIPI","PITO","POPO","PUTA","PUTO","QULO","RATA","ROBA","ROBE","ROBO","RUIN","SENO","TETA","VACA","VAGA","VAGO","VAKA","VUEI","VUEY","WUEI","WUEY"};
    private static String genMxCurp() {
        while (true) {
            String l1 = String.valueOf(randAlpha());
            String v1 = CURP_VOWELS[rng().nextInt(CURP_VOWELS.length)];
            String l2 = String.valueOf(randAlpha());
            String l3 = String.valueOf(randAlpha());
            String block = l1 + v1 + l2 + l3;
            boolean blocked = false;
            for (String b : CURP_BLOCKED) if (b.equals(block)) { blocked = true; break; }
            if (blocked) continue;
            int year = rng().nextInt(1950, 2006);
            int month = rng().nextInt(1, 13);
            int day = rng().nextInt(1, 29);
            String gender = rng().nextBoolean() ? "H" : "M";
            String state = MX_STATES[rng().nextInt(MX_STATES.length)];
            String c1 = CURP_CONSONANTS_POOL[rng().nextInt(CURP_CONSONANTS_POOL.length)];
            String c2 = CURP_CONSONANTS_POOL[rng().nextInt(CURP_CONSONANTS_POOL.length)];
            String c3 = CURP_CONSONANTS_POOL[rng().nextInt(CURP_CONSONANTS_POOL.length)];
            String digit = String.valueOf(rng().nextInt(10));
            String partial = block + String.format("%02d%02d%02d", year % 100, month, day)
                           + gender + state + c1 + c2 + c3 + digit;
            // compute check digit
            int s = 0;
            for (int i = 0; i < 17; i++) {
                s += CURP_ALPH.indexOf(partial.charAt(i)) * (18 - i);
            }
            int check = (10 - s % 10) % 10;
            return partial + check;
        }
    }

    // ── MX RFC ────────────────────────────────────────────────────────────────
    private static String genMxRfc() {
        int year = rng().nextInt(50, 100);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        return randAlphas(4) + String.format("%02d%02d%02d", year, month, day)
             + randAlphaNum() + randAlphaNum() + randAlphaNum();
    }

    // ── IT Codice Fiscale ─────────────────────────────────────────────────────
    private static final int[] CF_ODD = {
        1,0,5,7,9,13,15,17,19,21,2,4,18,20,11,3,6,8,12,14,16,10,22,25,24,23
    };
    private static final int[] CF_EVEN_DIGIT = {0,1,2,3,4,5,6,7,8,9};
    private static final int[] CF_EVEN_ALPHA = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25};
    private static String genItCf() {
        String surname = randAlphas(3);
        String name = randAlphas(3);
        int year = rng().nextInt(50, 100);
        String[] months = {"A","B","C","D","E","H","L","M","P","R","S","T"};
        String month = months[rng().nextInt(12)];
        int dayBase = rng().nextInt(1, 29);
        boolean female = rng().nextBoolean();
        int day = female ? dayBase + 40 : dayBase;
        String mun = randAlphas(1) + randDigits(3);
        String partial = (surname + name + String.format("%02d", year) + month
                        + String.format("%02d", day) + mun).toUpperCase();
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            char c = partial.charAt(i);
            int v;
            if ((i + 1) % 2 == 0) {
                // even position (1-based)
                v = Character.isDigit(c) ? CF_EVEN_DIGIT[c - '0'] : CF_EVEN_ALPHA[c - 'A'];
            } else {
                // odd position
                v = Character.isDigit(c) ? CF_ODD[c - '0'] : CF_ODD[c - 'A'];
            }
            sum += v;
        }
        char check = (char)('A' + sum % 26);
        return partial + check;
    }

    // ── ES DNI ────────────────────────────────────────────────────────────────
    private static String genEsDni() {
        int num = rng().nextInt(10000000, 100000000);
        return String.format("%08d", num) + DNI_LETTERS.charAt(num % 23);
    }

    // ── ES NIE ────────────────────────────────────────────────────────────────
    private static String genEsNie() {
        char prefix = "XYZ".charAt(rng().nextInt(3));
        int prefixVal = "XYZ".indexOf(prefix);
        int num = rng().nextInt(1000000, 10000000);
        int forMod = Integer.parseInt(prefixVal + String.format("%07d", num));
        return prefix + String.format("%07d", num) + DNI_LETTERS.charAt(forMod % 23);
    }

    // ── ES CCC ────────────────────────────────────────────────────────────────
    private static String genEsCcc() {
        String bank = randDigits(4);
        String branch = randDigits(4);
        String account = randDigits(10);
        int[] w = {4,8,5,10,9,7,3,6,1,2};
        int s1 = 0;
        String p1 = "00" + bank + branch;
        for (int i = 2; i < 10; i++) s1 += (p1.charAt(i) - '0') * w[i - 2];
        int c1 = 11 - s1 % 11; if (c1 > 9) c1 -= 10;
        int s2 = 0;
        for (int i = 0; i < 10; i++) s2 += (account.charAt(i) - '0') * w[i];
        int c2 = 11 - s2 % 11; if (c2 > 9) c2 -= 10;
        return bank + branch + c1 + c2 + account;
    }

    // ── DE IdNr ───────────────────────────────────────────────────────────────
    private static String genDeIdnr() {
        while (true) {
            int first = rng().nextInt(1, 10);
            StringBuilder b = new StringBuilder();
            b.append(first);
            for (int i = 0; i < 9; i++) b.append(rng().nextInt(10));
            String partial = b.toString();
            // must have at least one repeated digit among first 10
            boolean hasRepeat = false;
            for (int d = 0; d <= 9; d++) {
                int cnt = 0;
                for (char c : partial.toCharArray()) if (c - '0' == d) cnt++;
                if (cnt >= 2) { hasRepeat = true; break; }
            }
            if (!hasRepeat) continue;
            int check = mod1110Check(partial);
            return partial + check;
        }
    }

    // ── DE StNr ───────────────────────────────────────────────────────────────
    private static final String[] DE_STATE_PREFIXES = {
        "11","21","22","23","24","25","26","27","28","30",
        "31","32","33","40","41","42","43","51","52","61",
        "62","63","71","81","91","92","93","94","95","96","97","98"
    };
    private static String genDeStnr() {
        String prefix = DE_STATE_PREFIXES[rng().nextInt(DE_STATE_PREFIXES.length)];
        return prefix + "/" + randDigits(3) + "/" + randDigits(5);
    }

    // ── PK CNIC ───────────────────────────────────────────────────────────────
    private static String genPkCnic() {
        return randDigits(5) + "-" + randDigits(7) + "-" + rng().nextInt(1, 10);
    }

    // ── JP CN ─────────────────────────────────────────────────────────────────
    private static String genJpCn() {
        int[] weights = {1,2,1,2,1,2,1,2,1,2,1,2};
        int[] body = new int[12];
        for (int i = 0; i < 12; i++) body[i] = rng().nextInt(10);
        int s = 0;
        for (int i = 0; i < 12; i++) s = (s + weights[i] * body[11 - i]) % 9;
        int check = 9 - s;
        StringBuilder b = new StringBuilder();
        b.append(check);
        for (int x : body) b.append(x);
        return b.toString();
    }

    // ── JP IN ─────────────────────────────────────────────────────────────────
    private static String genJpIn() {
        int[] w = {6,5,4,3,2,7,6,5,4,3,2};
        StringBuilder partial = new StringBuilder();
        for (int i = 0; i < 11; i++) partial.append(rng().nextInt(10));
        int s = 0;
        for (int i = 0; i < 11; i++) s += (partial.charAt(i) - '0') * w[i];
        int r = s % 11;
        int check = r < 2 ? 0 : 11 - r;
        return partial + String.valueOf(check);
    }

    // ── KR RRN ────────────────────────────────────────────────────────────────
    private static String genKrRrn() {
        int[] weights = {2,3,4,5,6,7,8,9,2,3,4,5};
        int year = rng().nextInt(1950, 2006);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        int yy = year % 100;
        int gender = year < 2000 ? rng().nextInt(1, 3) : rng().nextInt(3, 5);
        int pb = rng().nextInt(0, 97);
        int xx = rng().nextInt(0, 100);
        int cc = rng().nextInt(0, 10);
        String base = String.format("%02d%02d%02d%d%02d%02d%d", yy, month, day, gender, pb, xx, cc);
        int s = 0;
        for (int i = 0; i < 12; i++) s += (base.charAt(i) - '0') * weights[i];
        int check = (11 - s % 11) % 10;
        return String.format("%02d%02d%02d-%d%02d%02d%d%d", yy, month, day, gender, pb, xx, cc, check);
    }

    // ── KR BRN ────────────────────────────────────────────────────────────────
    private static String genKrBrn() {
        String part1 = randDigits(3);
        String part2 = String.valueOf(rng().nextInt(1, 10));
        String part3 = randDigits(5);
        return part1 + "-" + part2 + "-" + part3;
    }

    // ── NL BSN ────────────────────────────────────────────────────────────────
    private static String genNlBsn() {
        while (true) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < 9; i++) b.append(rng().nextInt(10));
            String s = b.toString();
            int sum = 0;
            for (int i = 0; i < 8; i++) sum += (s.charAt(i) - '0') * (9 - i);
            sum -= (s.charAt(8) - '0');
            if (sum % 11 == 0 && !s.equals("000000000")) return s;
        }
    }

    // ── PL PESEL ─────────────────────────────────────────────────────────────
    private static String genPlPesel() {
        int year = rng().nextInt(1950, 2001);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        int encMonth = month + (year >= 2000 ? 20 : 0);
        String partial = String.format("%02d%02d%02d", year % 100, encMonth, day) + randDigits(4);
        int[] w = {1,3,7,9,1,3,7,9,1,3};
        int s = 0;
        for (int i = 0; i < 10; i++) s += (partial.charAt(i) - '0') * w[i];
        int check = (10 - s % 10) % 10;
        return partial + check;
    }

    // ── SE Personnummer ───────────────────────────────────────────────────────
    private static String genSePersonnummer() {
        int year = rng().nextInt(1950, 2001);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        String partial = String.format("%02d%02d%02d", year % 100, month, day) + randDigits(3);
        int check = luhnCheck(partial);
        return String.format("%02d%02d%02d-%s%d", year % 100, month, day, partial.substring(6), check);
    }

    // ── DK CPR ────────────────────────────────────────────────────────────────
    private static String genDkCpr() {
        int year = rng().nextInt(1950, 2001);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        int seq = rng().nextInt(0, 4000);
        return String.format("%02d%02d%02d-%04d", day, month, year % 100, seq);
    }

    // ── FI Hetu ───────────────────────────────────────────────────────────────
    private static final String HETU_CHARS = "0123456789ABCDEFHJKLMNPRSTUVWXY";
    private static String genFiHetu() {
        int year = rng().nextInt(1950, 2001);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        int seq = rng().nextInt(2, 900);
        String partial = String.format("%02d%02d%02d", day, month, year % 100) + String.format("%03d", seq);
        int r = Integer.parseInt(partial) % 31;
        return String.format("%02d%02d%02d-%03d%c", day, month, year % 100, seq, HETU_CHARS.charAt(r));
    }

    // ── NO Fødselsnummer ─────────────────────────────────────────────────────
    private static String genNoFodselsnummer() {
        while (true) {
            int year = rng().nextInt(1950, 2001);
            int month = rng().nextInt(1, 13);
            int day = rng().nextInt(1, 29);
            int ind = rng().nextInt(0, 500);
            String partial = String.format("%02d%02d%02d%03d", day, month, year % 100, ind);
            int[] w1 = {3,7,6,1,8,9,4,5,2};
            int s1 = 0;
            for (int i = 0; i < 9; i++) s1 += (partial.charAt(i) - '0') * w1[i];
            int c1r = s1 % 11;
            if (c1r == 1) continue;
            int c1 = c1r == 0 ? 0 : 11 - c1r;
            int[] w2 = {5,4,3,2,7,6,5,4,3,2};
            int s2 = 0;
            for (int i = 0; i < 9; i++) s2 += (partial.charAt(i) - '0') * w2[i];
            s2 += c1 * w2[9];
            int c2r = s2 % 11;
            if (c2r == 1) continue;
            int c2 = c2r == 0 ? 0 : 11 - c2r;
            return partial + c1 + c2;
        }
    }

    // ── AU ABN ────────────────────────────────────────────────────────────────
    private static String genAuAbn() {
        int[] w = {3,5,7,9,11,13,15,17,19};
        String body = randDigits(9);
        int s = 0;
        for (int i = 0; i < 9; i++) s += -w[i] * (body.charAt(i) - '0');
        // compute first 2 check digits: 11 + (s-1) % 89
        int check2val = 11 + ((s - 1) % 89 + 89) % 89;
        return String.format("%02d", check2val) + body;
    }

    // ── AU TFN ────────────────────────────────────────────────────────────────
    private static String genAuTfn() {
        while (true) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < 8; i++) b.append(rng().nextInt(10));
            String s = b.toString();
            int[] w = {1,4,3,7,5,8,6,9,10};
            int sum = 0;
            for (int i = 0; i < 8; i++) sum += (s.charAt(i) - '0') * w[i];
            int check = sum % 11;
            if (check == 10) continue;
            return s + check;
        }
    }

    // ── AU ACN ────────────────────────────────────────────────────────────────
    private static String genAuAcn() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 8; i++) b.append(rng().nextInt(10));
        String partial = b.toString();
        int[] w = {8,7,6,5,4,3,2,1};
        int s = 0;
        for (int i = 0; i < 8; i++) s += (partial.charAt(i) - '0') * w[i];
        int check = (10 - s % 10) % 10;
        return partial + check;
    }

    // ── MY NRIC ───────────────────────────────────────────────────────────────
    private static final String[] MY_STATES = {
        "01","02","03","04","05","06","07","08","09","10",
        "11","12","13","14","15","16"
    };
    private static String genMyNric() {
        int year = rng().nextInt(50, 100);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        String state = MY_STATES[rng().nextInt(MY_STATES.length)];
        int seq = rng().nextInt(0, 10000);
        return String.format("%02d%02d%02d%s%04d", year, month, day, state, seq);
    }

    // ── TH PIN ────────────────────────────────────────────────────────────────
    private static String genThPin() {
        StringBuilder b = new StringBuilder();
        b.append(rng().nextInt(1, 10));
        for (int i = 0; i < 11; i++) b.append(rng().nextInt(10));
        String partial = b.toString();
        int s = 0;
        for (int i = 0; i < 12; i++) s += (partial.charAt(i) - '0') * (13 - i);
        int check = (11 - s % 11) % 10;
        return partial + check;
    }

    // ── TH TIN ────────────────────────────────────────────────────────────────
    private static String genThTin() {
        return String.valueOf(rng().nextInt(1, 10)) + randDigits(9);
    }

    // ── SG UEN ────────────────────────────────────────────────────────────────
    private static String genSgUen() {
        int year = rng().nextInt(1950, 2026);
        return String.format("%d%05d%c", year, rng().nextInt(1, 100000), randAlpha());
    }

    // ── ZA IDNr ───────────────────────────────────────────────────────────────
    private static String genZaIdnr() {
        int year = rng().nextInt(50, 100);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        int gender = rng().nextInt(0, 10000);
        int citizen = rng().nextInt(0, 2);
        String partial = String.format("%02d%02d%02d%04d0%d8", year, month, day, gender, citizen);
        int check = luhnCheck(partial);
        return partial + check;
    }

    // ── CA BN ─────────────────────────────────────────────────────────────────
    private static String genCaBn() {
        String partial = randDigits(8);
        return partial + luhnCheck(partial);
    }

    // ── NZ IRD ────────────────────────────────────────────────────────────────
    private static String genNzIrd() {
        while (true) {
            int base = rng().nextInt(1000001, 15000000);
            String s = String.format("%08d", base);
            int[] w1 = {3,2,7,6,5,4,3,2};
            int sum1 = 0;
            for (int i = 0; i < 8; i++) sum1 += (s.charAt(i) - '0') * w1[i];
            int r1 = sum1 % 11;
            int check;
            if (r1 == 0) {
                check = 0;
            } else {
                check = 11 - r1;
                if (check == 10) {
                    int[] w2 = {7,4,3,2,5,2,7,6};
                    int sum2 = 0;
                    for (int i = 0; i < 8; i++) sum2 += (s.charAt(i) - '0') * w2[i];
                    int r2 = sum2 % 11;
                    check = r2 == 0 ? 0 : 11 - r2;
                    if (check == 10) continue;
                }
            }
            return s + check;
        }
    }

    // ── AR CUIT ───────────────────────────────────────────────────────────────
    private static String genArCuit() {
        int[] prefixes = {20,23,24,27,30,33,34};
        int prefix = prefixes[rng().nextInt(prefixes.length)];
        String body = String.format("%08d", rng().nextInt(1000000, 100000000));
        String partial = String.format("%02d%s", prefix, body);
        int[] w = {5,4,3,2,7,6,5,4,3,2};
        int s = 0;
        for (int i = 0; i < 10; i++) s += (partial.charAt(i) - '0') * w[i];
        int r = s % 11;
        int check = r < 2 ? 0 : 11 - r;
        return partial.substring(0,2) + "-" + body + "-" + check;
    }

    // ── AR DNI ────────────────────────────────────────────────────────────────
    private static String genArDni() {
        return String.valueOf(rng().nextInt(1000000, 100000000));
    }

    // ── CL RUT ────────────────────────────────────────────────────────────────
    private static String genClRut() {
        int num = rng().nextInt(1000000, 30000000);
        int[] digits = String.valueOf(num).chars().map(c -> c - '0').toArray();
        int[] w = {2,3,4,5,6,7};
        int s = 0;
        for (int i = 0; i < digits.length; i++) s += digits[digits.length - 1 - i] * w[i % 6];
        int r = 11 - s % 11;
        String check = r == 11 ? "0" : r == 10 ? "K" : String.valueOf(r);
        return String.format("%,d", num).replace(",", ".") + "-" + check;
    }

    // ── CO NIT ────────────────────────────────────────────────────────────────
    private static final String CO_NIT_TABLE = "01987654321";
    private static String genCoNit() {
        String body = randDigits(9);
        int[] w = {3,7,13,17,19,23,29,37,41,43,47};
        int s = 0;
        for (int i = 0; i < 9; i++) s += (body.charAt(i) - '0') * w[i];
        int r = s % 11;
        char check = CO_NIT_TABLE.charAt(r < CO_NIT_TABLE.length() ? r : 0);
        return body + "-" + check;
    }

    // ── IL IDNr ───────────────────────────────────────────────────────────────
    private static String genIlIdnr() {
        String partial = randDigits(8);
        int check = luhnCheck(partial);
        return partial + check;
    }

    // ── RO CNP ────────────────────────────────────────────────────────────────
    private static String genRoCnp() {
        int s = rng().nextInt(1, 9);
        int year = rng().nextInt(1950, 2006);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        int county = rng().nextInt(1, 47);
        int seq = rng().nextInt(1, 1000);
        String partial = String.format("%d%02d%02d%02d%02d%03d", s, year % 100, month, day, county, seq);
        int[] w = {2,7,9,1,4,6,3,5,8,2,7,9};
        int sum = 0;
        for (int i = 0; i < 12; i++) sum += (partial.charAt(i) - '0') * w[i];
        int check = sum % 11;
        if (check == 10) check = 1;
        return partial + check;
    }

    // ── RO CUI ────────────────────────────────────────────────────────────────
    private static String genRoCui() {
        int body = rng().nextInt(1, 100000000);
        String s = String.valueOf(body);
        String padded = String.format("%010d", body);
        int[] w = {7,5,3,2,1,7,5,3,2};
        int sum = 0;
        for (int i = 1; i <= 9; i++) sum += (padded.charAt(10 - i) - '0') * w[9 - i];
        int check = sum * 10 % 11;
        if (check == 10) check = 0;
        return s + check;
    }

    // ── HR OIB ────────────────────────────────────────────────────────────────
    private static String genHrOib() {
        while (true) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < 10; i++) b.append(rng().nextInt(10));
            String partial = b.toString();
            int check = mod1110Check(partial);
            if (check != 0) continue;
            return partial;
        }
    }

    // ── BG EGN ────────────────────────────────────────────────────────────────
    private static String genBgEgn() {
        int year = rng().nextInt(1950, 2001);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        int encMonth = year >= 2000 ? month + 40 : month;
        String partial = String.format("%02d%02d%02d", year % 100, encMonth, day) + randDigits(3);
        int[] w = {2,4,8,5,10,9,7,3,6};
        int s = 0;
        for (int i = 0; i < 9; i++) s += (partial.charAt(i) - '0') * w[i];
        int check = s % 11 % 10;
        return partial + check;
    }

    // ── LT Asmens ─────────────────────────────────────────────────────────────
    private static String genLtAsmens() {
        int gender = rng().nextInt(1, 7);
        int year = rng().nextInt(1950, 2001);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        String partial = gender + String.format("%02d%02d%02d", year % 100, month, day) + randDigits(3);
        int[] w1 = {1,2,3,4,5,6,7,8,9,1};
        int s1 = 0;
        for (int i = 0; i < 10; i++) s1 += (partial.charAt(i) - '0') * w1[i];
        int c = s1 % 11;
        if (c == 10) {
            int[] w2 = {3,4,5,6,7,8,9,1,2,3};
            int s2 = 0;
            for (int i = 0; i < 10; i++) s2 += (partial.charAt(i) - '0') * w2[i];
            c = s2 % 11 % 10;
        }
        return partial + c;
    }

    // ── EE IK ─────────────────────────────────────────────────────────────────
    private static String genEeIk() {
        int gender = rng().nextInt(1, 7);
        int year = rng().nextInt(1950, 2001);
        int month = rng().nextInt(1, 13);
        int day = rng().nextInt(1, 29);
        String partial = gender + String.format("%02d%02d%02d", year % 100, month, day) + randDigits(3);
        int[] w1 = {1,2,3,4,5,6,7,8,9,1};
        int s1 = 0;
        for (int i = 0; i < 10; i++) s1 += (partial.charAt(i) - '0') * w1[i];
        int c = s1 % 11;
        if (c == 10) {
            int[] w2 = {3,4,5,6,7,8,9,1,2,3};
            int s2 = 0;
            for (int i = 0; i < 10; i++) s2 += (partial.charAt(i) - '0') * w2[i];
            c = s2 % 11 % 10;
        }
        return partial + c;
    }

    // ── PT CC ─────────────────────────────────────────────────────────────────
    private static String genPtCc() {
        String num = randDigits(8);
        char c1 = randAlpha();
        char c2 = randAlpha();
        return num + " " + c1 + c2 + " " + rng().nextInt(1, 10);
    }

    // ── EG TN ─────────────────────────────────────────────────────────────────
    private static String genEgTn() {
        return randDigits(9);
    }

    // ── Dispatch ──────────────────────────────────────────────────────────────
    public static String generate(String type, String locale) {
        switch (type) {
            case "br_cpf":           return genBrCpf();
            case "br_cnpj":          return genBrCnpj();
            case "in_pan":           return genInPan();
            case "in_aadhaar":       return genInAadhaar();
            case "in_gstin":         return genInGstin();
            case "in_epic":          return genInEpic();
            case "cn_ric":           return genCnRic();
            case "mx_curp":          return genMxCurp();
            case "mx_rfc":           return genMxRfc();
            case "it_codicefiscale": return genItCf();
            case "es_dni":           return genEsDni();
            case "es_nie":           return genEsNie();
            case "es_ccc":           return genEsCcc();
            case "de_idnr":          return genDeIdnr();
            case "de_stnr":          return genDeStnr();
            case "pk_cnic":          return genPkCnic();
            case "jp_cn":            return genJpCn();
            case "jp_in":            return genJpIn();
            case "kr_rrn":           return genKrRrn();
            case "kr_brn":           return genKrBrn();
            case "nl_bsn":           return genNlBsn();
            case "pl_pesel":         return genPlPesel();
            case "se_personnummer":  return genSePersonnummer();
            case "dk_cpr":           return genDkCpr();
            case "fi_hetu":          return genFiHetu();
            case "no_fodselsnummer": return genNoFodselsnummer();
            case "au_abn":           return genAuAbn();
            case "au_tfn":           return genAuTfn();
            case "au_acn":           return genAuAcn();
            case "my_nric":          return genMyNric();
            case "th_pin":           return genThPin();
            case "th_tin":           return genThTin();
            case "sg_uen":           return genSgUen();
            case "za_idnr":          return genZaIdnr();
            case "ca_bn":            return genCaBn();
            case "nz_ird":           return genNzIrd();
            case "ar_cuit":          return genArCuit();
            case "ar_dni":           return genArDni();
            case "cl_rut":           return genClRut();
            case "co_nit":           return genCoNit();
            case "il_idnr":          return genIlIdnr();
            case "ro_cnp":           return genRoCnp();
            case "ro_cui":           return genRoCui();
            case "hr_oib":           return genHrOib();
            case "bg_egn":           return genBgEgn();
            case "lt_asmens":        return genLtAsmens();
            case "ee_ik":            return genEeIk();
            case "pt_cc":            return genPtCc();
            case "eg_tn":            return genEgTn();
            default:                 return "ERROR: Unknown IntlIds type '" + type + "'";
        }
    }
}
