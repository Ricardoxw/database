package edu.uob;

import static edu.entity.Expression.compareRowValueWithConditionValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import edu.constant.ExpressionType;
import edu.entity.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.ArrayList;

public class ExampleDBTests {

    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName() {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
        "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // A basic test that creates a database, creates a table, inserts some test data, then queries it.
    // It then checks the response to see that a couple of the entries in the table are returned as expected
    @Test
    public void testBasicCreateAndQuery() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Simon"), "An attempt was made to add Simon to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Chris"), "An attempt was made to add Chris to the table, but they were not returned by SELECT *");
    }

    // A test to make sure that querying returns a valid ID (this test also implicitly checks the "==" condition)
    // (these IDs are used to create relations between tables, so it is essential that suitable IDs are being generated and returned !)
    @Test
    public void testQueryID() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        String response = sendCommandToServer("SELECT id FROM marks WHERE name == 'Simon';");
        // Convert multi-lined responses into just a single line
        String singleLine = response.replace("\n"," ").trim();
        // Split the line on the space character
        String[] tokens = singleLine.split(" ");
        // Check that the very last token is a number (which should be the ID of the entry)
        String lastToken = tokens[tokens.length-1];
        try {
            Integer.parseInt(lastToken);
        } catch (NumberFormatException nfe) {
            fail("The last token returned by `SELECT id FROM marks WHERE name == 'Simon';` should have been an integer ID, but was " + lastToken);
        }
    }

    // A test to make sure that databases can be reopened after server restart
    @Test
    public void testTablePersistsAfterRestart() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Simon"), "Simon was added to a table and the server restarted - but Simon was not returned by SELECT *");
    }

    // Test to make sure that the [ERROR] tag is returned in the case of an error (and NOT the [OK] tag)
    @Test
    public void testForErrorTag() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        String response = sendCommandToServer("SELECT * FROM libraryfines;");
        assertTrue(response.contains("[ERROR]"), "An attempt was made to access a non-existent table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An attempt was made to access a non-existent table, however an [OK] tag was returned");
    }

    @Test
    public void testExpressionParseFunction() {
        String condition1 = "a == 'bbb'";
        Expression expr1 = new Expression(condition1);
        System.out.println("Parsed Expression: " + expr1.toString());
        System.out.println("Type: " + expr1.getType());

        String condition2 = "a > 0 AND b == 'aaa'";
        Expression expr2 = new Expression(condition2);
        System.out.println("Parsed Expression: " + expr2.toString());
        System.out.println("Type: " + expr2.getType());

        String condition3 = "(a == 'aaa' AND b > 20) OR (c LIKE 'd%')";
        Expression expr3 = new Expression(condition3);
        System.out.println("Parsed Expression: " + expr3.toString());
        System.out.println("Type: " + expr3.getType());

        String condition4 = "a == true";
        Expression expr4 = new Expression(condition4);
        System.out.println("Parsed Expression: " + expr4.toString());
        System.out.println("Type: " + expr4.getType());

        String condition5 = "a == null";
        Expression expr5 = new Expression(condition5);
        System.out.println("Parsed Expression: " + expr5.toString());
        System.out.println("Type: " + expr5.getType());

        String condition6 = "a == 1";
        Expression expr6 = new Expression(condition6);
        System.out.println("Parsed Expression: " + expr6.toString());
        System.out.println("Type: " + expr6.getType());
    }

    @Test
    public void testExpressionCompareFunction() {
        ArrayList<String> columns = new ArrayList<>();
        columns.add("name");
        columns.add("age");
        columns.add("salary");

        ArrayList<String> row1 = new ArrayList<>();
        row1.add("Alice");
        row1.add("25");
        row1.add("5000");

        ArrayList<String> row2 = new ArrayList<>();
        row2.add("Bob");
        row2.add("30");
        row2.add("7000");

        Expression expr1 = new Expression("age == 25");
        System.out.println("Test 1: " + expr1.isConditionSatisfied(columns, row1)); // true
        System.out.println("Test 1: " + expr1.isConditionSatisfied(columns, row2)); // false

        Expression expr2 = new Expression("age > 25 AND salary >= 6000");
        System.out.println("Test 2: " + expr2.isConditionSatisfied(columns, row1)); // false
        System.out.println("Test 2: " + expr2.isConditionSatisfied(columns, row2)); // true

        Expression expr3 = new Expression("name LIKE 'A%'");
        System.out.println("Test 3: " + expr3.isConditionSatisfied(columns, row1)); // true
        System.out.println("Test 3: " + expr3.isConditionSatisfied(columns, row2)); // false

        Expression expr4 = new Expression("gender == 'M'");
        try {
            System.out.println("Test 4: " + expr4.isConditionSatisfied(columns, row1)); // error
        } catch (IllegalArgumentException e) {
            System.out.println("Test 4: " + e.getMessage());
        }

        Expression expr5 = new Expression("(age > 29) OR (salary < 6000)");
        try {
            System.out.println("Test 5: " + expr5.isConditionSatisfied(columns, row1)); // true
            System.out.println("Test 5: " + expr5.isConditionSatisfied(columns, row2)); // true
        } catch (IllegalArgumentException e) {
            System.out.println("Test 5: " + e.getMessage());
        }
    }

}
