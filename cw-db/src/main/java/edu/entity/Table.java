package edu.entity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Table {
    private String tableName;
    private List<String[]> rows;
    private ArrayList<String> columns;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String[]> getRows() {
        return rows;
    }

    public void setRows(List<String[]> rows) {
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

    public Table loadTable(String filePath) throws IOException {
        Table table = new Table();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            table.setColumns(new ArrayList<>(List.of(br.readLine().split("\t"))));
            while ((line = br.readLine()) != null) {
                String[] row = line.split("\t");
                table.rows.add(row);
            }
        }
        return table;
    }

    public void saveTable(String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(String.join("\t", columns));
            bw.newLine();

            for (String[] row : rows) {
                bw.write(String.join("\t", row));
                bw.newLine();
            }
        }
    }
}
