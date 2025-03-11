package edu.constant;

public enum ExpressionType {
    AND_EXPRESSION, // condition and condition
    OR_EXPRESSION, // condition or condition
    COMPARISON_EXPRESSION, //a == 1
    COLUMN, // column name
    VALUE //value like 1, TRUE, 'aaa'
}
