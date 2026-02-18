package com.digitalid;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

public class AddID {

    private Path storagePath;
    private Clock clock;

    // Used to check duplicate ID numbers
    private static Set<String> existingIDs = new HashSet<>();

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

        // 1️⃣ Check age (must be 18 or older)
        int age = Period.between(dateOfBirth, today).getYears();
        if (age < 18) {
            return false;
        }

        // 2️⃣ Check duplicate ID number
        if (existingIDs.contains(idNumber)) {
            return false;
        }

        // 3️⃣ Validate based on ID type
        if (idType == IDType.PASSPORT) {

            // Passport format: 2 letters + 6 digits
            if (!idNumber.matches("[A-Z]{2}[0-9]{6}")) {
                return false;
            }

        } else if (idType == IDType.DRIVER_LICENSE) {

            // ❗ INTENTIONAL BUG:
            // Only checks length (so invalid format like CD123 passes)
            if (idNumber.length() < 5) {
                return false;
            }

        } else if (idType == IDType.MEDICARE) {

            // Medicare: must be exactly 9 digits
            if (!idNumber.matches("[0-9]{9}")) {
                return false;
            }

        } else {
            return false;
        }

        // 4️⃣ Check expiry date is after today
        if (expiryDate.isBefore(today)) {
            return false;
        }

        // If all checks passed, store ID
        existingIDs.add(idNumber);

        return true;
    }
}
