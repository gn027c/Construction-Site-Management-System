package util;

import exception.DataValidationException;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IdentifierHelper {
    public static final int MAX_NUMBER = 999_999_999;
    private static final int MAX_DIGITS = 9;
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
        if (!matcher.matches() || clean.equalsIgnoreCase("NONE")) {
            throw new DataValidationException(field, "Use a letter prefix followed by a positive numeric suffix.");
        }
        String prefix = matcher.group(1).toUpperCase(Locale.ROOT);
        return prefix + number(matcher.group(2), field);
    }

    public static String normalizeForLookup(String value) {
        try {
            return normalize(value);
        } catch (DataValidationException e) {
            return null;
        }
    }

    public static String next(String prefix, Iterable<String> existing, int floor) {
        int largest = floor;
        for (String identifier : existing) {
            String normalized = normalizeForLookup(identifier);
            if (normalized != null) {
                Matcher matcher = CODE.matcher(normalized);
                matcher.matches();
                if (matcher.group(1).equals(prefix)) {
                    largest = Math.max(largest, Integer.parseInt(matcher.group(2)));
                }
            }
        }
        if (largest >= MAX_NUMBER) {
            throw new DataValidationException(
                    "identifier",
                    "No " + prefix + " identifiers remain; the limit is " + prefix + MAX_NUMBER + "."
            );
        }
        return prefix + (largest + 1);
    }

    private static int number(String digits, String field) {
        String significant = digits.replaceFirst("^0+", "");
        if (significant.isEmpty()) {
            throw new DataValidationException(field, "The numeric suffix must be greater than zero.");
        }
        if (significant.length() > MAX_DIGITS) {
            throw new DataValidationException(field, "The numeric suffix must not exceed " + MAX_NUMBER + ".");
        }
        return Integer.parseInt(significant);
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
        return Integer.compare(Integer.parseInt(left.group(2)), Integer.parseInt(right.group(2)));
    }
}
