package edu.utils;

import edu.constant.Constants;
import edu.entity.Table;

import java.io.File;
import java.util.*;

public class ToolUtils {
    public static String printTable(List<String> columns, List<ArrayList<String>> rows) {
        StringBuilder sb = new StringBuilder();

        // Initialize an array to store the maximum width of each column
        int[] maxWidths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            maxWidths[i] = columns.get(i).length();
        }
        for (ArrayList<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                maxWidths[i] = Math.max(maxWidths[i], row.get(i).length());
            }
        }
        // Format and append the column headers
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sb.append("\t");
            sb.append(String.format("%-" + maxWidths[i] + "s", columns.get(i)));
        }
        sb.append("\n");

        // Format and append each row of data
        for (ArrayList<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) sb.append("\t");
                sb.append(String.format("%-" + maxWidths[i] + "s", row.get(i)));
            }
            sb.append("\n");
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

    // generate unique id by timestamp and append a random number.
    public static String generateId() {
        long timestamp = System.currentTimeMillis();
        int timePart = (int) (timestamp % 1000000);
        int random = new Random().nextInt(100);
        return String.valueOf(timePart) + random;
    }

    public static String generateId(Table table) {
        int index = table.getIndex();
        table.setIndex(index + 1);
        return String.valueOf(index);
    }

    // compare column name ignore case
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

    //just check that if value in pattern
    public static boolean like(String value, String pattern) {
        if (pattern.startsWith("'") && pattern.endsWith("'")) {
            pattern = pattern.substring(1, pattern.length() - 1);
        }
        return value.contains(pattern);
    }

    //complex like condition, I can use it parse condition like "%sim_n"
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

    // make sure that the columns is not a keyword of sql.
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
