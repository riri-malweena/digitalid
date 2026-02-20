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

@TempDir
    Path tempDir;

    //create person manager w/ recent date (for DOB test)
    //fixedClock/static so test always returns same value

    private Person createPersonManager() {
        Clock fixedClock = Clock.fixed(
            LocalDate.of(2026, 2, 10).atStartOfDay(ZoneId.systemDefault()).toInstant(), 
            ZoneId.systemDefault()
        );
        return new Person(tempDir, fixedClock);
    }

    @Test

    //valid addPerson
    //should return true
    //should add to text file

    void testAddValidPerson() {
        Person pm = createPersonManager();

        //valid inputs as per examples to ensure correctness
        boolean result = pm.addPerson("56s_d%&fAB", "John", "Doe", 
        "32|Highland Street|Melbourne|Victoria|Australia", "15-11-1990");
        
        assertTrue(result, "AddPerson Valid Inputs Test should return TRUE");
        }

    //invalid, state == queensland (not victoria)
    //should return false
    //should have no change to text file
    @Test
    void testAddPersonInQueenslandReturnsFalse() {
        Person pm = createPersonManager();

        boolean result = pm.addPerson("56s_d%&fAB", "Jane", "Smith", 
        "32|Canterbury Street|Brisbane|Queensland|Australia", "15-11-1990");
        
        assertFalse(result, "AddPerson Wrong State Test should return FALSE");
    }

    //invalid, no dd
    //should return false
    //should have no change to text file
    @Test
    void testAddPersonInvalidBirthDateFormatReturnsFalse() {
        Person pm = createPersonManager();

        boolean result = pm.addPerson("56s_d%&fAB", "Jane", "Smith",
        "32|Highland Street|Melbourne|Victoria|Australia", "11-1990");
        
        assertFalse(result, "AddPerson Missing DD Test should return FALSE");
    }

    //invalid, letters for first 2 chars of id
    //should return false
    //should have no change to text file
    @Test
    void testAddPersonInvalidIdStartReturnsFalse() {
        Person pm = createPersonManager();

        boolean result = pm.addPerson("abs_d%&fAB", "Jane", "Smith", 
        "32|Highland Street|Melbourne|Victoria|Australia", "15-11-1990");
        
        assertFalse(result, "AddPerson Leading Letters ID Test should return FALSE");
    }

    //invalid, last two letters lowercase
    //should return false
    //should have no change to text file
    @Test
    void testAddPersonLowercaseIdEndsReturnsFalse() {
        Person pm = createPersonManager();

        boolean result = pm.addPerson("56s_d%&fab", "Jane", "Smith",
         "32|Highland Street|Melbourne|Victoria|Australia", "15-11-1990");
        
        assertFalse(result, "AddPerson Lowercase Trailing Letters Test should return FALSE");
    }
}