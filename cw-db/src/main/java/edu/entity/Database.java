package edu.entity;

import edu.uob.DBServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Database {
    private HashMap<String, Table> tables;
    private String name;
    private String storagePath;

    //lazy loading
    public Database(String dbName, String filePath) {
        this.name = dbName;
        this.storagePath = filePath;
        this.tables = new HashMap<>();
    }

    public static String dropDatabase(DBServer dbServer, String dbName) {
        Database db = new Database(dbName, dbServer.getStorageFolderPath());
        String storagePath = db.storagePath;
        String dbFolderPath = storagePath + File.separator + dbName;
        File dbFolder = new File(dbFolderPath);
        if (dbFolder.exists()) {
            if (deleteDirectory(dbFolder)) {
                return "[OK] Database dropped: " + dbName;
            } else {
                return "[ERROR] Failed to delete database folder: " + dbName;
            }
        } else {
            return "[ERROR] Database does not exist: " + dbName;
        }

    }

    private static boolean deleteDirectory(File directory) {
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

    public boolean containsTable(String tableName) {
        String tableFilePath = storagePath + File.separator + tableName + ".tab";
        File tableFile = new File(tableFilePath);
        return tableFile.exists();
    }

    public String createTable(String tableName, String[] columns) {
        String tableFilePath = storagePath + File.separator + tableName + ".tab";
        File tableFile = new File(tableFilePath);

        if (tableFile.exists()) {
            return "[ERROR] Failed to create table file: " + tableName + ", the table already exists.";
        }

        try (FileWriter writer = new FileWriter(tableFile)) {
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
        String tableFilePath = storagePath + File.separator + tableName + ".tab";
        File tableFile = new File(tableFilePath);
        if(tables.containsKey(tableName)) {
            return tables.get(tableName);
        }else if(tableFile.exists()) {
            this.tables.put(tableName, Table.loadTable(tableFilePath));
            return tables.get(tableName);
        }else{
            return null;
        }
    }

    public String joinTables(Table table1, Table table2, String column1, String column2) {
        return "";
    }

    public String remove(String tableName) throws IOException {
        Table table = getTable(tableName);
        tables.remove(tableName);
        return Table.dropTable(table);
    }
}
