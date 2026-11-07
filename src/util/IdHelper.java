package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IdHelper {
    private static final Pattern PREFIXED_ID = Pattern.compile("^([A-Za-z]+[-_]?)0*(\\d+)$");

    private IdHelper() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String clean = raw.trim();
        if (clean.isEmpty()) {
            return "";
        }
        Matcher matcher = PREFIXED_ID.matcher(clean);
        if (matcher.matches()) {
            String prefix = matcher.group(1).toUpperCase().replace("-", "").replace("_", "");
            String num = matcher.group(2);
            return prefix + num;
        }
        return clean.toUpperCase();
    }

    public static boolean isSame(String id1, String id2) {
        if (id1 == null && id2 == null) {
            return true;
        }
        if (id1 == null || id2 == null) {
            return false;
        }
        if (id1.trim().equalsIgnoreCase(id2.trim())) {
            return true;
        }
        String n1 = normalize(id1);
        String n2 = normalize(id2);
        return !n1.isEmpty() && n1.equalsIgnoreCase(n2);
    }
}

