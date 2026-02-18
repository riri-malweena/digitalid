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

public class UpdatePersonalDetailsTest {

    @TempDir
    Path tempDir;

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final LocalDate TODAY = LocalDate.of(2026, 2, 18);

    private Person personWithFixedToday() {
        Clock fixed = Clock.fixed(TODAY.atStartOfDay(ZONE).toInstant(), ZONE);
        return new Person(tempDir, fixed);
    }

    private Path personsFile() {
        return tempDir.resolve("persons.txt");
    }

    private void writePersons(String... lines) throws Exception {
        Files.write(personsFile(), List.of(lines), StandardCharsets.UTF_8);
    }

    // Helper: valid IDs for your rules
    private static final String VALID_ID_ODD_FIRST = "56s_d%&fAB";  // first digit 5 (odd)
    private static final String VALID_ID_EVEN_FIRST = "26s_d%&fAB"; // first digit 2 (even)

    private static final String ADDR_1 = "32|Highland Street|Melbourne|Victoria|Australia";
    private static final String ADDR_2 = "99|King Street|Melbourne|Victoria|Australia";

    // file format:
    // personId|firstName|lastName|streetNumber|street|city|state|country|birthDate|demeritPoints|isSuspended
    private String line(String id, String first, String last, String address, String dob, String demerit, String suspended) {
        String[] a = address.split("\\|", -1);
        return String.join("|",
                id, first, last,
                a[0], a[1], a[2], a[3], a[4],
                dob, demerit, suspended
        );
    }

    @Test
    void update_validAdult_allowsChange_returnsTrue_andKeepsDemeritAndSuspension() throws Exception {
        // Arrange: adult, odd-first-digit ID, so updates allowed
        writePersons(line(VALID_ID_ODD_FIRST, "Amy", "Lee", ADDR_1, "15-11-1990", "7", "false"));
        Person p = personWithFixedToday();

        // Act
        boolean result = p.updatePersonalDetails(
                VALID_ID_ODD_FIRST,
                VALID_ID_ODD_FIRST,              // ID unchanged
                "Alicia",
                "Brown",
                ADDR_2,                           // address changed
                "15-11-1990"                      // DOB unchanged
        );

        // Assert
        assertEquals(true, result);

        List<String> after = Files.readAllLines(personsFile(), StandardCharsets.UTF_8);
        assertEquals(1, after.size());
        assertEquals(
                line(VALID_ID_ODD_FIRST, "Alicia", "Brown", ADDR_2, "15-11-1990", "7", "false"),
                after.get(0)
        );
    }

    @Test
    void update_under18_cannotChangeAddress_returnsFalse_andFileUnchanged() throws Exception {
        // Arrange: under 18 on 2026-02-18 (DOB 01-03-2010 -> 15)
        writePersons(line(VALID_ID_ODD_FIRST, "Sam", "Young", ADDR_1, "01-03-2010", "0", "false"));
        Person p = personWithFixedToday();

        // Act: try changing address
        boolean result = p.updatePersonalDetails(
                VALID_ID_ODD_FIRST,
                VALID_ID_ODD_FIRST,
                "Sam",
                "Young",
                ADDR_2,            // change address -> should fail
                "01-03-2010"
        );

        // Assert
        assertEquals(false, result);

        List<String> after = Files.readAllLines(personsFile(), StandardCharsets.UTF_8);
        assertEquals(1, after.size());
        assertEquals(
                line(VALID_ID_ODD_FIRST, "Sam", "Young", ADDR_1, "01-03-2010", "0", "false"),
                after.get(0)
        );
    }

    @Test
    void update_changeDob_onlyDobCanChange_returnsTrue_whenOthersSame() throws Exception {
        // Arrange
        writePersons(line(VALID_ID_ODD_FIRST, "Nina", "Park", ADDR_1, "15-11-1990", "3", "true"));
        Person p = personWithFixedToday();

        // Act: DOB changed, all other fields identical
        boolean result = p.updatePersonalDetails(
                VALID_ID_ODD_FIRST,
                VALID_ID_ODD_FIRST,
                "Nina",
                "Park",
                ADDR_1,
                "16-11-1990" // new DOB
        );

        // Assert
        assertEquals(true, result);

        List<String> after = Files.readAllLines(personsFile(), StandardCharsets.UTF_8);
        assertEquals(
                line(VALID_ID_ODD_FIRST, "Nina", "Park", ADDR_1, "16-11-1990", "3", "true"),
                after.get(0)
        );
    }

    @Test
    void update_changeDob_andAlsoChangeName_returnsFalse() throws Exception {
        // Arrange
        writePersons(line(VALID_ID_ODD_FIRST, "Nina", "Park", ADDR_1, "15-11-1990", "3", "true"));
        Person p = personWithFixedToday();

        // Act: DOB changed AND name changed -> should fail (Condition 2)
        boolean result = p.updatePersonalDetails(
                VALID_ID_ODD_FIRST,
                VALID_ID_ODD_FIRST,
                "NinaChanged",     // not allowed if DOB changes
                "Park",
                ADDR_1,
                "16-11-1990"
        );

        // Assert
        assertEquals(false, result);
    }

    @Test
    void update_evenFirstDigit_cannotChangeId_returnsFalse() throws Exception {
        // Arrange: current ID starts with even digit (2) -> cannot change ID
        writePersons(line(VALID_ID_EVEN_FIRST, "John", "Even", ADDR_1, "15-11-1990", "1", "false"));
        Person p = personWithFixedToday();

        // Act: try changing ID
        boolean result = p.updatePersonalDetails(
                VALID_ID_EVEN_FIRST,
                VALID_ID_ODD_FIRST,   // attempt new ID -> should fail (Condition 3)
                "John",
                "Even",
                ADDR_1,
                "15-11-1990"
        );

        // Assert
        assertEquals(false, result);
    }
}