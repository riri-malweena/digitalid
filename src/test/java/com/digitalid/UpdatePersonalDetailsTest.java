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

    private final Path testStorage = Path.of("test_data");


    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final LocalDate TODAY = LocalDate.of(2026, 2, 18);

    private Person personWithFixedToday() {
        Clock fixed = Clock.fixed(TODAY.atStartOfDay(ZONE).toInstant(), ZONE);
        return new Person(testStorage, fixed);
    }

    private Path personsFile() {
        return testStorage.resolve("persons.txt");
    }

    private void writePersons(String... lines) throws Exception {
        Files.write(personsFile(), List.of(lines), StandardCharsets.UTF_8);
    }

    // Use valid IDs that satisfy your addPerson ID rule (so update validation won't fail for wrong reason)
    private static final String PERSON_ID_ODD = "56s_d%&fAB";   // odd first digit
    private static final String PERSON_ID_EVEN = "26s_d%&fAB";  // even first digit

    // Use valid address format
    private static final String ADDR_1 = "32|Highland Street|Melbourne|Victoria|Australia";
    private static final String ADDR_2 = "99|King Street|Melbourne|Victoria|Australia";

    // file format:
    // personId|firstName|lastName|streetNumber|street|city|state|country|birthDate|demeritPoints|isSuspended
    private String line(String id, String first, String last, String address,
                        String dob, String demerit, String suspended) {
        String[] a = address.split("\\|", -1);
        return String.join("|",
                id, first, last,
                a[0], a[1], a[2], a[3], a[4],
                dob, demerit, suspended
        );
    }

    @Test
    void testUpdatePersonalDetailsFunction() throws Exception {

        Person p = personWithFixedToday();

        // =========================================================
        // Test Case 1: Update name for person over 18 years old
        // Test Data: PersonID=56s_d%&fAB, DOB=15-11-2000,
        //           NewFirstName=Mila, NewLastName=Tran,
        //           Address unchanged, Birthday unchanged
        // Expected: returns true
        // =========================================================
        writePersons(line(PERSON_ID_ODD, "Amy", "Lee", ADDR_1, "15-11-2000", "7", "false"));

        boolean tc1 = p.updatePersonalDetails(
                PERSON_ID_ODD,      // existing personId
                PERSON_ID_ODD,      // ID unchanged
                "Mila",             // NewFirstName
                "Tran",             // NewLastName
                ADDR_1,             // Address unchanged
                "15-11-2000"        // Birthday unchanged
        );

        assertEquals(true, tc1);

        // =========================================================
        // Test Case 2: Attempt to change address for person under 18
        // Test Data: PersonID=56s_d%&fAB, DOB=15-11-2010,
        //           NewAddress=99|King Street|Melbourne|Victoria|Australia
        // Expected: returns false
        // =========================================================
        writePersons(line(PERSON_ID_ODD, "Sam", "Young", ADDR_1, "15-11-2010", "0", "false"));

        boolean tc2 = p.updatePersonalDetails(
                PERSON_ID_ODD,
                PERSON_ID_ODD,
                "Sam",
                "Young",
                ADDR_2,          // NewAddress (attempted change)
                "15-11-2010"
        );

        assertEquals(false, tc2);

        // =========================================================
        // Test Case 3: Change birthday only
        // Test Data: PersonID=56s_d%&fAB, OldDOB=15-11-2000, NewDOB=15-11-2001,
        //           No other fields changed
        // Expected: returns true
        // =========================================================
        writePersons(line(PERSON_ID_ODD, "Nina", "Park", ADDR_1, "15-11-2000", "3", "true"));

        boolean tc3 = p.updatePersonalDetails(
                PERSON_ID_ODD,
                PERSON_ID_ODD,
                "Nina",
                "Park",
                ADDR_1,
                "15-11-2001"     // NewDOB
        );

        assertEquals(true, tc3);

        // =========================================================
        // Test Case 4: Attempt to change birthday and address together
        // Test Data: PersonID=56s_d%&fAB, NewDOB=15-11-2001, NewAddress=99|King Street|...
        // Expected: returns false
        // =========================================================
        writePersons(line(PERSON_ID_ODD, "Nina", "Park", ADDR_1, "15-11-2000", "3", "true"));

        boolean tc4 = p.updatePersonalDetails(
                PERSON_ID_ODD,
                PERSON_ID_ODD,
                "Nina",
                "Park",
                ADDR_2,          // NewAddress
                "15-11-2001"     // NewDOB
        );

        assertEquals(false, tc4);

        // =========================================================
        // Test Case 5: Attempt to change ID when first digit is even
        // Test Data: Existing PersonID=26s_d%&fAB, NewPersonID=56s_d%&fAB
        // Expected: returns false
        // =========================================================
        writePersons(line(PERSON_ID_EVEN, "John", "Even", ADDR_1, "15-11-2000", "1", "false"));

        boolean tc5 = p.updatePersonalDetails(
                PERSON_ID_EVEN,   // Existing PersonID (even first digit)
                PERSON_ID_ODD,    // NewPersonID attempt
                "John",
                "Even",
                ADDR_1,
                "15-11-2000"
        );

        assertEquals(false, tc5);
    }
}
