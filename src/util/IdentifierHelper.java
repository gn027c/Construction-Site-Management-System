package util;

import exception.DataValidationException;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes business identifiers before storage and lookup.
 * Numeric suffixes are positive and unpadded; input may contain one separator hyphen.
 */
public final class IdentifierHelper {
    private static final Pattern CODE = Pattern.compile("([A-Za-z]+)-?([0-9]+)");
    public static final Comparator<String> NATURAL_ORDER = IdentifierHelper::compare;

    private IdentifierHelper() {
    }

    public static String normalize(String value) {
        return normalize(value, "identifier");
    }

    public static String normalize(String value, String field) {
        String clean = value == null ? "" : value.trim();
        Matcher matcher = CODE.matcher(clean);
        if (!matcher.matches() || clean.endsWith("-") || clean.equalsIgnoreCase("NONE")) {
            throw new DataValidationException(field, "Use a letter prefix followed by a positive numeric suffix.");
        }
        String prefix = matcher.group(1).toUpperCase(Locale.ROOT);
        String suffix = matcher.group(2);
        BigInteger number = new BigInteger(suffix);
        if (number.signum() <= 0) {
            throw new DataValidationException(field, "The numeric suffix must be greater than zero.");
        }
        return prefix + number;
    }

    public static String normalizeForLookup(String value) {
        try {
            return normalize(value);
        } catch (DataValidationException e) {
            return null;
        }
    }

    private static int compare(String first, String second) {
        Matcher left = CODE.matcher(normalize(first));
        Matcher right = CODE.matcher(normalize(second));
        left.matches();
        right.matches();
        int prefix = left.group(1).compareTo(right.group(1));
        if (prefix != 0) {
            return prefix;
        }
        BigInteger leftNumber = new BigInteger(left.group(2));
        BigInteger rightNumber = new BigInteger(right.group(2));
        return leftNumber.compareTo(rightNumber);
    }
}
