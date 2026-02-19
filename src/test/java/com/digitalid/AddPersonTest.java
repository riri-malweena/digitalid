package com.digitalid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AddPersonTest {

// Add Person for someone with valid ID, street, and birthdate
// PersonID:
// 56s_d%&fAB
// Address:
// 32|Highland Street|Melbourne|Victoria|Australia.
// Birthday:
// 15-11-1990
// ‘True’ (details added to text file)

// Add Person for someone in Queensland
// PersonID:
// 56s_d%&fAB
// Address:
// 32|Canterbury Street|Brisbane|Queensland|Australia.
// Birthday:
// 15-11-1990
// ‘False’ (no details added)

// Add Person for someone with incorrect birthdate format
// PersonID:
// 56s_d%&fAB
// Address:
// 32|Highland Street|Melbourne|Victoria|Australia.
// Birthday:
// 11-1990
// ‘False’ (no details added)

// Add Person for someone whose ID does not have 2 numbers between 2 and 9 at start
// PersonID:
// abs_d%&fAB
// Address:
// 32|Highland Street|Melbourne|Victoria|Australia.
// Birthday:
// 15-11-1990
// ‘False’ (no details added)

// Add Person for someone whose ID ends with lowercase letters
// PersonID:
// 56s_d%&fab
// Address:
// 32|Highland Street|Melbourne|Victoria|Australia.
// Birthday:
// 15-11-1990
// ‘False’ (no details added)

}
