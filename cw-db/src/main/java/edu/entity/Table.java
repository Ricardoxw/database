package edu.entity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Table {
    private String tableName;
    private List<ArrayList<String>> rows;
    private ArrayList<String> columns;
    private String storagePath;

    public static String dropTable(Table table) {
        String storagePath = table.getStoragePath();
        String tableName = table.getTableName();
        File tableFile = new File(storagePath);

        if (tableFile.exists()) {
            if (tableFile.delete()) {
                return "[OK] Table dropped: " + tableName;
            } else {
                return "[ERROR] Failed to delete table file: " + tableName;
            }
        } else {
            return "[ERROR] Table does not exist: " + tableName;
        }
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ArrayList<String>> getRows() {
        return rows;
    }

    public void setRows(List<ArrayList<String>> rows) {
        this.rows = rows;
    }

    public ArrayList<String> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<String> columns) {
        this.columns = columns;
    }

    private static String generateId() {
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        return String.valueOf(timestamp) + random;
    }

    public static Table loadTable(String filePath) throws IOException {
        Table table = new Table();
        table.setStoragePath(filePath);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            table.setColumns(new ArrayList<>(List.of(br.readLine().split("\t"))));
            while ((line = br.readLine()) != null) {
                ArrayList<String> row = new ArrayList<>(List.of(line.split("\t")));
                table.rows.add(row);
            }
        }
        return table;
    }

    public void saveTable(String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(String.join("\t", columns));
            bw.newLine();
            for (ArrayList<String> row : rows) {
                bw.write(String.join("\t", row));
                bw.newLine();
            }
        }
    }


    public String insert(ArrayList<String> values) {
        String id = generateId();
        ArrayList<String> row = new ArrayList<>();
        row.add(id);
        row.addAll(values);
        rows.add(row);
        return "";
    }

    public String addColumns(ArrayList<String> columns) {
        columns.addAll(this.columns);
        return "";
    }

    public String dropColumns(ArrayList<String> columns) {
    }

    public String delete(String condition) {
    }

    public String update(String updates, String condition) {
    }

    public String select(ArrayList<String> columns, String condition) {

    }

    public String printTable() {
        StringBuilder sb = new StringBuilder();

        sb.append("Table: ").append(tableName).append("\n");

        for (String column : columns) {
            sb.append(column).append("\t");
        }
        sb.append("\n");
        sb.append("-".repeat(columns.size() * 10)).append("\n");

        for (ArrayList<String> row : rows) {
            for (String cell : row) {
                sb.append(cell).append("\t");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
