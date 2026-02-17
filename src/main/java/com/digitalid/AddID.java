package com.digitalid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * addID stores a person's ID information in a TXT file.
 *
 * Rubric behaviour:
 * - If conditions are met -> write to TXT and return true
 * - Otherwise -> do not write and return false
 */
public class AddID {

    private static final String STORE_FILE = "ids.txt";
    private static final String SEP = "|";

    private final Path storageDir;
    private final Clock clock;

    public AddID(Path storageDir) {
        this(storageDir, Clock.systemDefaultZone());
    }

    public AddID(Path storageDir, Clock clock) {
        this.storageDir = storageDir;
        this.clock = clock;
    }

    /**
     * File format per line:
     * personId|dob|idType|idNumber|issueDate|expiryDate|country
     *
     * Notes:
     * - Student card: only allowed if person is under 18 AND person has no other IDs already stored.
     * - Passport/Driver/Medicare formats must match rubric exactly.
     */
    public boolean addID(String personId,
                         LocalDate dateOfBirth,
                         IDType idType,
                         String idNumber,
                         LocalDate issueDate,
                         LocalDate expiryDate,
                         String country) {

        // ===== Basic null/blank checks =====
        if (isBlank(personId) || dateOfBirth == null || idType == null || isBlank(idNumber)
                || issueDate == null || expiryDate == null || isBlank(country)) {
            return false;
        }

        // ===== Date checks (reasonable + prevents expired IDs being stored) =====
        LocalDate today = LocalDate.now(clock);
        if (issueDate.isAfter(expiryDate)) return false;
        if (expiryDate.isBefore(today)) return false;

        // ===== ID format checks (exact rubric rules) =====
        String cleanedNumber = idNumber.trim().toUpperCase();
        if (!matchesRubricFormat(idType, cleanedNumber)) return false;

        // ===== Under-18 Student Card rule =====
        boolean under18 = Period.between(dateOfBirth, today).getYears() < 18;

        // If under 18: ONLY STUDENT_CARD is allowed
        if (under18 && idType != IDType.STUDENT_CARD) return false;

        try {
            Files.createDirectories(storageDir);
            Path file = storageDir.resolve(STORE_FILE);
            if (!Files.exists(file)) Files.createFile(file);

            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

            // Duplicate rule (safe to include): Do not allow same idNumber twice globally
            for (String line : lines) {
                String[] p = splitLine(line);
                if (p != null && cleanedNumber.equalsIgnoreCase(p[3])) {
                    return false; // do not insert duplicate
                }
            }

            // If under 18 and student card: person must have NO other IDs already stored
            if (under18 && idType == IDType.STUDENT_CARD) {
                for (String line : lines) {
                    String[] p = splitLine(line);
                    if (p != null && personId.trim().equalsIgnoreCase(p[0])) {
                        return false; // already has an ID -> student card not allowed per rubric assumption
                    }
                }
            }

            // ===== Write record =====
            String record = String.join(SEP,
                    personId.trim(),
                    dateOfBirth.toString(),
                    idType.name(),
                    cleanedNumber,
                    issueDate.toString(),
                    expiryDate.toString(),
                    country.trim()
            );

            Files.write(file, List.of(record), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            return true;

        } catch (IOException e) {
            // Rubric wants false if it cannot be inserted
            return false;
        }
    }

    // ===== Helpers =====

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean matchesRubricFormat(IDType type, String number) {
        // Condition 1: Passport exactly 8 chars, first 2 A-Z, rest digits
        if (type == IDType.PASSPORT) return number.matches("^[A-Z]{2}[0-9]{6}$");

        // Condition 2: Driver licence exactly 10 chars, first 2 A-Z, rest digits
        if (type == IDType.DRIVER_LICENSE) return number.matches("^[A-Z]{2}[0-9]{8}$");

        // Condition 3: Medicare exactly 9 digits
        if (type == IDType.MEDICARE) return number.matches("^[0-9]{9}$");

        // Condition 4: Student card exactly 12 digits
        if (type == IDType.STUDENT_CARD) return number.matches("^[0-9]{12}$");

        return false;
    }

    // Expected 7 fields after split
    private static String[] splitLine(String line) {
        if (line == null) return null;
        String[] p = line.split("\\|", -1);
        return (p.length == 7) ? p : null;
    }

    public Object listAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listAll'");
    }
}
