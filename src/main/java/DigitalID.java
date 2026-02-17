

import java.time.LocalDate;
import java.util.Objects;

public class DigitalID {
    private final String personId;
    private final IDType type;
    private final String idNumber;
    private final LocalDate issueDate;
    private final LocalDate expiryDate;
    private final String country;

    public DigitalID(String personId, IDType type, String idNumber,
                     LocalDate issueDate, LocalDate expiryDate, String country) {
        this.personId = Objects.requireNonNull(personId, "personId");
        this.type = Objects.requireNonNull(type, "type");
        this.idNumber = Objects.requireNonNull(idNumber, "idNumber");
        this.issueDate = Objects.requireNonNull(issueDate, "issueDate");
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate");
        this.country = Objects.requireNonNull(country, "country");
    }

    public String getPersonId() { return personId; }
    public IDType getType() { return type; }
    public String getIdNumber() { return idNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getCountry() { return country; }
}
