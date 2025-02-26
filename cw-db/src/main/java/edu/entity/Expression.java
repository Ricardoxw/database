package edu.entity;

import edu.constant.ExpressionType;

import java.util.ArrayList;

// Expression parser can only parse the non-nested expression
public class Expression {
    private String expressionStr;
    private Expression left;
    private Expression right;
    private String operator;
    private ExpressionType type;
    private String value;
    private String column;
    private static final String[] Comparators = {"==", ">=", "<=", "!=", ">", "<", "LIKE"};
    private static final String[] BoolOperators = {"OR", "AND"};

    public Expression(String expressionStr) {
        this.expressionStr = expressionStr;
        parse(expressionStr);
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String formatSubExpression(String str) {
        while (str.startsWith("(") && str.endsWith(")")) {
            str = str.substring(1, str.length() - 1).trim();
        }
        return str;
    }

    public void parse(String conditionStr) {
        conditionStr = conditionStr.trim().replaceAll("\\s+", " ");

        for (String op : BoolOperators) {
            int index = conditionStr.toUpperCase().indexOf(" " + op + " ");
            if (index != -1) {
                this.left = new Expression(formatSubExpression(conditionStr.substring(0, index).trim()));
                this.operator = op;
                this.right = new Expression(formatSubExpression(conditionStr.substring(index + op.length() + 1).trim()));
                this.type = op.equals("AND") ? ExpressionType.AND_EXPRESSION : ExpressionType.OR_EXPRESSION;
                return;
            }
        }

        for (String op : Comparators) {
            int index = conditionStr.indexOf(op);
            if (index != -1) {
                this.left = new Expression(conditionStr.substring(0, index).trim());
                this.operator = op;
                this.right = new Expression(conditionStr.substring(index + op.length()).trim());
                this.type = ExpressionType.COMPARISON_EXPRESSION;
                return;
            }
        }

        if (conditionStr.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            this.column = conditionStr;
            this.type = ExpressionType.COLUMN;
            return;
        }

        if (conditionStr.matches("'.*'") // str
                || isNumeric(conditionStr) // numeric
                || conditionStr.equalsIgnoreCase("true") // boolean
                || conditionStr.equalsIgnoreCase("false")
                || conditionStr.equalsIgnoreCase("null")) {
            this.value = conditionStr;
            this.type = ExpressionType.VALUE;
            return;
        }

        throw new IllegalArgumentException("Invalid condition format: " + conditionStr);
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public ExpressionType getType() {
        return type;
    }

    public void setType(ExpressionType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (type == ExpressionType.AND_EXPRESSION || type == ExpressionType.OR_EXPRESSION) {
            sb.append("\n").append(expressionStr).append("\n")
                    .append("  type=").append(type).append("\n");
        } else if (type == ExpressionType.COMPARISON_EXPRESSION) {
            sb.append("\n").append(expressionStr).append("\n")
                    .append("  left=").append(left)
                    .append("  right=").append(right)
                    .append("  operator='").append(operator).append("'\n");
        } else if (type == ExpressionType.VALUE || type == ExpressionType.COLUMN) {
            sb.append("\n").append(expressionStr).append("\n");
        }
        return sb.toString();
    }

    public boolean isCondition() {
        return type.equals(ExpressionType.AND_EXPRESSION) || type.equals(ExpressionType.OR_EXPRESSION) || type.equals(ExpressionType.COMPARISON_EXPRESSION);
    }

    public boolean isConditionSatisfied(ArrayList<String> columns, ArrayList<String> row) {
        switch (this.type) {
            case AND_EXPRESSION:
                return left.isConditionSatisfied(columns, row) && right.isConditionSatisfied(columns, row);
            case OR_EXPRESSION:
                return left.isConditionSatisfied(columns, row) || right.isConditionSatisfied(columns, row);
            case COMPARISON_EXPRESSION:
                int columnIndex = columns.indexOf(left.getColumn());
                if (columnIndex == -1)
                    throw new IllegalArgumentException("Column not found: " + left.getColumn());
                String rowValue = row.get(columnIndex);
                return compareRowValueWithConditionValue(rowValue, right.getValue(), operator);
        }
        return false;
    }

    private static boolean compareRowValueWithConditionValue(String rowValue, String conditionValue, String operator) {
        // @todo there is some error in execute like compare
        if (operator.equalsIgnoreCase("LIKE")) {
            conditionValue = conditionValue.replaceAll("([.^$*+?(){}\\[\\]|])", "\\\\$1");
            conditionValue = conditionValue.replace("%", ".*").replace("_", ".");
            return rowValue.matches(conditionValue);
        }

        try {
            double rowNum = Double.parseDouble(rowValue);
            double conditionNum = Double.parseDouble(conditionValue);
            return switch (operator) {
                case "==" -> rowNum == conditionNum;
                case ">" -> rowNum > conditionNum;
                case "<" -> rowNum < conditionNum;
                case ">=" -> rowNum >= conditionNum;
                case "<=" -> rowNum <= conditionNum;
                case "!=" -> rowNum != conditionNum;
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            };
        } catch (NumberFormatException e) {
            if (operator.equals("==")) {
                return rowValue.equals(conditionValue);
            }
            throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}