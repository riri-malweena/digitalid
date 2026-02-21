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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

//========================== ADD PERSON ===========================================

// Stores information (personID, name, address, birthdate, default demerit + suspension status) in
// text file if required conditions are met

    public boolean addPerson(
        String personID,
        String firstName,
        String lastName, 
        String address,
        String birthDate
         ) {
    
        // Returns false if any inputs are blank or null
        String[] inputs = {personID, firstName, lastName, address, birthDate};
        for (String s : inputs) {
            if (s == null || s.isBlank()) {
                return false;
            }
        }
        
        // Stores seperated parts of address in string array
        String[] addressParts = address.split("\\|", -1);

        // Calls helper functions to check if personID, address, and birth date requirements are met 
        if (!isValidPersonId(personID) || !isValidAddress(addressParts) || !isValidBirthDate(birthDate)){
            return false;
        }

        // If requirements met, attempts to append info to text file
        try {
        Path file = storageDir.resolve(STORE_FILE);
        List<String> lines = Files.exists(file) ? Files.readAllLines(file) : new ArrayList<>();

        if (personIdExists(lines, personID)) return false;

        // Splits address into individual parts for 
        

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

//=============UPDATE PERSONAL DETAILS=====================================

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

                String[] addressParts = (newAddress != null) ? newAddress.split("\\|", -1) : new String[0];

                if (!isValidPersonId(newPersonId)) return false;
                if (!isValidAddress(addressParts)) return false;
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

// ====================== ADD ID =====================================================

// --- Constant for ID storage file ---
    private static final String ID_STORE_FILE = "ids.txt";

    /**
     * addID function. This method stores information about a persons ID in a TXT file.
     * Based on the provided requirements for Passports, Drivers Licences, Medicare, and Student Cards.
     */
    public boolean addID(String personID,
                         LocalDate dateOfBirth,
                         IDType idType,
                         String idNumber,
                         LocalDate issueDate,
                         LocalDate expiryDate,
                         String country) {

        LocalDate today = LocalDate.now(clock);

        // 1️⃣ Age validation (must be 18+ for most IDs, except student cards)
        // Calculates the age of the applicant based on their date of birth and the current date. 
        int age = Period.between(dateOfBirth, today).getYears();
        if (age < 18) {
            return false;
        }
        
        // 2️⃣ Duplicate ID check
        // Note: For the file-based version, we check the file instead of a memory Set
        try {
            Path idFile = storageDir.resolve(ID_STORE_FILE);
            if (Files.exists(idFile)) {
                List<String> existingLines = Files.readAllLines(idFile);
                for (String line : existingLines) {
                    if (line.split("\\|")[2].equals(idNumber)) return false; 
                }
            }
        } catch (IOException e) {
            return false;
        }

        // 3️⃣ ID Type validation

        // Passport: exactly 8 characters long; 2 letters + 6 digits
        // Validates the format of a passport ID number. The method checks if the ID number matches the required pattern.
        if (idType == IDType.PASSPORT) {
            if (!idNumber.matches("^[A-Z]{2}[0-9]{6}$")) {
                return false;
            }
        }

        // Driver Licence: exactly 10 characters long; 2 letters + 8 digits
        // Validates the format of a driver licence ID number. 
        if (idType == IDType.DRIVER_LICENSE) {
            if (!idNumber.matches("^[A-Z]{2}[0-9]{8}$")) {
                return false;
            }
        }

        // Medicare: must be 9 digits
        // Validates the format of a Medicare ID number. The method checks if the ID number consists of exactly nine digits.
        if (idType == IDType.MEDICARE) {
            if (!idNumber.matches("[0-9]{9}")) {
                return false;
            }
        }

        // Student Card: exactly 12 digits, only if person is under 18
        // If a person is under 18 a student card can instead be added.
        if (idType == IDType.STUDENT_ID) {
            if (age >= 18 || !idNumber.matches("[0-9]{12}")) {
                return false;
            }
        }

        // 4️⃣ Expiry date check
        // Validates that the expiry date of the ID is in the future.
        if (expiryDate.isBefore(today)) {
            return false;
        }

        // If all checks passed, store ID in TXT file
        try {
            Path file = storageDir.resolve(ID_STORE_FILE);
            List<String> lines = Files.exists(file) ? Files.readAllLines(file) : new ArrayList<>();
            
            // Format: personID|idType|idNumber|expiryDate
            String newLine = String.join(SEP, personID, idType.toString(), idNumber, expiryDate.toString());
            lines.add(newLine);
            
            Files.write(file, lines, StandardCharsets.UTF_8, 
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


// ================================== HELPERS =========================================

// CONDITION 2: PERSONID REQUIREMENTS 
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

// CONDITION 2: ADDRESS REQUIREMENTS 
    private static boolean isValidAddress(String[] address) {
    
    // Checks address has current number of values
    if (address.length != 5) return false;

    // Assumes inputted address is in streetnum/street/city/state/country format
    // Will fail if address is correct but in different order
    String streetNum = address[0];
    String street = address[1];
    String city = address[2];
    String state = address[3];
    String country = address[4];

    // Returns true if all requirements met and false if not
    return streetNum.matches("\\d{1,6}") &&          // Street number must be a digit with at least 1 and no more than 6 characters
           street.matches("[a-zA-Z\\s]+") &&         // Street and City must only have letters (no numbers or special characters)
           city.matches("[a-zA-Z\\s]+") &&          
           state.matches("(?i)Victoria|Vic") &&     // State must be Victoria, accomodates capitilisation (victoria) and shorthand (vic)
           country.equalsIgnoreCase("Australia");    // Country must be 'Australia' regardless of capitilisation
                                                                    // Does not accomodate shorthand (aus) as could be different country
}

// CONDITION 3: DOB REQUIREMENTS
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
    // Update Personal Details under 18 checker

    private int getAge(String dob) {
        LocalDate birth = LocalDate.parse(dob, DOB_FMT);
        return Period.between(birth, LocalDate.now(clock)).getYears();
    }

    // Update Personal Details existing ID checker
    private static boolean personIdExists(List<String> lines, String id) {
        for (String l : lines) {
            if (l == null || l.isBlank()) continue;
            String[] p = l.split("\\|", -1);
            if (p.length > 0 && p[0].equals(id)) return true;
        }
        return false;
    }
}

