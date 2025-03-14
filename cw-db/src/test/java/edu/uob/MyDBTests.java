package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MyDBTests {

    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName() {
        String randomName = "";
        for (int i = 0; i < 10; i++) randomName += (char) (97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
                    return server.handleCommand(command);
                },
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void testCreateDatabaseAndTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        sendCommandToServer("DROP DATABASE " + randomName + ";");
    }

    public void useDatabase(String randomName) {
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
    }

    public void createMarkbookTable() {
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
    }

    @Test
    public void testInsertAndSelect() {
        String randomName = generateRandomName();
        useDatabase(randomName);
        createMarkbookTable();
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Simon"), "An attempt was made to add Simon to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Chris"), "An attempt was made to add Chris to the table, but they were not returned by SELECT *");
        sendCommandToServer("DROP DATABASE " + randomName + ";");
    }

    @Test
    public void testConditionalSelect() {
        String randomName = generateRandomName();
        useDatabase(randomName);
        createMarkbookTable();
        String response = sendCommandToServer("SELECT * FROM marks WHERE pass == TRUE;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Simon"), "Expected Simon to be returned by the conditional SELECT query");
        assertTrue(response.contains("Sion"), "Expected Sion to be returned by the conditional SELECT query");
        assertFalse(response.contains("Rob"), "Expected Rob NOT to be returned by the conditional SELECT query");
        assertFalse(response.contains("Chris"), "Expected Chris NOT to be returned by the conditional SELECT query");
        sendCommandToServer("DROP DATABASE " + randomName + ";");
    }

    @Test
    public void testJoinOperation() {
        String randomName = generateRandomName();
        useDatabase(randomName);
        createMarkbookTable();
        sendCommandToServer("CREATE TABLE coursework (name, task);");
        sendCommandToServer("INSERT INTO coursework VALUES ('Simon', 'OXO');");
        sendCommandToServer("INSERT INTO coursework VALUES ('Rob', 'DB');");
        String response = sendCommandToServer("JOIN coursework AND marks ON name AND name;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("marks.mark"), "Expected marks.mark to be returned by the JOIN query");
        assertTrue(response.contains("coursework.task"), "Expected coursework.task to be returned by the JOIN query");
        assertTrue(response.contains("DB"), "Expected Simon to be returned by the JOIN query");
        assertTrue(response.contains("OXO"), "Expected OXO to be returned by the JOIN query");
    }

    @Test
    public void testUpdateAndDelete() {
        String randomName = generateRandomName();
        useDatabase(randomName);
        createMarkbookTable();
        sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'Chris';");
        String response = sendCommandToServer("SELECT * FROM marks WHERE name == 'Chris';");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("38"), "Expected Chris's mark to be updated to 38");
        sendCommandToServer("DELETE FROM marks WHERE name == 'Chris';");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertFalse(response.contains("Chris"), "Expected Chris to be deleted from the table");
        sendCommandToServer("DROP DATABASE " + randomName + ";");
    }

    @Test
    public void testErrorHandling() {
        String randomName = generateRandomName();
        useDatabase(randomName);
        createMarkbookTable();
        String response = sendCommandToServer("SELECT * FROM crew;");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made, but no [ERROR] tag was returned");
        response = sendCommandToServer("SELECT height FROM marks WHERE name == 'Chris';");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made, but no [ERROR] tag was returned");
        sendCommandToServer("DROP DATABASE " + randomName + ";");
    }

    @Test
    public void testAlterTable() {
        String randomName = generateRandomName();
        useDatabase(randomName);
        createMarkbookTable();
        sendCommandToServer("ALTER TABLE marks ADD age;");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("age"), "Expected 'age' column to be added to the table");
        sendCommandToServer("UPDATE marks SET age = 35 WHERE name == 'Simon';");
        response = sendCommandToServer("SELECT * FROM marks WHERE name == 'Simon';");
        assertTrue(response.contains("35"), "Expected Simon's age to be set to 35");
        sendCommandToServer("ALTER TABLE marks DROP pass;");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertFalse(response.contains("pass"), "Expected 'pass' column to be dropped from the table");
        sendCommandToServer("DROP DATABASE " + randomName + ";");
    }

}
