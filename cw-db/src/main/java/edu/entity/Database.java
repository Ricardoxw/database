package edu.entity;

import edu.uob.DBServer;
import edu.utils.ToolUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Database {
    private HashMap<String, Table> tables;
    private String name;
    private static String storagePath = Paths.get("databases").toAbsolutePath().toString();;

    //lazy loading
    public Database(String dbName, String filePath) {
        this.name = dbName.toLowerCase().trim();
        this.storagePath = filePath;
        this.tables = new HashMap<>();
    }

    public static String dropDatabase(DBServer dbServer, String dbName) {
        dbName = dbName.toLowerCase().trim();
        Database db = new Database(dbName, dbServer.getStorageFolderPath());
        String storagePath = db.storagePath;
        String dbFolderPath = storagePath + File.separator + dbName;
        File dbFolder = new File(dbFolderPath);
        if (dbFolder.exists()) {
            if (ToolUtils.deleteDirectory(dbFolder)) {
                return "[OK] Database dropped: " + dbName;
            } else {
                return "[ERROR] Failed to delete database folder: " + dbName;
            }
        } else {
            return "[ERROR] Database does not exist: " + dbName;
        }

    }

    public static boolean isExistDataBase(String dbName) {
        dbName = dbName.toLowerCase().trim();
        String databasePath = storagePath + File.separator + dbName;
        File dbDirectory = new File(databasePath);
        return dbDirectory.exists() && dbDirectory.isDirectory();
    }

    public boolean containsTable(String tableName) {
        String tableFilePath = storagePath + File.separator + tableName.toLowerCase().trim() + ".tab";
        File tableFile = new File(tableFilePath);
        return tableFile.exists();
    }

    public String createTable(String tableName, String[] columns) {
        tableName = tableName.toLowerCase().trim();
        String tableFilePath = storagePath + File.separator + tableName + ".tab";
        File tableFile = new File(tableFilePath);

        if (tableFile.exists()) {
            return "[ERROR] Failed to create table file: " + tableName + ", the table already exists.";
        }

        try (FileWriter writer = new FileWriter(tableFile)) {
            writer.write("id");
            writer.write("\t");
            for (int i = 0; i < columns.length; i++) {
                writer.write(columns[i]);
                if (i < columns.length - 1) {
                    writer.write("\t");
                }
            }
            writer.write("\n");
        } catch (IOException e) {
            return "[ERROR] Failed to create table file: " + e.getMessage();
        }

        return "[OK] Table created: " + tableName;
    }

    public Table getTable(String tableName) throws IOException {
        tableName = tableName.toLowerCase().trim();
        String tableFilePath = storagePath + File.separator + tableName + ".tab";
        File tableFile = new File(tableFilePath);
        if(tables.containsKey(tableName)) {
            return tables.get(tableName);
        }else if(tableFile.exists()) {
            this.tables.put(tableName, Table.loadTable(tableName, tableFilePath));
            return tables.get(tableName);
        }else{
            return null;
        }
    }

    public String joinTables(Table table1, Table table2, String column1, String column2) {
        ArrayList<String> columnsT1 = table1.getColumnNames();
        ArrayList<String> columnsT2 = table2.getColumnNames();

        int index1 = ToolUtils.getIndexIgnoreCase(column1, columnsT1);
        int index2 = ToolUtils.getIndexIgnoreCase(column2, columnsT2);

        if (index1 == -1 || index2 == -1) {
            return "[ERROR] Join columns not found.";
        }

        ArrayList<String> resultColumns = new ArrayList<>();
        for (String column : columnsT2) {
            if (!column.equals(column1)) {
                resultColumns.add(table1.getTableName() + "." + column);
            }
        }
        for (String column : columnsT2) {
            if (!column.equals(column2)) {
                resultColumns.add(table2.getTableName() + "." + column);
            }
        }

        resultColumns.add("id");

        List<ArrayList<String>> resultRows = new ArrayList<>();
        List<ArrayList<String>> rows1 = table1.getRows();
        List<ArrayList<String>> rows2 = table2.getRows();

        for (ArrayList<String> row1 : rows1) {
            for (ArrayList<String> row2 : rows2) {
                if (row1.get(index1).equals(row2.get(index2))) {
                    ArrayList<String> newRow = new ArrayList<>();
                    newRow.add(ToolUtils.generateId());
                    for (int i = 1; i < columnsT1.size(); i++) {
                        if (i != index1) {
                            newRow.add(row1.get(i));
                        }
                    }
                    for (int i = 1; i < columnsT2.size(); i++) {
                        if (i != index2) {
                            newRow.add(row2.get(i));
                        }
                    }
                    resultRows.add(newRow);
                }
            }
        }

        return ToolUtils.printTable(resultColumns, resultRows);
    }

    public String remove(String tableName) throws IOException {
        tableName = tableName.toLowerCase().trim();
        Table table = getTable(tableName);
        tables.remove(tableName);
        return Table.dropTable(table);
    }
}
