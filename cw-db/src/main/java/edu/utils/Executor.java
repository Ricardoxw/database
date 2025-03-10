package edu.utils;


import edu.constant.CommandType;
import edu.entity.Database;
import edu.entity.Table;
import edu.uob.DBServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Executor {
    public static String use(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.USE, sql);
        String dbName = params[0].toLowerCase().trim();
        if (Database.isExistDataBase(dbServer, dbName)) {
            Database db = new Database(dbName, dbServer.getStorageFolderPath() + File.separator + dbName);
            dbServer.setDatabase(db);
            return "";
        } else {
            throw new IllegalArgumentException("Database " + dbName + " does not exist");
        }
    }

    public static String createDatabase(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.CREATE_DATABASE, sql);
        String dbName = params[0];
        File storageFolder = new File(dbServer.getStorageFolderPath() + File.separator + dbName);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
            return "";
        } else {
            throw new IllegalArgumentException("Attempting to create a database that already exists");
        }
    }

    public static String createTable(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.CREATE_TABLE, sql);
        String tableName = params[0];
        String[] columns = Arrays.stream(params[1].split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (!ToolUtils.checkColumnsUnique(columns)) {
            throw new IllegalArgumentException("Creating a table with duplicate column names");
        }
        ToolUtils.checkColumnsValid(columns);
        Database db = dbServer.getDatabase();
        return db.createTable(tableName, columns);
    }

    public static String insert(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.INSERT, sql);
        String tableName = params[0];
        String[] values = params[1].split(",");
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        if(values.length != table.getColumnNames().size() - 1) {
            throw new IllegalArgumentException("Trying to insert too many (or too few) values into a table entry");
        }
        return table.insert(new ArrayList<>(List.of(values)));
    }

    public static String select(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.SELECT, sql);
        String tableName = params[0];
        List<String> columns;
        String condition = params[2];
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        if (Objects.equals(params[1], "*")) {
            columns = dbServer.getDatabase().getTable(tableName).getColumnNames();
        } else {
            columns = Arrays.stream(params[1].split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty()).toList();
        }
        ToolUtils.checkColumnsValid(columns);
        return table.select(columns, condition);
    }

    public static String update(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.UPDATE, sql);
        String tableName = params[0];
        String updates = params[1];
        String condition = params[2];
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        return table.update(updates, condition);
    }

    public static String delete(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.DELETE, sql);
        String tableName = params[0];
        String condition = params[1];
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        return table.delete(condition);
    }

    public static String dropDatabase(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.DROP_DATABASE, sql);
        String dbName = params[0];
        return Database.dropDatabase(dbServer, dbName);
    }

    public static String dropTable(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.DROP_TABLE, sql);
        String tableName = params[0];
        Database db = dbServer.getDatabase();
        return db.remove(tableName);
    }

    public static String alterTableAdd(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.ALTER_TABLE_ADD, sql);
        String tableName = params[0];
        String[] columns = Arrays.stream(params[1].split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        ToolUtils.checkColumnsValid(columns);
        if (!ToolUtils.checkColumnsUnique(columns)) {
            throw new IllegalArgumentException("Trying to add a column with an existing name");
        }
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        return table.addColumns(new ArrayList<>(List.of(columns)));
    }

    public static String alterTableDrop(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.ALTER_TABLE_DROP, sql);
        String tableName = params[0];
        String[] columns = Arrays.stream(params[1].split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        ToolUtils.checkColumnsValid(columns);
        if (ToolUtils.checkColumnsContainsId(columns)) {
            throw new IllegalArgumentException("Attempting to remove the ID column from a table");
        }
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        return table.dropColumns(new ArrayList<>(List.of(columns)));
    }

    public static String join(String sql, DBServer dbServer) throws Exception {
        String[] params = SqlParser.parseSQL(CommandType.JOIN, sql);
        String tableName1 = params[0];
        String tableName2 = params[1];
        String column1 = params[2];
        String column2 = params[3];
        ToolUtils.checkColumnValid(column1);
        ToolUtils.checkColumnValid(column2);
        Database db = dbServer.getDatabase();
        Table table1 = db.getTable(tableName1);
        Table table2 = db.getTable(tableName2);
        return db.joinTables(table1, table2, column1, column2);
    }


}
