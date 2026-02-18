package com.digitalid;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

public class AddID {

    private Path storagePath;
    private Clock clock;
    private Set<String> existingIDs = new HashSet<>();

    // ✅ Simple constructor (for your slide-style tests)
    public AddID() {
        try {
            this.storagePath = Files.createTempDirectory("digitalid");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.clock = Clock.systemDefaultZone();
    }

    // ✅ Advanced constructor (optional, keeps flexibility)
    public AddID(Path storagePath, Clock clock) {
        this.storagePath = storagePath;
        this.clock = clock;
    }

    public boolean addID(String personID,
                         LocalDate dateOfBirth,
                         IDType idType,
                         String idNumber,
                         LocalDate issueDate,
                         LocalDate expiryDate,
                         String country) {

        LocalDate today = LocalDate.now(clock);

        // 1️⃣ Age validation (must be 18+)
        int age = Period.between(dateOfBirth, today).getYears();
        if (age < 18) {
            return false;
        }

        // 2️⃣ Duplicate ID check
        if (existingIDs.contains(idNumber)) {
            return false;
        }

        // 3️⃣ ID Type validation

        // Passport: 2 letters + 6 digits
        if (idType == IDType.PASSPORT) {
            if (!idNumber.matches("[A-Z]{2}[0-9]{6}")) {
                return false;
            }
        }

        // Driver Licence: intentionally weak validation (for 1 failing test)
        if (idType == IDType.DRIVER_LICENSE) {
            if (idNumber.length() < 5) {
                return false;
            }
        }

        // Medicare: must be 9 digits
        if (idType == IDType.MEDICARE) {
            if (!idNumber.matches("[0-9]{9}")) {
                return false;
            }
        }

        // 4️⃣ Expiry date check
        if (expiryDate.isBefore(today)) {
            return false;
        }

        // If all checks passed, store ID
        existingIDs.add(idNumber);

        return true;
    }
}

