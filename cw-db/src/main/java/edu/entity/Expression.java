package edu.entity;

import edu.constant.Constants;
import edu.constant.ExpressionType;
import edu.utils.ToolUtils;

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
    private static final String[] Comparators = {
            Constants.EQUAL_OPERATOR,
            Constants.GREATER_THAN_OR_EQUAL_OPERATOR,
            Constants.LESS_THAN_OR_EQUAL_OPERATOR,
            Constants.NOT_EQUAL_OPERATOR,
            Constants.GREATER_THAN_OPERATOR,
            Constants.LESS_THAN_OPERATOR,
            Constants.LIKE_OPERATOR
    };
    private static final String[] BoolOperators = {Constants.AND_OPERATOR, Constants.OR_OPERATOR};

    public Expression(String expressionStr) {
        this.expressionStr = expressionStr;
        parse(expressionStr);
    }

    public String formatSubExpressionForComparator(String str, boolean isLeft) {
        str = str.trim();
        while (str.startsWith("(") && isLeft) {
            str = str.substring(1);
        }
        while (str.endsWith(")")) {
            str = str.substring(0, str.length() - 1);
        }
        formatSubExpression(str);
        return str.trim();
    }

    public String formatSubExpression(String str) {
        str = str.trim();
        while (str.startsWith("(") && str.endsWith(")")) {
            str = str.substring(1, str.length() - 1).trim();
        }
        return str.trim();
    }

    public void parse(String conditionStr) {
        conditionStr = conditionStr.trim().replaceAll("\\s+", " ");
        // Empty condition will execute as TRUE
        if (conditionStr.isEmpty() || conditionStr.equals(" ")) {
            this.expressionStr = "";
            this.value = Constants.TRUE;
            this.type = ExpressionType.VALUE;
            return;
        }
        // Expression recursion should be parsed until the expression type is ExpressionType.VALUE or ExpressionType.COLUMN
        // 'and' 'or' condition should be recursion parsed, left and right are conditions, too.
        for (String op : BoolOperators) {
            int index = conditionStr.toUpperCase().indexOf(" " + op + " ");
            if (index != -1) {
                this.left = new Expression(formatSubExpression(conditionStr.substring(0, index).trim()));
                this.operator = op;
                this.right = new Expression(formatSubExpression(conditionStr.substring(index + op.length() + 1).trim()));
                this.type = op.equals(Constants.AND_OPERATOR) ? ExpressionType.AND_EXPRESSION : ExpressionType.OR_EXPRESSION;
                return;
            }
        }
        // >,<,==,!= should put the column in left, put the value in right
        for (String op : Comparators) {
            int index = conditionStr.indexOf(op);
            if (index != -1) {
                this.left = new Expression(formatSubExpressionForComparator(conditionStr.substring(0, index), true));
                this.operator = op;
                this.right = new Expression(formatSubExpressionForComparator(conditionStr.substring(index + op.length()), false));
                this.type = ExpressionType.COMPARISON_EXPRESSION;
                return;
            }
        }
        // Constant should be put in value
        if (conditionStr.matches("'.*'") // str
                || ToolUtils.isNumeric(conditionStr) // numeric
                || conditionStr.equalsIgnoreCase(Constants.TRUE) // boolean
                || conditionStr.equalsIgnoreCase(Constants.FALSE)
                || conditionStr.equalsIgnoreCase(Constants.NULL)) {
            this.value = conditionStr;
            this.type = ExpressionType.VALUE;
            return;
        }
        // Column name should be put in column
        if (conditionStr.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            this.column = conditionStr;
            ToolUtils.checkConditionColumnValid(this.column);
            this.type = ExpressionType.COLUMN;
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

    public boolean isConditionSatisfied(ArrayList<String> columnNames, ArrayList<String> row) {
        switch (this.type) {
            //for and and or expression, recursion the function until expression is a comparison expression or value expression
            case AND_EXPRESSION:
                return left.isConditionSatisfied(columnNames, row) && right.isConditionSatisfied(columnNames, row);
            case OR_EXPRESSION:
                return left.isConditionSatisfied(columnNames, row) || right.isConditionSatisfied(columnNames, row);
            case COMPARISON_EXPRESSION:
                int columnIndex = ToolUtils.getIndexIgnoreCase(left.getColumn(), columnNames);
                if (columnIndex == -1)
                    throw new IllegalArgumentException("Attribute does not exist");
                String rowValue = row.get(columnIndex);
                if (validateExpressionValue(right)) {
                    return compareRowValueWithConditionValue(rowValue, right.getValue(), operator);
                } else {
                    throw new IllegalArgumentException("Condition value is not valid");
                }
            case VALUE:
                return value.equals(Constants.TRUE);
        }
        return false;
    }

    // Value can only be TRUE FALSE 'aaa' 1.23124, if there is a string like a1231, it's invalid value
    public static boolean validateExpressionValue(Expression expression) {
        String expressionValue = expression.getValue();
        if (Constants.TRUE.equalsIgnoreCase(expressionValue) || Constants.FALSE.equalsIgnoreCase(expressionValue)) {
            expression.setValue(expressionValue.toUpperCase());
            return true;
        }
        try {
            Double.parseDouble(expressionValue);
            return true;
        } catch (NumberFormatException e) {
            if (expressionValue.startsWith("'") && expressionValue.endsWith("'")) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean compareRowValueWithConditionValue(String rowValue, String conditionValue, String operator) {
        rowValue = rowValue.trim();
        // validate like condition
        if (operator.equalsIgnoreCase(Constants.LIKE_OPERATOR)) {
//            return ToolUtils.checkLikeCondition(rowValue, 0, conditionValue, 0);
            return ToolUtils.like(rowValue, conditionValue);
        }

        try {
            // validate BoolOperators condition
            double rowNum = Double.parseDouble(rowValue);
            double conditionNum = Double.parseDouble(conditionValue);
            return switch (operator) {
                case Constants.EQUAL_OPERATOR -> rowNum == conditionNum;
                case Constants.GREATER_THAN_OPERATOR -> rowNum > conditionNum;
                case Constants.LESS_THAN_OPERATOR -> rowNum < conditionNum;
                case Constants.GREATER_THAN_OR_EQUAL_OPERATOR -> rowNum >= conditionNum;
                case Constants.LESS_THAN_OR_EQUAL_OPERATOR -> rowNum <= conditionNum;
                case Constants.NOT_EQUAL_OPERATOR -> rowNum != conditionNum;
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            };
        } catch (NumberFormatException e) {
            // compare string
            if (!conditionValue.equalsIgnoreCase(Constants.TRUE) && !conditionValue.equalsIgnoreCase(Constants.FALSE)) {
                rowValue = "'" + rowValue + "'";
            } else {
                return rowValue.equalsIgnoreCase(conditionValue);
            }
            // compare the TRUE and FALSE
            if (operator.equals(Constants.EQUAL_OPERATOR)) {
                return rowValue.equals(conditionValue);
            } else if (operator.equals(Constants.NOT_EQUAL_OPERATOR)) {
                return !rowValue.equals(conditionValue);
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