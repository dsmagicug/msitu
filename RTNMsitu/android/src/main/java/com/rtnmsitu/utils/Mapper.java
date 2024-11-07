package com.rtnmsitu.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class Mapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Configure date format and timezone globally
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(dateFormat);

        // Disable writing dates as timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static WritableMap toWritableMap(Object obj) {
        WritableMap map = Arguments.createMap();
        if (obj == null) {
            return map;
        }

        // Check if the object is a Map itself
        if (obj instanceof Map<?, ?> objMap) {
            for (Map.Entry<?, ?> entry : objMap.entrySet()) {
                String key = entry.getKey().toString();
                addValueToMap(map, key, entry.getValue());
            }
            return map;
        }

        // For other types of objects, use reflection to get fields
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            // Skip the Companion object and any static fields
            if (field.getName().equals("Companion") || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            try {
                String key = field.getName();
                Object value = field.get(obj);
                addValueToMap(map, key, value);
            } catch (IllegalAccessException e) {
                Log.e("msitu.utils", Objects.requireNonNull(e.getMessage()));
            }
        }

        return map;
    }

    private static void addValueToMap(WritableMap map, String key, Object value) {
        if (value == null) {
            map.putNull(key);
        } else if (value instanceof String) {
            map.putString(key, (String) value);
        } else if (value instanceof Integer) {
            map.putInt(key, (Integer) value);
        } else if (value instanceof Double) {
            map.putDouble(key, (Double) value);
        } else if (value instanceof Boolean) {
            map.putBoolean(key, (Boolean) value);
        } else if (value instanceof Date) {
            // Format date objects to UTC with specified format
            map.putString(key, objectMapper.convertValue(value, String.class));
        } else if (value instanceof Collection) {
            map.putArray(key, toWritableArray((List<?>) value));
        } else if (value instanceof Map) {
            map.putMap(key, toWritableMap(value));
        } else {
            map.putString(key, value.toString());
        }
    }

    public static WritableArray toWritableArray(List<?> list) {
        WritableArray array = Arguments.createArray();
        for (Object item : list) {
            if (item == null) {
                array.pushNull();
            } else if (item instanceof String) {
                array.pushString((String) item);
            } else if (item instanceof Integer) {
                array.pushInt((Integer) item);
            } else if (item instanceof Double) {
                array.pushDouble((Double) item);
            } else if (item instanceof Boolean) {
                array.pushBoolean((Boolean) item);
            } else if (item instanceof List) {
                array.pushArray(toWritableArray((List<?>) item));
            } else if (item instanceof Map) {
                array.pushMap(toWritableMap(item));
            } else {
               // try convert it to map
                array.pushMap(toWritableMap(item));
            }
        }
        return array;
    }
}