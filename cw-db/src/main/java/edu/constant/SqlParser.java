package edu.constant;


public class SqlParser {

    public static String[] parseSQL(CommandType type, String sql) {
        switch (type) {
            case USE:
                return parseUse(sql);
            case CREATE_DATABASE:
                return parseCreateDatabase(sql);
            case CREATE_TABLE:
                return parseCreateTable(sql);
            case INSERT:
                return parseInsert(sql);
            case SELECT:
                return parseSelect(sql);
            case UPDATE:
                return parseUpdate(sql);
            case DELETE:
                return parseDelete(sql);
            case DROP_DATABASE:
                return parseDropDatabase(sql);
            case DROP_TABLE:
                return parseDropTable(sql);
            case ALTER_TABLE_ADD:
                return parseAlterTableAdd(sql);
            case ALTER_TABLE_DROP:
                return parseAlterTableDrop(sql);
            case JOIN:
                return parseJoin(sql);
            default:
                throw new IllegalArgumentException("[ERROR] Unknown SQL command type: " + type);
        }
    }

    //USE markbook;
    private static String[] parseUse(String sql) {
        String[] parts = sql.split(" ");
        String dbName = parts[1].replaceAll(";", "");
        return new String[]{dbName};
    }

    //CREATE DATABASE markbook;
    private static String[] parseCreateDatabase(String sql) {
        String[] parts = sql.split(" ");
        String dbName = parts[2].replaceAll(";", "");
        return new String[]{dbName};
    }

    //CREATE TABLE students (id, name, age);
    private static String[] parseCreateTable(String sql) {
        String[] parts = sql.split(" ");
        String tableName = parts[2];
        String columns = sql.substring(sql.indexOf("(") + 1, sql.indexOf(")")).trim();
        return new String[]{tableName, columns};
    }

    //SELECT * FROM marks;
    //SELECT * FROM marks WHERE name != 'Sion';
    //SELECT * FROM marks WHERE name LIKE 'i';
    //SELECT a, b, c FROM marks WHERE name != 'Sion';
    private static String[] parseSelect(String sql) {
        String[] parts = sql.split(" ");
        String fields = parts[1];
        String tableName = "";
        String condition = "";
        for (int i = 2; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("FROM")) {
                tableName = parts[i + 1];
            } else if (parts[i].equalsIgnoreCase("WHERE")) {
                condition = String.join(" ", parts).substring(parts[i].length()).trim();
                break;
            }
        }
        return new String[]{tableName, fields, condition};
    }

    //INSERT INTO students VALUES (1, Bob, 21);
    private static String[] parseInsert(String sql) {
        String[] parts = sql.split(" ");
        String tableName = parts[2];
        String values = sql.substring(sql.indexOf("VALUES") + 6).trim().replaceAll(";", "").trim();
        return new String[]{tableName, values};
    }

    //UPDATE marks SET mark = 38 WHERE name == 'Chris';
    private static String[] parseUpdate(String sql) {
        String[] parts = sql.split(" ");
        String tableName = parts[1];
        String updates = "";
        String condition = "";
        for (int i = 2; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("SET")) {
                updates = parts[i + 1];
            } else if (parts[i].equalsIgnoreCase("WHERE")) {
                condition = String.join(" ", parts).substring(parts[i].length()).trim();
                break;
            }
        }
        return new String[]{tableName, updates, condition};
    }

    //DELETE FROM marks WHERE name == 'Sion';
    private static String[] parseDelete(String sql) {
        String[] parts = sql.split(" ");
        String tableName = parts[2];
        String condition = "";
        for (int i = 3; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("WHERE")) {
                condition = String.join(" ", parts).substring(parts[i].length()).trim();
                break;
            }
        }
        return new String[]{tableName, condition};
    }

    private static String[] parseDrop(String sql) {
        String[] parts = sql.split(" ");
        String tableName = parts[2].replaceAll(";", "");
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
        String columnName = parts[4].replaceAll(";", "");
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
        String[] parts = sql.split(" ");
        String table1 = parts[1];
        String table2 = parts[3];
        String column1 = parts[5];
        String column2 = parts[7].replaceAll(";", "");
        return new String[]{table1, table2, column1, column2};
    }


}
