package com.digitalid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AddPersonTest {

    // Creates path to store text file for viewing
    private final Path testStorage = Path.of("test_data");

    // Function to create person objects for testing
    private Person createPersonManager() {
       try {
            // Ensure folder exists before running
            if (!Files.exists(testStorage)) {
                Files.createDirectories(testStorage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Detailed error for testing
        }

        // Fixed clock so time remains static for testing DOB 
        Clock fixedClock = Clock.fixed(LocalDate.of(2026, 2, 10)
            .atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        return new Person(testStorage, fixedClock);
    }

// ========================== TEST 1: VALID ADD PERSON =========================
@Test

// Tests AddPerson can add data to text file when given valid inputs
// ERROR: If test has been run before, will fail as duplicate IDs exist
// FIX: Delete persons.txt before running OR use new ID for test

    void testAddValidPerson() {
        Person pm = createPersonManager();

        // Calls AddPerson with valid inputs and stores true/false outcome
        boolean result = pm.addPerson("56s_d%&fAB", "John", "Doe", 
        "32|Highland Street|Melbourne|Victoria|Australia", "15-11-1990");
        
        // Outcome should be TRUE
        // Throws error if outcome is false
        assertTrue(result, "AddPerson Valid Inputs Test should return TRUE");
        }

// ========================== TEST 2: WRONG STATE =========================
@Test

// Tests AddPerson can detect if input is state other than Victoria

    void testAddPersonStateMatching() {
        Person pm = createPersonManager();

        // Calls AddPerson with valid inputs except 'Queensland' for State instead of Victoria
        boolean result = pm.addPerson("56s_d%&fAB", "Jane", "Smith", 
        "32|Canterbury Street|Brisbane|Queensland|Australia", "15-11-1990");
        
        // Outcome should be FALSE
        // Throws error if outcome is true
        assertFalse(result, "AddPerson Wrong State Test should return FALSE");
    }

// ========================== TEST 3: WRONG DATE FORMAT =========================
@Test

// Tests AddPerson can detect if date input does not match dd/mm/yyyy

    void testAddPersonBirthDateFormat() {
        Person pm = createPersonManager();

        // Calls AddPerson with valid inputs expect DOB is missing dd
        boolean result = pm.addPerson("56s_d%&fAB", "Jane", "Smith",
        "32|Highland Street|Melbourne|Victoria|Australia", "11-1990");
        
        // Outcome should be FALSE
        // Throws error if outcome is TRUE
        assertFalse(result, "AddPerson Missing DD Test should return FALSE");
    }

// ========================== TEST 4: PERSONID LEADING NUMBERS =========================
@Test

// Tests AddPerson can detect if first two characters of PersonID are not numbers between 1 and 9

    void testAddPersonIDLeadingLetters() {
        Person pm = createPersonManager();

        // Calls AddPerson with valid inputs expect PersonID's first two characters are lowercase letters
        boolean result = pm.addPerson("abs_d%&fAB", "Jane", "Smith", 
        "32|Highland Street|Melbourne|Victoria|Australia", "15-11-1990");
        
        // Outcome should be FALSE
        // Throws error if outcome is TRUE
        assertFalse(result, "AddPerson Leading Letters ID Test should return FALSE");
    }

    // ========================== TEST 5: PERSONID TRAILING CAPITALISATION =========================
@Test

// Tests AddPerson can detect if last two characters of PersonID are not capital letters between A-Z

    void testAddPersonLowercaseIdEndsReturnsFalse() {
        Person pm = createPersonManager();

        // Calls AddPerson with valid inputs expect PersonID's last two characters are lowercase letters
        boolean result = pm.addPerson("56s_d%&fab", "Jane", "Smith",
         "32|Highland Street|Melbourne|Victoria|Australia", "15-11-1990");
        
        // Outcome should be FALSE
        // Throws error if outcome is TRUE
        assertFalse(result, "AddPerson Lowercase Trailing Letters Test should return FALSE");
    }
}