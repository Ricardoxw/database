package edu.utils;


import edu.constant.CommandType;
import edu.constant.SqlParser;

public class Executor {
    public static String use(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.USE, sql);
        String dbName = params[0];
        return "[ERROR] Method not implemented";
    }

    public static String createDatabase(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.CREATE_DATABASE, sql);
        String dbName = params[0];
        return "[ERROR] Method not implemented";
    }

    public static String createTable(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.CREATE_TABLE, sql);
        String tableName = params[0];
        String[] columns = params[1].split(",");
        return "[ERROR] Method not implemented";
    }

    public static String insert(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.INSERT, sql);
        String tableName = params[0];
        String[] values = params[1].split(",");
        return "[ERROR] Method not implemented";
    }

    public static String select(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.SELECT, sql);
        String tableName = params[0];
        String[] columns = params[1].split(",");
        String condition = params[2];
        return "[ERROR] Method not implemented";
    }

    public static String update(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.UPDATE, sql);
        String tableName = params[0];
        String updates = params[1];
        String condition = params[2];
        return "[ERROR] Method not implemented";
    }

    public static String delete(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.DELETE, sql);
        String tableName = params[0];
        String condition = params[1];
        return "[ERROR] Method not implemented";
    }

    public static String dropDatabase(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.DROP_DATABASE, sql);
        String dbName = params[0];
        return "[ERROR] Method not implemented";
    }

    public static String dropTable(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.DROP_TABLE, sql);
        String tableName = params[0];
        return "[ERROR] Method not implemented";
    }

    public static String alterTableAdd(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.ALTER_TABLE_ADD, sql);
        String tableName = params[0];
        String column = params[1];
        return "[ERROR] Method not implemented";
    }

    public static String alterTableDrop(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.ALTER_TABLE_DROP, sql);
        String tableName = params[0];
        String column = params[1];
        return "[ERROR] Method not implemented";
    }

    public static String join(String sql) {
        String[] params = SqlParser.parseSQL(CommandType.JOIN, sql);
        String tableName1 = params[0];
        String tableName2 = params[1];
        String column1 = params[2];
        String column2 = params[3];
        return "[ERROR] Method not implemented";
    }

}
