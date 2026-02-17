package com.digitalid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class AddIDTest {

    @TempDir
    Path tempDir;

    private static final LocalDate TODAY = LocalDate.of(2026, 2, 17);

    private AddID serviceWithToday(LocalDate today) {
        Clock fixed = Clock.fixed(
                today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        return new AddID(tempDir, fixed);
    }

    @Test
    void passport_valid_returnsTrue() {
        AddID s = serviceWithToday(TODAY);

        boolean result = s.addID(
                "P001",
                LocalDate.of(2000, 1, 1),
                IDType.PASSPORT,
                "AB123456",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        );

        System.out.println("passport_valid_returnsTrue => " + result);
        assertTrue(result);
    }

    @Test
    void driversLicence_invalidFormat_returnsFalse() {
        AddID s = serviceWithToday(TODAY);

        boolean result = s.addID(
                "P002",
                LocalDate.of(1999, 5, 5),
                IDType.DRIVER_LICENSE,
                "CD123", // invalid
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2029, 1, 1),
                "Australia"
        );

        System.out.println("driversLicence_invalidFormat_returnsFalse => " + result);
        assertFalse(result);
    }

    @Test
    void medicare_invalidFormat_returnsFalse() {
        AddID s = serviceWithToday(TODAY);

        boolean result = s.addID(
                "P003",
                LocalDate.of(1980, 3, 3),
                IDType.MEDICARE,
                "12345A789", // invalid
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        );

        System.out.println("medicare_invalidFormat_returnsFalse => " + result);
        assertFalse(result);
    }

    @Test
    void under18_nonStudent_returnsFalse() {
        AddID s = serviceWithToday(TODAY);

        boolean result = s.addID(
                "P004",
                LocalDate.of(2010, 3, 1), // under 18
                IDType.PASSPORT,
                "AB123456",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        );

        System.out.println("under18_nonStudent_returnsFalse => " + result);
        assertFalse(result);
    }

    @Test
    void duplicateIdNumber_returnsFalseSecondTime() {
        AddID s = serviceWithToday(TODAY);

        boolean first = s.addID(
                "P005",
                LocalDate.of(2000, 1, 1),
                IDType.PASSPORT,
                "EF123456",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        );

        boolean second = s.addID(
                "P006",
                LocalDate.of(1990, 1, 1),
                IDType.DRIVER_LICENSE,
                "EF123456", // duplicate idNumber
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2029, 1, 1),
                "Australia"
        );

        System.out.println("duplicateIdNumber: first=" + first + ", second=" + second);
        assertTrue(first);
        assertFalse(second);
    }
}
