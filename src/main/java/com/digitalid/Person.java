package com.digitalid;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Person {

    // File names for simple “database” storage (txt files)
    private static final String STORE_FILE = "persons.txt";
    private static final String ID_STORE_FILE = "ids.txt";

    // Separator used in each row of the txt file (basically our “columns”)
    private static final String SEP = "|";

    // Strict date parser: dd-MM-yyyy (strict stops invalid dates like 31-02-2025)
    private static final DateTimeFormatter DOB_FMT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    // Where we store the txt files (so tests can use a temp folder)
    private final Path storageDir;

    // Clock is injected so “today” is stable in unit tests (no flaky tests)
    private final Clock clock;

    public Person(Path storageDir, Clock clock) {
        this.storageDir = storageDir;
        this.clock = clock;
    }

//========================== ADD PERSON ===========================================

// Stores information (personID, name, address, birthdate, default demerit + suspension status) in text file if required conditions are met
// File Format: personId|firstName|lastName|streetNumber|street|city|state|country|dob|demeritPoints|suspended
    
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

        // Checks pathway for text file storage
        Path file = storageDir.resolve(STORE_FILE);
        List<String> lines = Files.exists(file) ? Files.readAllLines(file) : new ArrayList<>();

        // Checks if PersonID already exists in file
        if (personIdExists(lines, personID)) return false;        

        // Creates string with person's details seperated by |
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

        // Adds person's details to text file
        lines.add(newLine);
        Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        return true;
        
        // Failsafe (returns false) if error occurs
        } catch (IOException e) {
        
        return false;
    }
}

//=============UPDATE PERSONAL DETAILS=====================================

 /**
     * 1) If person is under 18 -> address cannot change
     * 2) If DOB changes -> NOTHING else can change
     * 3) If first digit of CURRENT ID is even -> ID cannot change
     *
     * Also re-checks addPerson-style validation for the new data.
     */

    public boolean updatePersonalDetails(
            String existingPersonId,
            String newPersonId,
            String newFirstName,
            String newLastName,
            String newAddress,   // StreetNumber|Street|City|State|Country
            String newBirthDate  // DD-MM-YYYY
    ) {
        try {

            // Basic “no blanks” checks (don’t update with empty data)
        if (newBirthDate == null || newBirthDate.isBlank()) return false;
        if (newPersonId == null || newPersonId.isBlank()) return false;
        if (newFirstName == null || newFirstName.isBlank()) return false;
        if (newLastName == null || newLastName.isBlank()) return false;
        if (newAddress == null || newAddress.isBlank()) return false;

            Path file = storageDir.resolve(STORE_FILE);
            if (!Files.exists(file)) return false; // can’t update if file doesn’t exist

            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            List<String> out = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    out.add(line);
                    continue;
                }

                // Split into fields (keep empty fields using -1)
                String[] p = line.split("\\|", -1);
                if (p.length < 11) { // expected 11 fields
                    // If the line is corrupted/short, don’t crash — keep it as-is
                    out.add(line);
                    continue;
                }

                // Current stored values
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

                // Not the person we want? keep the line unchanged
                if (!currentId.equals(existingPersonId)) {
                    out.add(line);
                    continue;
                }

                found = true;

                // Rebuild address string in the same format we expect
                String currentAddress =
                        currentStreetNo + SEP + currentStreet + SEP + currentCity + SEP + currentState + SEP + currentCountry;

                // ---------------- Condition 2 ----------------
                // If DOB changes, then no other detail can change.
                boolean dobChanged = !newBirthDate.equals(currentDob);
    
                // If DOB changes, everything else must stay exactly the same
                if (dobChanged) {
                    if (!newPersonId.equals(currentId)) return false;
                    if (!newFirstName.equals(currentFirst)) return false;
                    if (!newLastName.equals(currentLast)) return false;
                    if (!newAddress.equals(currentAddress)) return false;

                    // DOB still needs to be valid
                    if (!isValidBirthDate(newBirthDate)) return false;

                    // Only DOB field changes
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
            // If DOB not changing, new values must follow normal rules 

                // ---------------- addPerson rules must also pass ----------------

                String[] addressParts = (newAddress != null) ? newAddress.split("\\|", -1) : new String[0];

                if (!isValidPersonId(newPersonId)) return false;
                if (!isValidAddress(addressParts)) return false;
                if (!isValidBirthDate(newBirthDate)) return false;

                // Split new address into fields
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

                // Prevent changing to an ID that already exists (avoids duplicates)
                if (!newPersonId.equals(currentId) && personIdExists(lines, newPersonId)) return false;

                 // Build the updated record (demerit + suspended should not change here)
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

            if (!found) return false; // existingPersonId not found

            // Overwrite file with updated contents
            Files.write(file, out, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

            return true;

        } catch (IOException e) {
            return false;
        }
    }

// ====================== ADD ID =====================================================

    /**
   Stores a person’s ID in ids.txt (simple file storage).
    Current stored format:
     personID|idType|idNumber|expiryDate
    
     Checks:
    age rule (currently set as 18+ at the top)
    no duplicate idNumber across file
    type-specific idNumber formats
     expiryDate must be in the future
     */
    public boolean addID(String personID,
                         LocalDate dateOfBirth,
                         IDType idType,
                         String idNumber,
                         LocalDate issueDate,
                         LocalDate expiryDate,
                         String country) {

        LocalDate today = LocalDate.now(clock);

        // Age is calculated using DOB vs today 
        int age = Period.between(dateOfBirth, today).getYears();

        if (age < 18) {
            return false;
        }
        
        // Duplicate check: don’t allow same idNumber twice in ids.txt
        try {
            Path idFile = storageDir.resolve(ID_STORE_FILE);
            if (Files.exists(idFile)) {
                List<String> existingLines = Files.readAllLines(idFile);
                for (String line : existingLines) {
                    // stored: personID|idType|idNumber|expiry
                    if (line.split("\\|")[2].equals(idNumber)) return false; 
                }
            }
        } catch (IOException e) {
            return false;
        }

        // Format checks per ID type
        if (idType == IDType.PASSPORT) {
            // Passport: 2 uppercase letters + 6 digits (total 8 chars)
            if (!idNumber.matches("^[A-Z]{2}[0-9]{6}$")) {
                return false;
            }
        }

        if (idType == IDType.DRIVER_LICENSE) {
            // Driver Licence: 2 uppercase letters + 8 digits (total 10 chars)
            if (!idNumber.matches("^[A-Z]{2}[0-9]{8}$")) {
                return false;
            }
        }

        if (idType == IDType.MEDICARE) {
            // Medicare: exactly 9 digits
            if (!idNumber.matches("[0-9]{9}")) {
                return false;
            }
        }

        if (idType == IDType.STUDENT_ID) {
            // Student Card: exactly 12 digits, only if under 18
            // BUT because of the age < 18 return above, this block never gets used.
            if (age >= 18 || !idNumber.matches("[0-9]{12}")) {
                return false;
            }
        }

        // Expiry must be after today (not expired)
         if (expiryDate.isBefore(today)) {
            return false;
        }

        // Store ID into ids.txt
        try {
            Path file = storageDir.resolve(ID_STORE_FILE);
            List<String> lines = Files.exists(file) ? Files.readAllLines(file) : new ArrayList<>();
            
            // Stored Format: personID|idType|idNumber|expiryDate
            String newLine = String.join(SEP, personID, idType.toString(), idNumber, expiryDate.toString());
            lines.add(newLine);
            
            // Overwrite file with updated list
            Files.write(file, lines, StandardCharsets.UTF_8, 
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


// ================================== HELPERS =========================================

// CONDITION 2: PERSONID REQUIREMENTS 
// Checks PersonID meets neccessary requirements
    // Exactly 10 characters long
    // First two characters numbers between 1 and 9
    // At least two special characters between character 3 and 8
    // Last two characters uppercase letters A-Z

    private static boolean isValidPersonId(String id) {
        // Checks length is exactly 10 characters long
        if (id == null || id.length() != 10) return false;

        // Checks first two characeters are numbers between 1 and 9
        char c0 = id.charAt(0);
        char c1 = id.charAt(1);
        if (c0 < '2' || c0 > '9') return false;
        if (c1 < '2' || c1 > '9') return false;

        // Checks characters between 3rd and 8th position have 2 or more special characters
        String mid = id.substring(2, 8); // chars 3..8
        int specials = 0;
        for (char c : mid.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) specials++;
        }
        if (specials < 2) return false;

        // Checks last two characters are capital letters between A-Z
        char last1 = id.charAt(8);
        char last2 = id.charAt(9);
        return (last1 >= 'A' && last1 <= 'Z') && (last2 >= 'A' && last2 <= 'Z');
    }

// CONDITION 2: ADDRESS REQUIREMENTS 
// Checks given address meets necessary requirements
    // Should follow following format:
        // Street Number|Street|City|State|Country
    // State should only be Victoria
    // Country should only be Australia
    // Street number should only be numbers
    // Street, city, state, and country should only be letters

    private static boolean isValidAddress(String[] address) {
    
    // Checks address has correct number of inputs
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
// Checks given DOB meets necessary requirements
    // Should follow following format:
        // DD-MM-YYYY
    // DOB should be after current date (cannot be born in future)
    // Month should be valid (01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12)
    // Day should be no less than 1 and no more than 31

    private boolean isValidBirthDate(String dob) {
    if (dob == null || dob.isBlank()) return false;
    try {
        // Format check (DD-MM-YYYY) - dob formatter covers most requirements
        LocalDate birth = LocalDate.parse(dob, DOB_FMT);
        
        // Gets current date
        LocalDate today = LocalDate.now(clock);
        
        // Checks DOB is after current date
        return !birth.isAfter(today);
    } catch (Exception e) {
        return false;
    }
}

    // Helper for under-18 rule (uses clock, so tests don’t break later)
    private int getAge(String dob) {
        LocalDate birth = LocalDate.parse(dob, DOB_FMT);
        return Period.between(birth, LocalDate.now(clock)).getYears();
    }

    // Checks if a personId already exists in persons.txt
     private static boolean personIdExists(List<String> lines, String id) {
        for (String l : lines) {
            if (l == null || l.isBlank()) continue;
            String[] p = l.split("\\|", -1);
            if (p.length > 0 && p[0].equals(id)) return true;
        }
        return false;
    }
}


