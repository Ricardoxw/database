package edu.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ToolUtils {
    public static String printTable(ArrayList<String> columns, List<ArrayList<String>> rows) {
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
}
