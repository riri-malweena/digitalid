package com.digitalid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 5 key tests for AddID (rubric-focused).
 */
class AddIDTest {

    @TempDir
    Path tempDir;

    private AddID serviceWithToday(LocalDate today) {
        Clock fixed = Clock.fixed(
                today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        return new AddID(tempDir, fixed);
    }

    private static final LocalDate TODAY = LocalDate.of(2026, 2, 17);

    @Test
    void passport_valid_returnsTrue() {
        AddID s = serviceWithToday(TODAY);
        assertTrue(s.addID(
                "P001",
                LocalDate.of(2000, 1, 1),
                IDType.PASSPORT,
                "AB123456",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        ));
    }

    @Test
    void driversLicence_invalidFormat_returnsFalse() {
        AddID s = serviceWithToday(TODAY);
        // Should be exactly 10 chars: 2 letters + 8 digits
        assertFalse(s.addID(
                "P002",
                LocalDate.of(1999, 5, 5),
                IDType.DRIVER_LICENSE,
                "CD123", // invalid
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2029, 1, 1),
                "Australia"
        ));
    }

    @Test
    void medicare_invalidFormat_returnsFalse() {
        AddID s = serviceWithToday(TODAY);
        // Should be exactly 9 digits
        assertFalse(s.addID(
                "P003",
                LocalDate.of(1980, 3, 3),
                IDType.MEDICARE,
                "12345A789", // invalid
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        ));
    }

    @Test
    void under18_onlyStudentCardAllowed_nonStudentReturnsFalse() {
        AddID s = serviceWithToday(TODAY);
        // Under 18 on TODAY -> only STUDENT_CARD allowed
        assertFalse(s.addID(
                "P004",
                LocalDate.of(2010, 3, 1),
                IDType.PASSPORT,
                "AB123456",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        ));
    }

    @Test
    void duplicateIdNumber_returnsFalse() {
        AddID s = serviceWithToday(TODAY);

        assertTrue(s.addID(
                "P005",
                LocalDate.of(2000, 1, 1),
                IDType.PASSPORT,
                "EF123456",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        ));

        // Same idNumber again -> should be rejected
        assertFalse(s.addID(
                "P006",
                LocalDate.of(1990, 1, 1),
                IDType.DRIVER_LICENSE,
                "EF123456",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2029, 1, 1),
                "Australia"
        ));
    }
}
