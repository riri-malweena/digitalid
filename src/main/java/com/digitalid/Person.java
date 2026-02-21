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

    // ADD PERSON
    /**
     Stores a new person into persons.txt.
    Format in file:
     personId|firstName|lastName|streetNumber|street|city|state|country|dob|demeritPoints|suspended
     */
    public boolean addPerson(
            String personID,
            String firstName,
            String lastName,
            String address,
            String birthDate
    ) {

        // Quick “no empty input” check
        String[] inputs = {personID, firstName, lastName, address, birthDate};
        for (String s : inputs) {
            if (s == null || s.isBlank()) {
                return false; // reject missing details
            }
        }

        // Validate rules given by spec (ID, address format, and DOB)
        if (!isValidPersonId(personID) || !isValidAddress(address) || !isValidBirthDate(birthDate)) {
            return false;
        }

        try {
            Path file = storageDir.resolve(STORE_FILE);

            // If the file exists, load it; otherwise start with empty list
            List<String> lines = Files.exists(file) ? Files.readAllLines(file) : new ArrayList<>();

            // Make sure the personID is not already used
            if (personIdExists(lines, personID)) return false;

            // Address is stored as: StreetNumber|Street|City|State|Country
            String[] addressParts = address.split("\\|", -1);

            // Build a new record line for the txt file
            // default demeritPoints = 0, suspended = false
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
                    "0",
                    "false"
            );

            lines.add(newLine);

            // Writes back the whole file 
            Files.write(file, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return true;

        } catch (IOException e) {
            // If something goes wrong reading/writing, we return false (safe fail)
            return false;
        }
    }

    // UPDATE PERSONAL DETAILS
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
            String newBirthDate  // dd-MM-yyyy
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
                if (p.length < 11) {
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

                //Rule 2: if DOB changes, nothing else can change
                boolean dobChanged = !newBirthDate.equals(currentDob);
                if (dobChanged) {

                    // If DOB changes, everything else must stay exactly the same
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
                if (!isValidPersonId(newPersonId)) return false;
                if (!isValidAddress(newAddress)) return false;
                if (!isValidBirthDate(newBirthDate)) return false;

                // Split new address into fields
                String[] addr = newAddress.split("\\|", -1);
                String newStreetNo = addr[0];
                String newStreet = addr[1];
                String newCity = addr[2];
                String newState = addr[3];
                String newCountry = addr[4];

                // Rule 1: if under 18, address cannot change 
                int ageNow = getAge(currentDob);
                if (ageNow < 18 && !newAddress.equals(currentAddress)) return false;

                // Rule 3: if first digit of CURRENT id is even, ID cannot change
                int firstDigit = currentId.charAt(0) - '0';
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

    //ADD ID
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

            // Stored format: personID|idType|idNumber|expiryDate
            String newLine = String.join(SEP,
                    personID,
                    idType.toString(),
                    idNumber,
                    expiryDate.toString()
            );

            lines.add(newLine);

            // Overwrite file with updated list
            Files.write(file, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return true;

        } catch (IOException e) {
            return false;
        }
    }


    /**
     * Person ID rule:
    length 10
     first 2 chars are digits 2..9
     chars 3..8 contain at least 2 special characters (not letters/digits)
     last 2 chars are uppercase letters A..Z
     */
    private static boolean isValidPersonId(String id) {
        if (id == null || id.length() != 10) return false;

        char c0 = id.charAt(0);
        char c1 = id.charAt(1);

        //First two characters must be digits between 2 and 9
        if (c0 < '2' || c0 > '9') return false;
        if (c1 < '2' || c1 > '9') return false;

        //Middle section must contain at least 2 special characters
        String mid = id.substring(2, 8);
        int specials = 0;
        for (char c : mid.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) specials++;
        }
        if (specials < 2) return false;

        //Last two must be uppercase letters
        char last1 = id.charAt(8);
        char last2 = id.charAt(9);
        return (last1 >= 'A' && last1 <= 'Z') && (last2 >= 'A' && last2 <= 'Z');
    }

    /**
     Address rule:
     streetNumber|street|city|state|country
     streetNumber: 1..6 digits
     street/city: letters/spaces only
     state must be Victoria
     country must be Australia
     */
    private static boolean isValidAddress(String address) {
        if (address == null) return false;

        String[] parts = address.split("\\|", -1);
        if (parts.length != 5) return false;

        String streetNum = parts[0];
        String street = parts[1];
        String city = parts[2];
        String state = parts[3];
        String country = parts[4];

        return streetNum.matches("\\d{1,6}")
                && street.matches("[a-zA-Z\\s]+")
                && city.matches("[a-zA-Z\\s]+")
                && state.equalsIgnoreCase("Victoria")
                && country.equalsIgnoreCase("Australia");
    }

    /**
     DOB rule:
     must match dd-MM-yyyy and be a real date
     must not be in the future
     Uses clock so tests can control "today".
     */
    private boolean isValidBirthDate(String dob) {
        if (dob == null || dob.isBlank()) return false;
        try {
            LocalDate birth = LocalDate.parse(dob, DOB_FMT);
            LocalDate today = LocalDate.now(clock);

            // DOB cannot be after today
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

