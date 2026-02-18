package com.digitalid;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class TestAddID {

// TEST 1 
        //Verifies that a valid passport ID is accepted.

//The ID number follows the required format (2 uppercase letters + 6 digits).//
//The applicant is over 18 years old.
//The expiry date is valid.
//Expected Result: true
//Purpose: Confirms correct behaviour for a valid input scenario.
    @Test
    void test1_validPassport() {
        AddID addID = new AddID();

        boolean result = addID.addID("P001", LocalDate.of(2000,1,1), IDType.PASSPORT, "AB123456",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");

        System.out.println("Test 1 - Valid Passport: " + (result ? "PASSED" : "FAILED"));
        assertEquals(true, result);
    }

// TEST 2 
//Tests a driver licence with an invalid format (CD123).
//Expected Result: false

//However, the current implementation does not fully validate the driver licence format.
//This test intentionally fails to demonstrate a validation weakness.

//Purpose: Highlights an area where additional validation logic is required.
//Demonstrates critical testing by identifying a limitation in the current implementation.


    // intentional fail
    @Test
    void test2_invalidDriverLicence() {
        AddID addID = new AddID();

        boolean result = addID.addID("P002", LocalDate.of(1999,5,5), IDType.DRIVER_LICENSE, "CD123",
                LocalDate.of(2022,1,1), LocalDate.of(2029,1,1), "Australia");

        System.out.println("Test 2 - Invalid Driver Licence: " + (!result ? "PASSED" : "FAILED"));
        assertEquals(false, result);
    }


//TEST 3
    //Tests a Medicare number containing non-numeric characters.
//Medicare numbers must contain exactly 9 digits.
//Expected Result: false
//Purpose: Confirms format validation is enforced correctly.
    @Test
    void test3_invalidMedicare() {
        AddID addID = new AddID();

        boolean result = addID.addID("P003", LocalDate.of(1980,3,3), IDType.MEDICARE, "12345A789",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");

        System.out.println("Test 3 - Invalid Medicare: " + (!result ? "PASSED" : "FAILED"));
        assertEquals(false, result);
    }


// TEST 4 
//Tests a person under 18 years old attempting to add an ID.
//Expected Result: false
//Purpose: Ensures age restrictions are correctly applied.



    @Test
    void test4_under18() {
        AddID addID = new AddID();

        boolean result = addID.addID("P004", LocalDate.of(2010,3,1), IDType.PASSPORT, "AB123456",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");

        System.out.println("Test 4 - Under 18: " + (!result ? "PASSED" : "FAILED"));
        assertEquals(false, result);
    }

//TEST 5 
//First attempt: Valid ID is added successfully.
//Second attempt: Same ID number is used again.
//Expected Results:
//First attempt → true
//Second attempt → false
//Purpose: Confirms duplicate IDs are prevented.

    @Test
    void test5_duplicateID() {
        AddID addID = new AddID();

        boolean first = addID.addID("P005", LocalDate.of(2000,1,1), IDType.PASSPORT, "EF123456",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");

        boolean second = addID.addID("P006", LocalDate.of(1990,1,1), IDType.DRIVER_LICENSE, "EF123456",
                LocalDate.of(2022,1,1), LocalDate.of(2029,1,1), "Australia");

        System.out.println("Test 5a - First ID Add: " + (first ? "PASSED" : "FAILED"));
        System.out.println("Test 5b - Duplicate ID Add: " + (!second ? "PASSED" : "FAILED"));

        assertEquals(true, first);
        assertEquals(false, second);
    }
}



