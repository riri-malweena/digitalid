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
        Clock fixed = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        return new AddID(tempDir, fixed);
    }

    private static final LocalDate TODAY = LocalDate.of(2026, 2, 17);

    @Test
    void passport_valid_returnsTrue() {
        AddID s = serviceWithToday(TODAY);
        assertTrue(s.addID("P001", LocalDate.of(2000, 1, 1), IDType.PASSPORT, "AB123456",
                LocalDate.of(2020, 1, 1), LocalDate.of(2030, 1, 1), "Australia"));
    }

    @Test
    void driversLicence_invalidFormat_returnsFalse() {
        AddID s = serviceWithToday(TODAY);
        // Should be 10 chars total: 2 letters
    }
}