package com.serve.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ObjectGetIdUtil {

    public static Long getIdFromObject(Object param, String variable) {
        if (param == null) {
            return null;
        }

        try {
            Method getIdMethod = param.getClass().getMethod("get" + upperCaseFirstLetter(variable));
            Object idValue = getIdMethod.invoke(param);
            return convertToLong(idValue);
        }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            try {
                Field idField = param.getClass().getDeclaredField(variable);
                idField.setAccessible(true);
                Object idValue = idField.get(param);
                return convertToLong(idValue);

            } catch (NoSuchFieldException | IllegalAccessException exception) {
                throw new RuntimeException(exception.getMessage());
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static Long convertToLong(Object idValue) {
        if (idValue instanceof Long) { return (Long) idValue; }
        if (idValue instanceof Integer) { return ((Integer) idValue).longValue(); }
        if (idValue instanceof Number) { return ((Number) idValue).longValue(); }
        if (idValue instanceof String) {
            try {
                return Long.valueOf((String) idValue);
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static String upperCaseFirstLetter(String value) {
        char[] chars = value.toCharArray();
        char c = chars[0];
        if (c >= 'a' && c <= 'z') {
            c ^= 32;
        }
        chars[0] = c;
        return new String(chars);
    }
}
