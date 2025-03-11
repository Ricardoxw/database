package edu.utils;


import edu.constant.CommandType;
import edu.constant.Constants;

public class SqlParser {

    public static String[] parseSQL(CommandType type, String sql) {
        // we parse the sql sentence by split the keywords
        // and get the index of params then split params and return them
        String[] res = null;
        try {
            switch (type) {
                case USE -> res = parseUse(sql);
                case CREATE_DATABASE -> res = parseCreateDatabase(sql);
                case CREATE_TABLE -> res = parseCreateTable(sql);
                case INSERT -> res = parseInsert(sql);
                case SELECT -> res = parseSelect(sql);
                case UPDATE -> res = parseUpdate(sql);
                case DELETE -> res = parseDelete(sql);
                case DROP_DATABASE -> res = parseDropDatabase(sql);
                case DROP_TABLE -> res = parseDropTable(sql);
                case ALTER_TABLE_ADD -> res = parseAlterTableAdd(sql);
                case ALTER_TABLE_DROP -> res = parseAlterTableDrop(sql);
                case JOIN -> res = parseJoin(sql);
                default -> throw new IllegalArgumentException("Invalid SQL: " + sql);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SQL: " + sql);
        }
        return res;
    }

    //USE markbook;
    private static String[] parseUse(String sql) {
        String[] parts = sql.split(" ");
        String dbName = parts[1];
        return new String[]{dbName};
    }

    //CREATE DATABASE markbook;
    private static String[] parseCreateDatabase(String sql) {
        String[] parts = sql.split(" ");
        String dbName = parts[2].toLowerCase();
        return new String[]{dbName};
    }

    //CREATE TABLE students (id, name, age);
    private static String[] parseCreateTable(String sql) {
        String[] parts = sql.split(" ");
        String tableName = parts[2].toLowerCase();
        String columns;
        try {
            columns = sql.substring(sql.indexOf("(") + 1, sql.indexOf(")")).trim();
        } catch (Exception e) {
            columns = "";
        }
        return new String[]{tableName, columns};
    }

    //SELECT * FROM marks;
    //SELECT * FROM marks WHERE name != 'Sion';
    //SELECT * FROM marks WHERE name LIKE 'i';
    //SELECT a, b, c FROM marks WHERE name != 'Sion';
    private static String[] parseSelect(String sql) {
        int fromIndex = ToolUtils.indexOfIgnoreCase(sql, "FROM");
        String fields = sql.substring(7, fromIndex).trim();

        int whereIndex = ToolUtils.indexOfIgnoreCase(sql, "WHERE");
        String tableName;
        if (whereIndex != -1) {
            tableName = sql.substring(fromIndex + 5, whereIndex).trim();
        } else {
            tableName = sql.substring(fromIndex + 5).trim();
        }

        String condition = "";
        if (whereIndex != -1) {
            condition = sql.substring(whereIndex + 6).trim();
        }

        return new String[]{tableName, fields, condition};
    }

    //INSERT INTO students VALUES (1, Bob, 21);
    private static String[] parseInsert(String sql) {
        String[] parts = sql.split(" ");
        String tableName = parts[2];
        String values = sql.substring(ToolUtils.indexOfIgnoreCase(sql, "VALUES") + 6).trim();
        if (values.startsWith("(") && values.endsWith(")")) {
            values = values.substring(1, values.length() - 1);
        }
        return new String[]{tableName, values};
    }

    //UPDATE marks SET mark = 38 WHERE name == 'Chris';
    private static String[] parseUpdate(String sql) {
        int updateIndex = ToolUtils.indexOfIgnoreCase(sql, Constants.UPDATE);
        int setIndex = ToolUtils.indexOfIgnoreCase(sql, "SET");
        String tableName = sql.substring(updateIndex + 6, setIndex).trim();

        int whereIndex = ToolUtils.indexOfIgnoreCase(sql, "WHERE");
        String updates;
        if (whereIndex != -1) {
            updates = sql.substring(setIndex + 4, whereIndex).trim();
        } else {
            updates = sql.substring(setIndex + 4).trim();
        }

        String condition = "";
        if (whereIndex != -1) {
            condition = sql.substring(whereIndex + 6).trim();
        }

        return new String[]{tableName, updates, condition};
    }

    //DELETE FROM marks WHERE name == 'Sion';
    private static String[] parseDelete(String sql) {

        int tableNameIndex = ToolUtils.indexOfIgnoreCase(sql, "FROM") + 5;
        int whereIndex = ToolUtils.indexOfIgnoreCase(sql, "WHERE");
        ;

        String tableName;
        if (whereIndex != -1) {
            tableName = sql.substring(tableNameIndex, whereIndex).trim();
        } else {
            tableName = sql.substring(tableNameIndex).trim();
        }

        String condition = "";
        if (whereIndex != -1) {
            condition = sql.substring(whereIndex + 6).trim();
        }

        return new String[]{tableName, condition};
    }

    private static String[] parseDrop(String sql) {
        String[] parts = sql.split(" ");
        String tableName = parts[2];
        return new String[]{tableName};
    }

    //DROP DATABASE markbook;
    private static String[] parseDropDatabase(String sql) {
        return parseDrop(sql);
    }

    //DROP TABLE marks;
    private static String[] parseDropTable(String sql) {
        return parseDrop(sql);
    }

    private static String[] parseAlterTable(String sql) {
        String[] parts = sql.split(" ");
        String tableName = parts[2];
        String columnName = parts[4];
        return new String[]{tableName, columnName};
    }

    //ALTER TABLE marks ADD pass;
    private static String[] parseAlterTableAdd(String sql) {
        return parseAlterTable(sql);
    }

    //ALTER TABLE marks DROP pass;
    private static String[] parseAlterTableDrop(String sql) {
        return parseAlterTable(sql);
    }

    //JOIN coursework AND marks ON submission AND id;
    private static String[] parseJoin(String sql) {
        sql = sql.trim().toUpperCase().trim();
        int joinIndex = sql.indexOf(Constants.JOIN);
        int onIndex = sql.indexOf("ON");
        String tables = sql.substring(joinIndex + 5, onIndex).trim();
        String[] tableNames = tables.split(Constants.AND_OPERATOR, 2);
        String table1 = tableNames[0].trim();
        String table2 = tableNames[1].trim();

        String columns = sql.substring(onIndex + 3).trim();
        String[] columnNames = columns.split(Constants.AND_OPERATOR, 2);
        String column1 = columnNames[0].trim();
        String column2 = columnNames[1].trim();

        return new String[]{table1, table2, column1, column2};
    }

}
