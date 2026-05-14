package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class SocialGen {
    private SocialGen() {}
    private static final String[] ADJECTIVES = {"cool","super","pro","mega","ultra","fast","smart","lazy","happy","sad"};
    private static final String[] NOUNS      = {"cat","dog","dev","coder","ninja","guru","wizard","hacker","pixel","byte"};
    private static final String[] HASHTAGS   = {"#tech","#ai","#dev","#code","#startup","#fintech","#java","#mock","#test","#data"};
    private static final String[] BIO_TMPL   = {
        "Building things with code ☕","Senior %s developer | %s enthusiast","Open source contributor | %s lover",
        "Making the web better one commit at a time","Coffee-driven developer"
    };
    private static final String[] LANGS = {"Java","Python","Go","Rust","TypeScript","Kotlin"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "username"        -> ADJECTIVES[rng.nextInt(ADJECTIVES.length)] + NOUNS[rng.nextInt(NOUNS.length)] + rng.nextInt(10,9999);
            case "handle"          -> "@" + ADJECTIVES[rng.nextInt(ADJECTIVES.length)] + NOUNS[rng.nextInt(NOUNS.length)] + rng.nextInt(10,999);
            case "hashtag"         -> HASHTAGS[rng.nextInt(HASHTAGS.length)];
            case "bio"             -> String.format(BIO_TMPL[rng.nextInt(BIO_TMPL.length)], LANGS[rng.nextInt(LANGS.length)], LANGS[rng.nextInt(LANGS.length)]);
            case "follower_count"  -> String.valueOf(rng.nextInt(0, 1000000));
            default -> "ERROR: Unknown social type '" + type + "'";
        };
    }
}
