package com.piyush.library.util;

import com.piyush.library.model.Item;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Tiny JSON helper so the project needs no external libraries. */
public final class Json {
    private static final Pattern FIELD =
            Pattern.compile("\"(\\w+)\"\\s*:\\s*(?:\"((?:[^\"\\\\]|\\\\.)*)\"|([^,}\\s]+))");

    private Json() {}

    /** Parses a flat JSON object such as {"type":"BOOK","title":"Clean Code"}. */
    public static Map<String, String> parseFlatObject(String body) {
        Map<String, String> result = new HashMap<>();
        Matcher m = FIELD.matcher(body == null ? "" : body);
        while (m.find()) {
            String value = m.group(2) != null ? m.group(2).replace("\\\"", "\"") : m.group(3);
            result.put(m.group(1), value);
        }
        return result;
    }

    public static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String toJson(Item item) {
        return "{\"id\":" + item.getId()
                + ",\"type\":\"" + item.getType() + "\""
                + ",\"title\":\"" + escape(item.getTitle()) + "\""
                + ",\"creator\":\"" + escape(item.getCreator()) + "\""
                + ",\"available\":" + item.isAvailable()
                + ",\"borrowedBy\":" + (item.getBorrowedBy() == null ? "null" : "\"" + escape(item.getBorrowedBy()) + "\"")
                + ",\"dueDate\":" + (item.getDueDate() == null ? "null" : "\"" + item.getDueDate() + "\"")
                + "}";
    }

    public static String toJson(List<Item> items) {
        return items.stream().map(Json::toJson).collect(Collectors.joining(",", "[", "]"));
    }

    public static String message(String key, String value) {
        return "{\"" + key + "\":\"" + escape(value) + "\"}";
    }
}
