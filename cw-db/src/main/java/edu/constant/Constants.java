package edu.constant;

import java.util.Set;

public class Constants {
    // define some constants
    public static final String USE = "USE";
    public static final String CREATE = "CREATE";
    public static final String DROP = "DROP";
    public static final String ALTER = "ALTER";
    public static final String INSERT = "INSERT";
    public static final String SELECT = "SELECT";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String JOIN = "JOIN";

    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";
    public static final String NULL = "NULL";

    public static final String LIKE_OPERATOR = "LIKE";
    public static final String AND_OPERATOR = "AND";
    public static final String OR_OPERATOR = "OR";
    public static final String EQUAL_OPERATOR = "==";
    public static final String GREATER_THAN_OR_EQUAL_OPERATOR = ">=";
    public static final String LESS_THAN_OR_EQUAL_OPERATOR = "<=";
    public static final String NOT_EQUAL_OPERATOR = "!=";
    public static final String GREATER_THAN_OPERATOR = ">";
    public static final String LESS_THAN_OPERATOR = "<";

    public static final String SUCCESS_STATUS = "[OK]";
    public static final String FAILURE_STATUS = "[ERROR]";

    public static final Set<String> SQL_KEYWORDS = Set.of(
            "SELECT", "FROM", "WHERE", "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "ON", "AND", "OR", "NOT",
            "IN", "BETWEEN", "LIKE", "IS", "NULL", "AS", "DISTINCT", "GROUP", "BY", "HAVING", "ORDER",
            "ASC", "DESC", "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE", "CREATE", "TABLE",
            "DROP", "ALTER", "ADD", "COLUMN", "PRIMARY", "KEY", "FOREIGN", "UNIQUE", "INDEX", "CONSTRAINT",
            "DEFAULT", "CHECK", "UNION", "EXCEPT", "INTERSECT", "CASE", "WHEN", "THEN", "ELSE", "END",
            "EXISTS", "ANY", "ALL", "SOME", "CAST", "CONVERT", "DATE", "TIME", "TIMESTAMP", "CHAR", "VARCHAR",
            "INTEGER", "FLOAT", "DOUBLE", "DECIMAL", "NUMERIC", "BOOLEAN", "TRUE", "FALSE"
    );
}
