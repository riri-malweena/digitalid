public class testAddIDFunction {
    

@Test
void testAddIDFunction() {

    AddID addID = new AddID();

    // 1️⃣ Valid Passport
    boolean test1 = addID.addID(
            "P001",
            LocalDate.of(2000,1,1),
            IDType.PASSPORT,
            "AB123456",
            LocalDate.of(2020,1,1),
            LocalDate.of(2030,1,1),
            "Australia"
    );
    System.out.println("Test 1 - Valid Passport: " + test1);
    assertEquals(true, test1);


    // 2️⃣ Invalid Driver Licence (Intentional Fail)
    boolean test2 = addID.addID(
            "P002",
            LocalDate.of(1999,5,5),
            IDType.DRIVER_LICENSE,
            "CD123",
            LocalDate.of(2022,1,1),
            LocalDate.of(2029,1,1),
            "Australia"
    );
    System.out.println("Test 2 - Invalid Driver Licence: " + test2);
    assertEquals(false, test2);


    // 3️⃣ Invalid Medicare
    boolean test3 = addID.addID(
            "P003",
            LocalDate.of(1980,3,3),
            IDType.MEDICARE,
            "12345A789",
            LocalDate.of(2020,1,1),
            LocalDate.of(2030,1,1),
            "Australia"
    );
    System.out.println("Test 3 - Invalid Medicare: " + test3);
    assertEquals(false, test3);


    // 4️⃣ Under 18
    boolean test4 = addID.addID(
            "P004",
            LocalDate.of(2010,3,1),
            IDType.PASSPORT,
            "AB123456",
            LocalDate.of(2020,1,1),
            LocalDate.of(2030,1,1),
            "Australia"
    );
    System.out.println("Test 4 - Under 18: " + test4);
    assertEquals(false, test4);


    // 5️⃣ Duplicate ID
    boolean test5a = addID.addID(
            "P005",
            LocalDate.of(2000,1,1),
            IDType.PASSPORT,
            "EF123456",
            LocalDate.of(2020,1,1),
            LocalDate.of(2030,1,1),
            "Australia"
    );
    System.out.println("Test 5a - First Duplicate Entry: " + test5a);
    assertEquals(true, test5a);

    boolean test5b = addID.addID(
            "P006",
            LocalDate.of(1990,1,1),
            IDType.DRIVER_LICENSE,
            "EF123456",
            LocalDate.of(2022,1,1),
            LocalDate.of(2029,1,1),
            "Australia"
    );
    System.out.println("Test 5b - Second Duplicate Entry: " + test5b);
    assertEquals(false, test5b);
}
          }
