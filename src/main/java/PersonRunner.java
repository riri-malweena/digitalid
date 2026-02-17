import java.nio.file.Paths;

public class PersonRunner {
    public static void main(String[] args) {

        Person person = new Person(Paths.get(".")); // digitalid folder

        boolean ok = person.updatePersonalDetails(
                "56s_d%&fAB",
                "56s_d%&fAB",
                "Emma", // changed from Emily
                "Smith",
                "32|Highland Street|Melbourne|Victoria|Australia",
                "15-11-1990"
        );

        System.out.println("Update ok = " + ok);
    }
}

