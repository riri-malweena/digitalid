package com.digitalid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

public class TestAddID {

    @TempDir
    Path tempDir;

    private AddID createService(LocalDate today) {
        Clock fixedClock = Clock.fixed(
                today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        return new AddID(tempDir, fixedClock);
    }

    @Test
    void testAddIDFunction() {

        LocalDate TODAY = LocalDate.of(2026, 2, 17);
        AddID addID = createService(TODAY);

        // 1) Valid passport -> expected TRUE
        assertEquals(true, addID.addID(
                "P001",
                LocalDate.of(2000, 1, 1),
                IDType.PASSPORT,
                "AB123456",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        ));

        // 2) Invalid driver licence format -> expected FALSE (INTENTIONAL FAIL if your validation is weak)
        assertEquals(false, addID.addID(
                "P002",
                LocalDate.of(1999, 5, 5),
                IDType.DRIVER_LICENSE,
                "CD123",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2029, 1, 1),
                "Australia"
        ));

        // 3) Invalid medicare format -> expected FALSE
        assertEquals(false, addID.addID(
                "P003",
                LocalDate.of(1980, 3, 3),
                IDType.MEDICARE,
                "12345A789",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        ));

        // 4) Under 18 -> expected FALSE
        assertEquals(false, addID.addID(
                "P004",
                LocalDate.of(2010, 3, 1),
                IDType.PASSPORT,
                "AB123456",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        ));

        // 5) Duplicate ID number -> first TRUE, second FALSE
        assertEquals(true, addID.addID(
                "P005",
                LocalDate.of(2000, 1, 1),
                IDType.PASSPORT,
                "EF123456",
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1),
                "Australia"
        ));

        assertEquals(false, addID.addID(
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
