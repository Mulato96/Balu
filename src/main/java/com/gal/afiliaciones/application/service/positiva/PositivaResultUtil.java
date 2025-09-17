package com.gal.afiliaciones.application.service.positiva;

import java.util.*;

public final class PositivaResultUtil {

    private static final List<String> STATUS_KEYS = Arrays.asList(
            "codigo", "code", "status", "estado", "resultado", "result"
    );

    private PositivaResultUtil() {}

    public static Integer deriveResultCode(Object response) {
        if (response == null) return -1;
        if (response instanceof Number number) return fromNumber(number);
        if (response instanceof String str) return fromString(str);
        if (response instanceof Map<?,?> map) return fromMap(map);
        if (response instanceof Collection<?>) return 0; // assume ok when list provided
        return 0; // default success when object is present
    }

    private static Integer extractFromMap(Map<?,?> map) {
        // direct check
        for (String key : STATUS_KEYS) {
            Object value = findKeyIgnoreCase(map, key);
            Integer parsed = parseZeroMinusOne(value);
            if (parsed != null) return parsed;
        }
        // recursive search in nested maps
        for (Object v : map.values()) {
            if (v instanceof Map<?,?> nested) {
                Integer nestedVal = extractFromMap(nested);
                if (nestedVal != null) return nestedVal;
            }
        }
        return null;
    }

    private static Object findKeyIgnoreCase(Map<?,?> map, String key) {
        for (Map.Entry<?,?> e : map.entrySet()) {
            Object k = e.getKey();
            if (k != null && k.toString().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    private static Integer parseZeroMinusOne(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) {
            int i = n.intValue();
            if (i == 0 || i == -1) return i;
            return null;
        }
        if (value instanceof String s) {
            try {
                int i = Integer.parseInt(s.trim());
                if (i == 0 || i == -1) return i;
            } catch (NumberFormatException ignored) { }
        }
        return null;
    }

    private static Integer fromNumber(Number number) {
        int value = number.intValue();
        return (value == 0 || value == -1) ? value : 0;
    }

    private static Integer fromString(String str) {
        try {
            int value = Integer.parseInt(str.trim());
            return (value == 0 || value == -1) ? value : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static Integer fromMap(Map<?,?> map) {
        Integer direct = extractFromMap(map);
        if (direct != null) return direct;
        Integer fromErrors = fromErrors(map);
        if (fromErrors != null) return fromErrors;
        return 0;
    }

    private static Integer fromErrors(Map<?,?> map) {
        Object errors = findKeyIgnoreCase(map, "errors");
        if (errors instanceof Collection<?> coll) {
            return coll.isEmpty() ? 0 : -1;
        }
        if (errors instanceof Map<?,?> errMap) {
            return errMap.isEmpty() ? 0 : -1;
        }
        return null;
    }
}


