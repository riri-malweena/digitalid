package com.digitalid;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class TestAddID {

    @Test
    void testAddIDFunction() {

        AddID addID = new AddID();

        assertEquals(true, addID.addID(
                "P001",
                LocalDate.of(2000,1,1),
                IDType.PASSPORT,
                "AB123456",
                LocalDate.of(2020,1,1),
                LocalDate.of(2030,1,1),
                "Australia"
        ));

        // Intentional failure
        assertEquals(false, addID.addID(
                "P002",
                LocalDate.of(1999,5,5),
                IDType.DRIVER_LICENSE,
                "CD123",
                LocalDate.of(2022,1,1),
                LocalDate.of(2029,1,1),
                "Australia"
        ));

        assertEquals(false, addID.addID(
                "P003",
                LocalDate.of(1980,3,3),
                IDType.MEDICARE,
                "12345A789",
                LocalDate.of(2020,1,1),
                LocalDate.of(2030,1,1),
                "Australia"
        ));

        assertEquals(false, addID.addID(
                "P004",
                LocalDate.of(2010,3,1),
                IDType.PASSPORT,
                "AB123456",
                LocalDate.of(2020,1,1),
                LocalDate.of(2030,1,1),
                "Australia"
        ));

        assertEquals(true, addID.addID(
                "P005",
                LocalDate.of(2000,1,1),
                IDType.PASSPORT,
                "EF123456",
                LocalDate.of(2020,1,1),
                LocalDate.of(2030,1,1),
                "Australia"
        ));

        assertEquals(false, addID.addID(
                "P006",
                LocalDate.of(1990,1,1),
                IDType.DRIVER_LICENSE,
                "EF123456",
                LocalDate.of(2022,1,1),
                LocalDate.of(2029,1,1),
                "Australia"
        ));
    }
}
