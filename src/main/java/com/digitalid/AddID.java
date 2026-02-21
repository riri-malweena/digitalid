// package com.digitalid;

// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.time.Clock;
// import java.time.LocalDate;
// import java.time.Period;
// import java.util.HashSet;
// import java.util.Set;

// public class AddID {

//     private Path storagePath;
//     private Clock clock;
//     private Set<String> existingIDs = new HashSet<>();

//     //  Simple constructor (for your slide-style tests)
//     //  Initializes storage path to a temporary directory and uses the system clock. This constructor is suitable for basic testing scenarios where default settings are sufficient. It abstracts away the complexities of storage management and time handling, allowing you to focus on testing the core functionality of adding IDs without worrying about external dependencies.
//     public AddID() {
//         try {
//             this.storagePath = Files.createTempDirectory("digitalid");
//         } catch (Exception e) {
//             throw new RuntimeException(e);
//         }
//         this.clock = Clock.systemDefaultZone();
//     }

//     //  Advanced constructor (optional, keeps flexibility)
//     //  Allows injection of storage path and clock for better testability and separation of concerns.
//     public AddID(Path storagePath, Clock clock) {
//         this.storagePath = storagePath;
//         this.clock = clock;
//     }

//     public boolean addID(String personID,
//                          LocalDate dateOfBirth,
//                          IDType idType,
//                          String idNumber,
//                          LocalDate issueDate,
//                          LocalDate expiryDate,
//                          String country) {

//         LocalDate today = LocalDate.now(clock);

//         // 1️⃣ Age validation (must be 18+)
//         // Calculates the age of the applicant based on their date of birth and the current date. If the calculated age is less than 18 years, the method returns false, indicating that the ID cannot be added due to age restrictions. This check ensures compliance with legal requirements for identification documents, which typically require individuals to be of a certain age to obtain valid IDs.
//         int age = Period.between(dateOfBirth, today).getYears();
//         if (age < 18) {
//             return false;
//         }

//         // 2️⃣ Duplicate ID check
//         // Checks if the provided ID number already exists in the system. If the ID number is found in the existingIDs set, the method returns false, indicating that duplicate IDs are not allowed. This check helps maintain the integrity of the digital ID system by preventing multiple entries with the same identification number, which could lead to confusion and potential security issues.
//         if (existingIDs.contains(idNumber)) {
//             return false;
//         }

//         // 3️⃣ ID Type validation

//         // Passport: 2 letters + 6 digits
//         // Validates the format of a passport ID number. The method checks if the ID number matches the required pattern of two uppercase letters followed by six digits. If the ID number does not conform to this format, the method returns false, indicating that the passport ID is invalid. This validation ensures that only properly formatted passport IDs are accepted into the system, which is crucial for maintaining data consistency and reliability.
//         if (idType == IDType.PASSPORT) {
//             if (!idNumber.matches("[A-Z]{2}[0-9]{6}")) {
//                 return false;
//             }
//         }

//         // Driver Licence: intentionally weak validation (for 1 failing test)
//         // Validates the format of a driver licence ID number. The current implementation only checks if the ID number is at least 5 characters long, which is intentionally weak to allow for a failing test case. In a real-world application, this validation would need to be more robust, checking for specific patterns and formats associated with driver licences. This placeholder validation serves to demonstrate the importance of comprehensive input validation in ensuring the integrity of the digital ID system.
//         if (idType == IDType.DRIVER_LICENSE) {
//             if (idNumber.length() < 5) {
//                 return false;
//             }
//         }

//         // Medicare: must be 9 digits
//         // Validates the format of a Medicare ID number. The method checks if the ID number consists of exactly nine digits. If the ID number does not match this pattern, the method returns false, indicating that the Medicare ID is invalid. This validation ensures that only properly formatted Medicare IDs are accepted into the system, which is essential for maintaining data accuracy and preventing errors in identification.
//         if (idType == IDType.MEDICARE) {
//             if (!idNumber.matches("[0-9]{9}")) {
//                 return false;
//             }
//         }

//         // 4️⃣ Expiry date check
//         // Validates that the expiry date of the ID is in the future. The method compares the provided expiry date with the current date, and if the expiry date is before today, it returns false, indicating that the ID cannot be added because it has already expired. This check ensures that only valid, non-expired IDs are stored in the system, which is crucial for maintaining the reliability and trustworthiness of the digital ID management system.
//         if (expiryDate.isBefore(today)) {
//             return false;
//         }

//         // If all checks passed, store ID
//         // In a real implementation, this would involve writing to a database or file system. For simplicity, we just track existing IDs in memory.
//         existingIDs.add(idNumber);

//         return true;
//     }
// }

