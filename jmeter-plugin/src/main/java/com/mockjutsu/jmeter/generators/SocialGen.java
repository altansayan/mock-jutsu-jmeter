package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class SocialGen {
    private SocialGen() {}

    private static final String[] ADJECTIVES = {
        "cool","super","pro","mega","ultra","fast","smart","lazy","happy","dark",
        "swift","silent","bright","bold","sharp","wild","calm","quick","lone","epic"
    };
    private static final String[] NOUNS = {
        "cat","dog","dev","coder","ninja","guru","wizard","hacker","pixel","byte",
        "fox","wolf","hawk","lion","bear","tiger","eagle","shark","viper","storm"
    };
    private static final String[] TOPICS = {
        "tech","ai","dev","code","startup","fintech","java","mock","test","data",
        "cloud","devops","python","security","mobile","backend","frontend","api","ux","infra"
    };
    private static final String[] BIO_TMPL = {
        "Building things with code",
        "Senior %s developer | %s enthusiast",
        "Open source contributor | %s lover",
        "Making the web better one commit at a time",
        "Coffee-driven developer",
        "%s engineer by day, %s hacker by night",
        "Turning coffee into %s code",
        "Passionate about %s and %s"
    };
    private static final String[] LANGS = {"Java","Python","Go","Rust","TypeScript","Kotlin","Swift","Dart"};

    // Power-law tier weights matching social.py: tiers (max, weight)
    // 0-99: w=40, 100-999: w=30, 1k-9999: w=18, 10k-99999: w=8, 100k-999999: w=3, 1M-5M: w=1
    private static final int[][] FOLLOWER_TIERS = {
        {100, 40}, {1000, 30}, {10000, 18}, {100000, 8}, {1000000, 3}, {5000000, 1}
    };

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "username"       -> username(rng);
            case "handle"         -> "@" + username(rng);
            case "hashtag"        -> hashtag(rng);
            case "bio"            -> bio(rng);
            case "follower_count" -> String.valueOf(followerCount(rng));
            default -> "ERROR: Unknown social type '" + type + "'";
        };
    }

    private static String username(ThreadLocalRandom rng) {
        String result;
        int strategy = rng.nextInt(3);
        if (strategy == 0) {
            // adj + noun
            result = pick(rng, ADJECTIVES) + pick(rng, NOUNS);
        } else if (strategy == 1) {
            // noun + 1000-9999
            result = pick(rng, NOUNS) + (1000 + rng.nextInt(9000));
        } else {
            // adj + noun + 10-99
            result = pick(rng, ADJECTIVES) + pick(rng, NOUNS) + (10 + rng.nextInt(90));
        }
        // clamp to 4-15 chars
        if (result.length() < 4) result = result + "user";
        if (result.length() > 15) result = result.substring(0, 15);
        return result;
    }

    private static String hashtag(ThreadLocalRandom rng) {
        String topic = pick(rng, TOPICS);
        if (rng.nextBoolean()) {
            return "#" + topic;
        } else {
            return "#" + topic + (1000 + rng.nextInt(9000));
        }
    }

    private static String bio(ThreadLocalRandom rng) {
        String tmpl = BIO_TMPL[rng.nextInt(BIO_TMPL.length)];
        long pct = tmpl.chars().filter(c -> c == '%').count();
        if (pct == 0) return tmpl;
        if (pct == 1) return String.format(tmpl, pick(rng, LANGS));
        return String.format(tmpl, pick(rng, LANGS), pick(rng, LANGS));
    }

    private static int followerCount(ThreadLocalRandom rng) {
        int totalWeight = 0;
        for (int[] tier : FOLLOWER_TIERS) totalWeight += tier[1];
        int r = rng.nextInt(totalWeight);
        int cumulative = 0;
        int min = 0;
        for (int[] tier : FOLLOWER_TIERS) {
            cumulative += tier[1];
            if (r < cumulative) {
                int max = tier[0];
                return min + rng.nextInt(max - min);
            }
            min = tier[0];
        }
        return rng.nextInt(1000000);
    }

    private static <T> T pick(ThreadLocalRandom rng, T[] arr) { return arr[rng.nextInt(arr.length)]; }
}
