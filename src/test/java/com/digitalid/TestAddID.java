package com.digitalid;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class TestAddID {

    @Test
    void test1_validPassport() {
        AddID addID = new AddID();

        boolean result = addID.addID("P001", LocalDate.of(2000,1,1), IDType.PASSPORT, "AB123456",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");

        System.out.println("Test 1 - Valid Passport: " + (result ? "PASSED" : "FAILED"));
        assertEquals(true, result);
    }

    // intentional fail
    @Test
    void test2_invalidDriverLicence() {
        AddID addID = new AddID();

        boolean result = addID.addID("P002", LocalDate.of(1999,5,5), IDType.DRIVER_LICENSE, "CD123",
                LocalDate.of(2022,1,1), LocalDate.of(2029,1,1), "Australia");

        System.out.println("Test 2 - Invalid Driver Licence: " + (!result ? "PASSED" : "FAILED"));
        assertEquals(false, result);
    }

    @Test
    void test3_invalidMedicare() {
        AddID addID = new AddID();

        boolean result = addID.addID("P003", LocalDate.of(1980,3,3), IDType.MEDICARE, "12345A789",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");

        System.out.println("Test 3 - Invalid Medicare: " + (!result ? "PASSED" : "FAILED"));
        assertEquals(false, result);
    }

    @Test
    void test4_under18() {
        AddID addID = new AddID();

        boolean result = addID.addID("P004", LocalDate.of(2010,3,1), IDType.PASSPORT, "AB123456",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");

        System.out.println("Test 4 - Under 18: " + (!result ? "PASSED" : "FAILED"));
        assertEquals(false, result);
    }

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



