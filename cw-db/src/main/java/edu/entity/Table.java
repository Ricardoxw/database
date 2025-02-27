package edu.entity;

import edu.utils.ToolUtils;

import java.io.*;
import java.util.*;

public class Table {
    private String tableName;
    private List<ArrayList<String>> rows;
    private ArrayList<String> columnNames;

    public Table() {
        rows = new ArrayList<>();
    }

    private String storagePath;

    public static String dropTable(Table table) {
        String storagePath = table.getStoragePath();
        String tableName = table.getTableName().toLowerCase().trim();
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

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(ArrayList<String> columnNames) {
        this.columnNames = columnNames;
    }

    public static Table loadTable(String tableName, String filePath) throws IOException {
        tableName = tableName.toLowerCase().trim();
        Table table = new Table();
        table.setStoragePath(filePath);
        table.setTableName(tableName);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            table.setColumnNames(new ArrayList<>(List.of(br.readLine().split("\t"))));
            while ((line = br.readLine()) != null) {
                ArrayList<String> row = new ArrayList<>(List.of(line.split("\t")));
                table.rows.add(row);
            }
        }
        return table;
    }

    public void appendRowToFile(String filePath, ArrayList<String> row) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(String.join("\t", row));
            bw.newLine();
        }
    }

    public void saveTable() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(storagePath))) {
            bw.write(String.join("\t", columnNames));
            bw.newLine();
            for (ArrayList<String> row : rows) {
                bw.write(String.join("\t", row));
                bw.newLine();
            }
        }
    }

    public String insert(ArrayList<String> values) throws IOException {
        String id = ToolUtils.generateId();
        ArrayList<String> row = new ArrayList<>();
        row.add(id);
        row.addAll(values);
        rows.add(row);
        appendRowToFile(storagePath, row);
        return "[OK]";
    }

    public String addColumns(ArrayList<String> columns) throws IOException{
        int count = 0;
        for (String column : columns) {
            if (columnNames.stream().noneMatch(col -> col.equalsIgnoreCase(column))) {
                columnNames.add(column);
                count++;
                for (ArrayList<String> row : rows) {
                    row.add(" ");
                }
            }
        }
        saveTable();
        return "[OK] Add " + count + " columns.";
    }

    public String dropColumns(ArrayList<String> columns) throws IOException{
        List<Integer> columnIndexes = new ArrayList<>();
        int count = 0;
        for (String column : columns) {
            int index = ToolUtils.getIndexIgnoreCase(column, columnNames);
            if (index == -1) {
                return "[ERROR] Column not found: " + column;
            }
            columnIndexes.add(index);
        }

        columnIndexes.sort(Collections.reverseOrder());

        for (int index : columnIndexes) {
            columnNames.remove(index);
            count++;
            for (ArrayList<String> row : rows) {
                row.remove(index);
            }
        }
        saveTable();
        return "[OK] Drop " + count + " columns.";
    }

    public String delete(String conditionStr) throws IOException{
        Expression condition = new Expression(conditionStr);
        int count = 0;
        for (Iterator<ArrayList<String>> it = rows.iterator(); it.hasNext(); ) {
            ArrayList<String> row = it.next();
            if (condition.isConditionSatisfied(columnNames, row)) {
                it.remove();
                count++;
            }
        }
        saveTable();
        return "[OK] Deleted " + count + " rows.";
    }

    public String update(String updatesStr, String conditionStr) throws IOException{
        Expression condition = new Expression(conditionStr);
        int count = 0;

        String[] updates = updatesStr.split(",");
        Map<String, String> updateMap = new HashMap<>();
        for (String update : updates) {
            String[] params = update.trim().split("=");
            String column = params[0].trim();
            String value = params[1].trim();
            updateMap.put(column, value);
        }

        for (ArrayList<String> row : rows) {
            if (condition.isConditionSatisfied(columnNames, row)) {
                for (Map.Entry<String, String> entry : updateMap.entrySet()) {
                    int columnIndex = ToolUtils.getIndexIgnoreCase(entry.getKey(), columnNames);
                    if (columnIndex != -1) {
                        row.set(columnIndex, entry.getValue());
                    }
                }
                count++;
            }
        }
        saveTable();
        return "[OK] Updated " + count + " rows.";
    }

    public String select(ArrayList<String> columns, String conditionStr) {
        Expression condition = new Expression(conditionStr);
        List<ArrayList<String>> result = new ArrayList<>();
        for (ArrayList<String> row : rows) {
            if (condition.isConditionSatisfied(columnNames, row)) {
                ArrayList<String> selectedRow = new ArrayList<>();
                for (String column : columns) {
                    int columnIndex = ToolUtils.getIndexIgnoreCase(column, columnNames);;
                    if (columnIndex != -1) {
                        selectedRow.add(row.get(columnIndex));
                    }
                }
                result.add(selectedRow);
            }
        }
        return ToolUtils.printTable(columns, result);
    }
}
