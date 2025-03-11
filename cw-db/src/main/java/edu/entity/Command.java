package edu.entity;

import edu.constant.CommandType;
import edu.constant.Constants;
import edu.uob.DBServer;
import edu.utils.Executor;


public class Command {
    private String sql;
    private CommandType type;

    // parse the sql type and make sure that the sql with semicolon
    public Command(String content) {
        String trimmedSQL = content.trim().replaceAll("\\s+", " ");

        if (!trimmedSQL.endsWith(";")) {
            throw new IllegalArgumentException("Semi colon missing at end of line");
        }

        this.sql = trimmedSQL.substring(0, trimmedSQL.length() - 1);
        this.type = getCommandType(this.sql);
    }

    public String execute(DBServer dbServer) throws Exception {
        return switch (type) {
            case USE -> Executor.use(sql, dbServer);
            case CREATE_DATABASE -> Executor.createDatabase(sql, dbServer);
            case CREATE_TABLE -> Executor.createTable(sql, dbServer);
            case INSERT -> Executor.insert(sql, dbServer);
            case SELECT -> Executor.select(sql, dbServer);
            case UPDATE -> Executor.update(sql, dbServer);
            case DELETE -> Executor.delete(sql, dbServer);
            case DROP_DATABASE -> Executor.dropDatabase(sql, dbServer);
            case DROP_TABLE -> Executor.dropTable(sql, dbServer);
            case ALTER_TABLE_ADD -> Executor.alterTableAdd(sql, dbServer);
            case ALTER_TABLE_DROP -> Executor.alterTableDrop(sql, dbServer);
            case JOIN -> Executor.join(sql, dbServer);
            default -> throw new IllegalArgumentException("Unknown command type: " + type);
        };
    }

    private CommandType getCommandType(String sql) throws IllegalArgumentException {
        String type = sql.split(" ")[0].toUpperCase();
        String upperSql = sql.toUpperCase();
        switch (type) {
            case Constants.USE:
                return CommandType.USE;
            case Constants.CREATE:
                if (upperSql.contains("DATABASE")) {
                    return CommandType.CREATE_DATABASE;
                } else if (upperSql.contains("TABLE")) {
                    return CommandType.CREATE_TABLE;
                }
                break;
            case Constants.DROP:
                if (upperSql.contains("DATABASE")) {
                    return CommandType.DROP_DATABASE;
                } else if (upperSql.contains("TABLE")) {
                    return CommandType.DROP_TABLE;
                }
                break;
            case Constants.ALTER:
                if (upperSql.contains("ADD")) {
                    return CommandType.ALTER_TABLE_ADD;
                } else if (upperSql.contains("DROP")) {
                    return CommandType.ALTER_TABLE_DROP;
                }
                break;
            case Constants.INSERT:
                return CommandType.INSERT;
            case Constants.SELECT:
                return CommandType.SELECT;
            case Constants.UPDATE:
                return CommandType.UPDATE;
            case Constants.DELETE:
                return CommandType.DELETE;
            case Constants.JOIN:
                return CommandType.JOIN;
            default:
                throw new IllegalArgumentException("Unknown SQL command: " + sql);
        }
        return null;
    }
}
