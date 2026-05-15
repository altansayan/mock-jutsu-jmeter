package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class CommerceGen {
    private CommerceGen() {}

    // currency: code → {symbol, name, decimals}
    private static final String[][] CURRENCIES = {
        {"USD","$","US Dollar","2"},
        {"EUR","€","Euro","2"},
        {"GBP","£","British Pound","2"},
        {"TRY","₺","Turkish Lira","2"},
        {"JPY","¥","Japanese Yen","0"},
        {"CAD","C$","Canadian Dollar","2"},
        {"CHF","Fr","Swiss Franc","2"},
        {"AUD","A$","Australian Dollar","2"},
        {"CNY","¥","Chinese Yuan","2"},
        {"RUB","₽","Russian Ruble","2"}
    };

    // tax_rate: locale → {rate, name, type}
    private static final String[][][] TAX_RATES = {
        {{"TR"},{"18","KDV","VAT"},{"8","KDV İndirimli","reduced VAT"},{"1","KDV Süper İndirimli","super-reduced VAT"}},
        {{"DE"},{"19","MwSt","VAT"},{"7","MwSt ermäßigt","reduced VAT"}},
        {{"FR"},{"20","TVA","VAT"},{"10","TVA intermédiaire","intermediate VAT"},{"5.5","TVA réduite","reduced VAT"}},
        {{"UK"},{"20","VAT","VAT"},{"5","VAT reduced","reduced VAT"},{"0","VAT zero","zero VAT"}},
        {{"US"},{"0","Sales Tax","varies by state"},{"6.5","Sales Tax avg","state average"}},
        {{"RU"},{"20","НДС","VAT"},{"10","НДС льготный","reduced VAT"}}
    };

    private static final String[] VEHICLE_MAKES  = {
        "Toyota","Honda","Ford","BMW","Mercedes-Benz","Volkswagen","Hyundai","Kia","Renault","Peugeot",
        "Fiat","Volvo","Audi","Chevrolet","Nissan"
    };
    private static final String[] VEHICLE_MODELS = {
        "Sedan","SUV","Hatchback","Pickup","Coupe","Minivan","Crossover","Wagon","Convertible"
    };
    private static final String[] VEHICLE_COLORS = {
        "White","Black","Silver","Red","Blue","Gray","Green","Brown","Yellow","Orange"
    };
    private static final String[] FUEL_TYPES = {"Petrol","Diesel","Electric","Hybrid","LPG"};

    // WMI codes per locale (first 3 chars of VIN)
    private static final String[] WMI_TR = {"NMT","NMB","NM4"};
    private static final String[] WMI_DE = {"WBA","WVW","W0L","WDB","WAU"};
    private static final String[] WMI_FR = {"VF1","VF3","VF7","VF6"};
    private static final String[] WMI_UK = {"SAJ","SAL","SCA","SCF"};
    private static final String[] WMI_US = {"1HG","1FA","2T1","3VW","4T1","1G1"};
    private static final String[] WMI_RU = {"XTA","XW8","X7L"};

    // VIN character value map for check digit (ISO 3779)
    private static final String VIN_CHARS = "ABCDEFGHJKLMNPRSTUVWXYZ0123456789";
    private static final int[] VIN_VALUES = {
        1,2,3,4,5,6,7,8,1,2,3,4,5,7,9,2,3,4,5,6,7,8,9, // letters A-Z (no I,O,Q)
        0,1,2,3,4,5,6,7,8,9 // digits 0-9
    };
    private static final int[] VIN_WEIGHTS = {8,7,6,5,4,3,2,10,0,9,8,7,6,5,4,3,2};

    // Model year codes (position 10): chars 1980-2030
    private static final char[] MODEL_YEAR_CHARS =
        "ABCDEFGHJKLMNPRSTUVWXY123456789ABCDEFGHJKLMNPRSTUVWXY1234".toCharArray();

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "currency"                       -> currency(rng);
            case "tax_rate","taxrate"             -> taxRate(rng, locale);
            case "invoice_number","invoicenumber" -> invoiceNumber(rng, locale);
            case "vin"                            -> vin(rng, locale);
            case "vehicle"                        -> vehicle(rng, locale);
            default -> "ERROR: Unknown commerce type '" + type + "'";
        };
    }

    // currency → JSON dict {code, symbol, name, decimals}
    private static String currency(ThreadLocalRandom rng) {
        String[] c = CURRENCIES[rng.nextInt(CURRENCIES.length)];
        return String.format("{\"code\":\"%s\",\"symbol\":\"%s\",\"name\":\"%s\",\"decimals\":%s}",
            c[0], c[1], c[2], c[3]);
    }

    // tax_rate → JSON dict {rate, name, type}
    private static String taxRate(ThreadLocalRandom rng, String locale) {
        String[][][] pool = null;
        for (String[][] entry : TAX_RATES) {
            if (entry[0][0].equals(locale)) { pool = new String[][][]{entry}; break; }
        }
        if (pool == null) pool = TAX_RATES;
        String[][] localeRates = pool[rng.nextInt(pool.length)];
        // pick a rate row (index 1+)
        int idx = 1 + rng.nextInt(localeRates.length - 1);
        String[] row = localeRates[idx];
        return String.format("{\"rate\":\"%s%%\",\"name\":\"%s\",\"type\":\"%s\"}", row[0], row[1], row[2]);
    }

    // invoice_number — locale-specific format
    private static String invoiceNumber(ThreadLocalRandom rng, String locale) {
        int year  = java.time.LocalDate.now().getYear();
        int month = java.time.LocalDate.now().getMonthValue();
        int day   = java.time.LocalDate.now().getDayOfMonth();
        int n     = 1 + rng.nextInt(999999);
        return switch (locale) {
            case "TR" -> String.format("INV-%d-%06d", year, n);
            case "US" -> String.format("INV-%d%02d%02d-%04d", year, month, day, rng.nextInt(10000));
            case "UK" -> String.format("INV/%d/%06d", year, n);
            case "DE" -> String.format("RE-%d/%05d", year, rng.nextInt(100000));
            case "FR" -> String.format("FACT-%d-%06d", year, n);
            case "RU" -> String.format("СФ-%d-%06d", year, n);
            default   -> String.format("INV-%d-%06d", year, n);
        };
    }

    // VIN — ISO 3779 with WMI per locale and proper check digit
    private static String vin(ThreadLocalRandom rng, String locale) {
        String[] wmis = switch (locale) {
            case "DE" -> WMI_DE; case "FR" -> WMI_FR; case "UK" -> WMI_UK;
            case "US" -> WMI_US; case "RU" -> WMI_RU; default  -> WMI_TR;
        };
        char[] vin = new char[17];
        String wmi = wmis[rng.nextInt(wmis.length)];
        for (int i = 0; i < 3; i++) vin[i] = wmi.charAt(i);

        // VDS (pos 3-8): random VIN_CHARS
        for (int i = 3; i < 8; i++) vin[i] = VIN_CHARS.charAt(rng.nextInt(VIN_CHARS.length()));

        // pos 8 = check digit placeholder (0 for now)
        vin[8] = '0';

        // pos 9 = model year
        int yearOffset = (java.time.LocalDate.now().getYear() - 1980) % MODEL_YEAR_CHARS.length;
        vin[9] = MODEL_YEAR_CHARS[yearOffset];

        // pos 10-16: plant + sequence
        for (int i = 10; i < 17; i++) vin[i] = VIN_CHARS.charAt(rng.nextInt(VIN_CHARS.length()));

        // compute check digit (position 8)
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += vinCharValue(vin[i]) * VIN_WEIGHTS[i];
        }
        int check = sum % 11;
        vin[8] = check == 10 ? 'X' : (char)('0' + check);

        return new String(vin);
    }

    private static int vinCharValue(char c) {
        int idx = VIN_CHARS.indexOf(c);
        return idx >= 0 ? VIN_VALUES[idx] : 0;
    }

    // vehicle → JSON dict {make, model, year, vin, color, fuel}
    private static String vehicle(ThreadLocalRandom rng, String locale) {
        String make  = VEHICLE_MAKES[rng.nextInt(VEHICLE_MAKES.length)];
        String model = VEHICLE_MODELS[rng.nextInt(VEHICLE_MODELS.length)];
        int year     = 2000 + rng.nextInt(26); // 2000-2025
        String vinVal = vin(rng, locale);
        String color  = VEHICLE_COLORS[rng.nextInt(VEHICLE_COLORS.length)];
        String fuel   = FUEL_TYPES[rng.nextInt(FUEL_TYPES.length)];
        return String.format("{\"make\":\"%s\",\"model\":\"%s\",\"year\":%d,\"vin\":\"%s\",\"color\":\"%s\",\"fuel\":\"%s\"}",
            make, model, year, vinVal, color, fuel);
    }
}
