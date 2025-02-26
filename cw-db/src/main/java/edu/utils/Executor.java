package edu.utils;


import edu.constant.CommandType;
import edu.entity.Database;
import edu.entity.Table;
import edu.uob.DBServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Executor {
    public static String use(String sql, DBServer dbServer) {
        String[] params = SqlParser.parseSQL(CommandType.USE, sql);
        String dbName = params[0];
        dbServer.setDatabase(new Database(dbName, dbServer.getStorageFolderPath()+File.separator+dbName));
        return "[ERROR] Method not implemented";
    }

    public static String createDatabase(String sql, DBServer dbServer) {
        String[] params = SqlParser.parseSQL(CommandType.CREATE_DATABASE, sql);
        String dbName = params[0];
        File storageFolder = new File(dbServer.getStorageFolderPath()+File.separator+dbName);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
            return "[OK]";
        }else{
            return "[ERROR] Database already exists: " + dbName;
        }
    }

    public static String createTable(String sql, DBServer dbServer){
        String[] params = SqlParser.parseSQL(CommandType.CREATE_TABLE, sql);
        String tableName = params[0];
        String[] columns = params[1].split(",");
        Database db = dbServer.getDatabase();
        return db.createTable(tableName, columns);
    }

    public static String insert(String sql, DBServer dbServer) throws IOException {
        String[] params = SqlParser.parseSQL(CommandType.INSERT, sql);
        String tableName = params[0];
        String[] values = params[1].split(",");
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        table.insert(new ArrayList<>(List.of(values)));
        return "[ERROR] Method not implemented";
    }

    public static String select(String sql, DBServer dbServer) throws IOException {
        String[] params = SqlParser.parseSQL(CommandType.SELECT, sql);
        String tableName = params[0];
        String[] columns = params[1].split(",");
        String condition = params[2];
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        table.select(new ArrayList<>(List.of(columns)), condition);
        return "[ERROR] Method not implemented";
    }

    public static String update(String sql, DBServer dbServer) throws IOException {
        String[] params = SqlParser.parseSQL(CommandType.UPDATE, sql);
        String tableName = params[0];
        String updates = params[1];
        String condition = params[2];
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        table.update(updates, condition);
        return "[ERROR] Method not implemented";
    }

    public static String delete(String sql, DBServer dbServer) throws IOException {
        String[] params = SqlParser.parseSQL(CommandType.DELETE, sql);
        String tableName = params[0];
        String condition = params[1];
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        table.delete(condition);
        return "[ERROR] Method not implemented";
    }

    public static String dropDatabase(String sql, DBServer dbServer) {
        String[] params = SqlParser.parseSQL(CommandType.DROP_DATABASE, sql);
        String dbName = params[0];
        return Database.dropDatabase(dbServer, dbName);
    }

    public static String dropTable(String sql, DBServer dbServer) throws IOException {
        String[] params = SqlParser.parseSQL(CommandType.DROP_TABLE, sql);
        String tableName = params[0];
        Database db = dbServer.getDatabase();
        db.remove(tableName);
        return "[ERROR] Method not implemented";
    }

    public static String alterTableAdd(String sql, DBServer dbServer) throws IOException {
        String[] params = SqlParser.parseSQL(CommandType.ALTER_TABLE_ADD, sql);
        String tableName = params[0];
        String[] columns = params[1].split(",");
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        table.addColumns(new ArrayList<>(List.of(columns)));
        return "[ERROR] Method not implemented";
    }

    public static String alterTableDrop(String sql, DBServer dbServer) throws IOException {
        String[] params = SqlParser.parseSQL(CommandType.ALTER_TABLE_DROP, sql);
        String tableName = params[0];
        String[] columns = params[1].split(",");
        Database db = dbServer.getDatabase();
        Table table = db.getTable(tableName);
        table.dropColumns(new ArrayList<>(List.of(columns)));
        return "[ERROR] Method not implemented";
    }

    public static String join(String sql, DBServer dbServer) throws IOException {
        String[] params = SqlParser.parseSQL(CommandType.JOIN, sql);
        String tableName1 = params[0];
        String tableName2 = params[1];
        String column1 = params[2];
        String column2 = params[3];
        Database db = dbServer.getDatabase();
        Table table1 = db.getTable(tableName1);
        Table table2 = db.getTable(tableName2);
        db.joinTables(table1, table2, column1, column2);
        return "[ERROR] Method not implemented";
    }

}
