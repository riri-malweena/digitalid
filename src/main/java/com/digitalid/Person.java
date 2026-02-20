package com.digitalid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class Person {

    private static final String STORE_FILE = "persons.txt";
    private static final String SEP = "|";

    private static final DateTimeFormatter DOB_FMT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final Path storageDir;
    private final Clock clock;

    public Person(Path storageDir, Clock clock) {
        this.storageDir = storageDir;
        this.clock = clock;
    }

    public boolean addPerson(
        String personID,
        String firstName,
        String lastName, 
        String address,
        String birthDate
         ) {
        
        String[] inputs = {personID, firstName, lastName, address, birthDate};
        for (String s : inputs) {
            if (s == null || s.isBlank()) {
                return false;
            }
        }
        
        if (!isValidPersonId(personID) || !isValidAddress(address) || !isValidBirthDate(birthDate)){
            return false;
        }

        try {
        Path file = storageDir.resolve(STORE_FILE);
        List<String> lines = Files.exists(file) ? Files.readAllLines(file) : new ArrayList<>();

        if (personIdExists(lines, personID)) return false;

        String[] addressParts = address.split("\\|", -1);

        String newLine = String.join(SEP,
            personID,
            firstName,
            lastName,
            addressParts[0], // streetNumber
            addressParts[1], // street
            addressParts[2], // city
            addressParts[3], // state
            addressParts[4], // country
            birthDate,
            "0",          // default demerit points
            "false"       // default suspended value
        );

        lines.add(newLine);
        Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        return true;
        
        } catch (IOException e) {
        
        return false;
    }
}

    public boolean updatePersonalDetails(
            String existingPersonId,
            String newPersonId,
            String newFirstName,
            String newLastName,
            String newAddress,   // StreetNumber|Street|City|State|Country
            String newBirthDate  // DD-MM-YYYY
    ) {
        try {

        if (newBirthDate == null || newBirthDate.isBlank()) return false;
        if (newPersonId == null || newPersonId.isBlank()) return false;
        if (newFirstName == null || newFirstName.isBlank()) return false;
        if (newLastName == null || newLastName.isBlank()) return false;
        if (newAddress == null || newAddress.isBlank()) return false;

            Path file = storageDir.resolve(STORE_FILE);
            if (!Files.exists(file)) return false;

            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<String> out = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    out.add(line);
                    continue;
                }

                String[] p = line.split("\\|", -1);
                if (p.length < 11) { // expected 11 fields
                    out.add(line);
                    continue;
                }

                String currentId = p[0];
                String currentFirst = p[1];
                String currentLast = p[2];
                String currentStreetNo = p[3];
                String currentStreet = p[4];
                String currentCity = p[5];
                String currentState = p[6];
                String currentCountry = p[7];
                String currentDob = p[8];
                String currentDemerit = p[9];
                String currentSuspended = p[10];

                if (!currentId.equals(existingPersonId)) {
                    out.add(line);
                    continue;
                }

                found = true;

                String currentAddress =
                        currentStreetNo + SEP + currentStreet + SEP + currentCity + SEP + currentState + SEP + currentCountry;

                // ---------------- Condition 2 ----------------
                // If DOB changes, then no other detail can change.
                boolean dobChanged = !newBirthDate.equals(currentDob);
                if (dobChanged) {
                    if (!newPersonId.equals(currentId)) return false;
                    if (!newFirstName.equals(currentFirst)) return false;
                    if (!newLastName.equals(currentLast)) return false;
                    if (!newAddress.equals(currentAddress)) return false;

                    if (!isValidBirthDate(newBirthDate)) return false;

                    String updatedLine = String.join(SEP,
                            currentId,
                            currentFirst,
                            currentLast,
                            currentStreetNo,
                            currentStreet,
                            currentCity,
                            currentState,
                            currentCountry,
                            newBirthDate,
                            currentDemerit,
                            currentSuspended
                    );
                    out.add(updatedLine);
                    continue;
                }

                // ---------------- addPerson rules must also pass ----------------
                if (!isValidPersonId(newPersonId)) return false;
                if (!isValidAddress(newAddress)) return false;
                if (!isValidBirthDate(newBirthDate)) return false;

                String[] addr = newAddress.split("\\|", -1);
                String newStreetNo = addr[0];
                String newStreet = addr[1];
                String newCity = addr[2];
                String newState = addr[3];
                String newCountry = addr[4];

                // ---------------- Condition 1 ----------------
                // If person is under 18, their address cannot be changed.
                int ageNow = getAge(currentDob);
                if (ageNow < 18 && !newAddress.equals(currentAddress)) return false;

                // ---------------- Condition 3 ----------------
                // If first digit of CURRENT ID is even, their ID cannot be changed.
                int firstDigit = currentId.charAt(0) - '0'; // ID format guarantees digit
                if (firstDigit % 2 == 0 && !newPersonId.equals(currentId)) return false;

                // Optional (safe): don't allow changing to an ID that already exists
                if (!newPersonId.equals(currentId) && personIdExists(lines, newPersonId)) return false;

                // Write updated row (demeritPoints + suspension stay unchanged)
                String updatedLine = String.join(SEP,
                        newPersonId,
                        newFirstName,
                        newLastName,
                        newStreetNo,
                        newStreet,
                        newCity,
                        newState,
                        newCountry,
                        newBirthDate,
                        currentDemerit,
                        currentSuspended
                );

                out.add(updatedLine);
            }

            if (!found) return false;

            Files.write(file, out, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    // ---------------- Helpers (addPerson validations) ----------------

    // Condition 1: ID rule
    private static boolean isValidPersonId(String id) {
        if (id == null || id.length() != 10) return false;

        char c0 = id.charAt(0);
        char c1 = id.charAt(1);
        if (c0 < '2' || c0 > '9') return false;
        if (c1 < '2' || c1 > '9') return false;

        String mid = id.substring(2, 8); // chars 3..8
        int specials = 0;
        for (char c : mid.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) specials++;
        }
        if (specials < 2) return false;

        char last1 = id.charAt(8);
        char last2 = id.charAt(9);
        return (last1 >= 'A' && last1 <= 'Z') && (last2 >= 'A' && last2 <= 'Z');
    }

    // Condition 2: Address rule
    private static boolean isValidAddress(String address) {
    if (address == null) return false;
    
    String[] parts = address.split("\\|", -1);
    if (parts.length != 5) return false;

    String streetNum = parts[0];
    String street = parts[1];
    String city = parts[2];
    String state = parts[3];
    String country = parts[4];

    return streetNum.matches("\\d{1,6}") &&          // Max 6 digits
           street.matches("[a-zA-Z\\s]+") &&         // Letters/spaces only
           city.matches("[a-zA-Z\\s]+") &&           // Letters/spaces only
           state.equalsIgnoreCase("Victoria") &&     // Strict State
           country.equalsIgnoreCase("Australia");    // Strict Country
}

    // Condition 3: DOB rule
    private boolean isValidBirthDate(String dob) {
    if (dob == null || dob.isBlank()) return false;
    try {
        // Format check (DD-MM-YYYY)
        LocalDate birth = LocalDate.parse(dob, DOB_FMT);
        
        // check date
        LocalDate today = LocalDate.now(clock);
        
        // DOB must be after current date
        return !birth.isAfter(today);
    } catch (Exception e) {
        return false;
    }
}

    // CHANGED: uses clock (stable tests)
    private int getAge(String dob) {
        LocalDate birth = LocalDate.parse(dob, DOB_FMT);
        return Period.between(birth, LocalDate.now(clock)).getYears();
    }

    private static boolean personIdExists(List<String> lines, String id) {
        for (String l : lines) {
            if (l == null || l.isBlank()) continue;
            String[] p = l.split("\\|", -1);
            if (p.length > 0 && p[0].equals(id)) return true;
        }
        return false;
    }
}

