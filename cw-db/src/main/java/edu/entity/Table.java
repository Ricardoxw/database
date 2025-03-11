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
                return "";
            } else {
                throw new IllegalArgumentException("Failed to delete table file: " + tableName);
            }
        } else {
            throw new IllegalArgumentException("Table does not exist: " + tableName);
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

    //load table from filePath
    public static Table loadTable(String tableName, String filePath) throws IOException {
        tableName = tableName.toLowerCase().trim();
        Table table = new Table();
        table.setStoragePath(filePath);
        table.setTableName(tableName);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            ArrayList<String> columnNames = new ArrayList<>();
            String[] columns = br.readLine().split("\t");
            for (String column : columns) {
                columnNames.add(column.trim());
            }
            table.setColumnNames(columnNames);
            //if there is a column without value, put a "" value in row
            while ((line = br.readLine()) != null) {
                String[] rowArray = line.split("\t");
                ArrayList<String> row = new ArrayList<>();
                for (int i = 0; i < columnNames.size(); i++) {
                    if (i < rowArray.length) {
                        row.add(rowArray[i].trim());
                    } else {
                        row.add("");
                    }
                }
                table.rows.add(row);
            }
        }
        return table;
    }

    // Store the table after insert
    public void appendRowToFile(String filePath, ArrayList<String> row) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(String.join("\t", row));
            bw.newLine();
        }
    }

    // Save all table, it may cost long time, also we can improve the time by execute this function before server close instead of sql command commit.
    // but it may lost some data.
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

    public String insert(ArrayList<String> values) throws Exception {
        // insert id, generateId is the function which ensure the id is unique.
        String id = ToolUtils.generateId();
        ArrayList<String> row = new ArrayList<>();
        row.add(id);
        // if it is a string with '', '' should be removed.
        for (String value : values) {
            Expression expression = new Expression(value);
            if (Expression.validateExpressionValue(expression)) {
                if (value.startsWith("'") && value.endsWith("'")) {
                    value = value.substring(1, value.length() - 1).trim();
                }
                row.add(value.trim());
            }
        }
        //save row in memory
        rows.add(row);
        //save in file
        appendRowToFile(storagePath, row);
        return "";
    }

    //just add a column at the end of first row
    public String addColumns(ArrayList<String> columns) throws Exception {
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
        return "Add " + count + " columns.";
    }

    //remove column in the first row
    public String dropColumns(ArrayList<String> columns) throws Exception {
        List<Integer> columnIndexes = new ArrayList<>();
        int count = 0;
        for (String column : columns) {
            int index = ToolUtils.getIndexIgnoreCase(column, columnNames);
            if (index == -1) {
                throw new IllegalArgumentException("Column not found: " + column);
            }
            columnIndexes.add(index);
        }
        //it should be reverse the order, otherwise, it will change the index of other columns
        columnIndexes.sort(Collections.reverseOrder());

        for (int index : columnIndexes) {
            columnNames.remove(index);
            count++;
            for (ArrayList<String> row : rows) {
                row.remove(index);
            }
        }
        saveTable();
        return "Drop " + count + " columns.";
    }

    public String delete(String conditionStr) throws Exception {
        Expression condition = new Expression(conditionStr);
        int count = 0;
        //find the row which is satisfied the condition
        for (Iterator<ArrayList<String>> it = rows.iterator(); it.hasNext(); ) {
            ArrayList<String> row = it.next();
            if (condition.isConditionSatisfied(columnNames, row)) {
                it.remove();
                count++;
            }
        }
        saveTable();
        return "Deleted " + count + " rows.";
    }

    public String update(String updatesStr, String conditionStr) throws Exception {
        Expression condition = new Expression(conditionStr);
        int count = 0;
        String[] updates = updatesStr.split(",");
        for (String update : updates) {
            String[] params = update.trim().split("=");
            String column = params[0].trim();
            ToolUtils.checkColumnValid(column);
            // check column is valid and not id.
            if (ToolUtils.checkColumnEqualsId(column)) {
                throw new IllegalArgumentException("Updating the ID of a record");
            }
            if (ToolUtils.getIndexIgnoreCase(column, columnNames) == -1) {
                throw new IllegalArgumentException("Attribute does not exist");
            }
        }
        //get column and value
        Map<String, String> updateMap = new HashMap<>();
        for (String update : updates) {
            String[] params = update.trim().split("=");
            String column = params[0].trim();
            String value = params[1].trim();
            updateMap.put(column, value);
        }
        //find the row which is satisfied the condition
        for (ArrayList<String> row : rows) {
            if (condition.isConditionSatisfied(columnNames, row)) {
                for (Map.Entry<String, String> entry : updateMap.entrySet()) {
                    int columnIndex = ToolUtils.getIndexIgnoreCase(entry.getKey(), columnNames);
                    if (columnIndex != -1) {
                        row.set(columnIndex, entry.getValue());
                    } else {
                        throw new IllegalArgumentException("Attribute in condition does not exist");
                    }
                }
                count++;
            }
        }
        saveTable();
        return "Updated " + count + " rows.";
    }

    public String select(List<String> columns, String conditionStr) {
        Expression condition = new Expression(conditionStr);
        List<ArrayList<String>> result = new ArrayList<>();
        for (String column : columns) {
            if (ToolUtils.getIndexIgnoreCase(column, columnNames) == -1) {
                throw new IllegalArgumentException("Attribute in condition does not exist");
            }
        }
        //find the row which is satisfied the condition
        for (ArrayList<String> row : rows) {
            if (condition.isConditionSatisfied(columnNames, row)) {
                ArrayList<String> selectedRow = new ArrayList<>();
                for (String column : columns) {
                    int columnIndex = ToolUtils.getIndexIgnoreCase(column, columnNames);
                    selectedRow.add(row.get(columnIndex));
                }
                result.add(selectedRow);
            }
        }

        return ToolUtils.printTable(columns, result);
    }
}
