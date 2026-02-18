package com.digitalid;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class TestAddID {

    @Test
    void testAddIDFunction() {

        AddID addID = new AddID();

        boolean t1 = addID.addID("P001", LocalDate.of(2000,1,1), IDType.PASSPORT, "AB123456",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");
        System.out.println("Test 1 - Valid Passport: " + t1);
        assertEquals(true, t1);

        boolean t2 = addID.addID("P002", LocalDate.of(1999,5,5), IDType.DRIVER_LICENSE, "CD123",
                LocalDate.of(2022,1,1), LocalDate.of(2029,1,1), "Australia");
        System.out.println("Test 2 - Invalid Driver Licence: " + t2);
        assertEquals(false, t2); // intentional fail

        boolean t3 = addID.addID("P003", LocalDate.of(1980,3,3), IDType.MEDICARE, "12345A789",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");
        System.out.println("Test 3 - Invalid Medicare: " + t3);
        assertEquals(false, t3);

        boolean t4 = addID.addID("P004", LocalDate.of(2010,3,1), IDType.PASSPORT, "AB123456",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");
        System.out.println("Test 4 - Under 18: " + t4);
        assertEquals(false, t4);

        boolean t5a = addID.addID("P005", LocalDate.of(2000,1,1), IDType.PASSPORT, "EF123456",
                LocalDate.of(2020,1,1), LocalDate.of(2030,1,1), "Australia");
        System.out.println("Test 5a - First ID Add: " + t5a);
        assertEquals(true, t5a);

        boolean t5b = addID.addID("P006", LocalDate.of(1990,1,1), IDType.DRIVER_LICENSE, "EF123456",
                LocalDate.of(2022,1,1), LocalDate.of(2029,1,1), "Australia");
        System.out.println("Test 5b - Duplicate ID Add: " + t5b);
        assertEquals(false, t5b);
    }
}

