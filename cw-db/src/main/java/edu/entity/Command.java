package edu.entity;

import edu.constant.CommandType;
import edu.uob.DBServer;
import edu.utils.Executor;


public class Command {
    private String sql;
    private CommandType type;

    public Command(String content) {
        String trimmedSQL = content.trim().replaceAll("\\s+", " ");
        this.type = getCommandType(sql);
        this.sql = trimmedSQL;
    }

    public String execute() {
        return switch (type) {
            case USE -> Executor.use(sql);
            case CREATE_DATABASE -> Executor.createDatabase(sql);
            case CREATE_TABLE -> Executor.createTable(sql);
            case INSERT -> Executor.insert(sql);
            case SELECT -> Executor.select(sql);
            case UPDATE -> Executor.update(sql);
            case DELETE -> Executor.delete(sql);
            case DROP_DATABASE -> Executor.dropDatabase(sql);
            case DROP_TABLE -> Executor.dropTable(sql);
            case ALTER_TABLE_ADD -> Executor.alterTableAdd(sql);
            case ALTER_TABLE_DROP -> Executor.alterTableDrop(sql);
            case JOIN -> Executor.join(sql);
            default -> "[ERROR] Unknown command type: " + type;
        };
    }

    private CommandType getCommandType(String sql) throws IllegalArgumentException {
        String type = sql.split(" ")[0].toUpperCase();
        String upperSql = sql.toUpperCase();
        switch (type) {
            case "USE":
                return CommandType.USE;
            case "CREATE":
                if (upperSql.contains("DATABASE")) {
                    return CommandType.CREATE_DATABASE;
                } else if (upperSql.contains("TABLE")) {
                    return CommandType.CREATE_TABLE;
                }
                break;
            case "DROP":
                if (upperSql.contains("DATABASE")) {
                    return CommandType.DROP_DATABASE;
                } else if (upperSql.contains("TABLE")) {
                    return CommandType.DROP_TABLE;
                }
                break;
            case "ALTER":
                if (upperSql.contains("ADD")) {
                    return CommandType.ALTER_TABLE_ADD;
                } else if (upperSql.contains("DROP")) {
                    return CommandType.ALTER_TABLE_DROP;
                }
                break;
            case "INSERT":
                return CommandType.INSERT;
            case "SELECT":
                return CommandType.SELECT;
            case "UPDATE":
                return CommandType.UPDATE;
            case "DELETE":
                return CommandType.DELETE;
            case "JOIN":
                return CommandType.JOIN;
            default:
                throw new IllegalArgumentException("Unknown SQL command: " + sql);
        }
        return null;
    }
}
