package edu.utils;

import edu.constant.Constants;

import java.io.File;
import java.util.*;

public class ToolUtils {
    public static String printTable(List<String> columns, List<ArrayList<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\t", columns)).append("\n");
        sb.append("-".repeat(columns.size() * 10)).append("\n");
        for (ArrayList<String> row : rows) {
            sb.append(String.join("\t", row)).append("\n");
        }
        return sb.toString();
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                }
            }
        }
        return directory.delete();
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String generateId() {
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        return String.valueOf(timestamp) + random;
    }

    public static int getIndexIgnoreCase(String column, ArrayList<String> columns) {
        return columns.stream()
                .mapToInt(col -> col.equalsIgnoreCase(column) ? columns.indexOf(col) : -1)
                .filter(i -> i != -1)
                .findFirst()
                .orElse(-1);
    }

    public static int indexOfIgnoreCase(String source, String target) {
        if (source == null || target == null) {
            return -1;
        }
        return source.toLowerCase().indexOf(target.toLowerCase());
    }

    public static boolean like(String value, String pattern) {
        if (pattern.startsWith("'") && pattern.endsWith("'")) {
            pattern = pattern.substring(1, pattern.length() - 1);
        }
        return value.contains(pattern);
    }

    public static boolean checkLikeCondition(String rowValue, int rowIndex, String conditionValue, int conditionIndex) {
        if (conditionIndex == conditionValue.length()) {
            return rowValue.length() == rowIndex;
        }
        char conditionChar = conditionValue.charAt(conditionIndex);
        if (conditionChar == '%') {
            if (conditionIndex == conditionValue.length() - 1) {
                return true;
            }
            for (int i = rowIndex; i <= rowValue.length(); i++) {
                if (checkLikeCondition(rowValue, i, conditionValue, conditionIndex + 1)) {
                    return true;
                }
            }
            return false;
        }
        if (conditionChar == '_') {
            if (rowIndex >= rowValue.length()) {
                return false;
            }
            return checkLikeCondition(rowValue, rowIndex + 1, conditionValue, conditionIndex + 1);
        }
        if (rowIndex >= rowValue.length() || rowValue.charAt(rowIndex) != conditionChar) {
            return false;
        }
        return checkLikeCondition(rowValue, rowIndex + 1, conditionValue, conditionIndex + 1);
    }

    public static void checkColumnValid(String columnName) {
        if (Constants.SQL_KEYWORDS.contains(columnName.toUpperCase())) {
            throw new IllegalArgumentException(columnName + " is a keyword of SQL.");
        }
    }

    public static void checkConditionColumnValid(String columnName) {
        if (Constants.SQL_KEYWORDS.contains(columnName.toUpperCase())) {
            throw new IllegalArgumentException("Condition has a column named '" + columnName + "' which is an SQL keyword.");
        }
    }

    public static void checkColumnsValid(String[] columns) {
        for (String column : columns) {
            checkColumnValid(column.trim());
        }
    }

    public static void checkColumnsValid(List<String> columns) {
        for (String column : columns) {
            checkColumnValid(column.trim());
        }
    }

    public static boolean checkColumnEqualsId(String column) {
        if (column.equalsIgnoreCase("id")) {
            return true;
        }
        return false;
    }

    public static boolean checkColumnsContainsId(String[] columns) {
        for (String column : columns) {
            if (checkColumnEqualsId(column)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkColumnsUnique(String[] columns) {
        Set<String> set = new HashSet<>();
        for (String column : columns) {
            if (!set.add(column)) {
                return false;
            }
        }
        return true;
    }

}
