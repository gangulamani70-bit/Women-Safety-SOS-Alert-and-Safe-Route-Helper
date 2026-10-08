package womensafety.util;

import womensafety.model.Location;
import womensafety.model.TrustedContact;
import womensafety.model.User;

import java.util.List;

public final class SampleData {
    private SampleData() { }

    public static void initialize(FileManager files) {
        initialize(files, true);
    }

    public static void initialize(FileManager files, boolean includeDemoData) {
        if (includeDemoData && files.isUsersFileEmpty()) {
            files.writeUsers(List.of(
                    new User("U001", "Ananya", "9876543210", "ananya@example.test", "1234"),
                    new User("U002", "Priya", "9876543212", "priya@example.test", "1234"),
                    new User("U003", "Sneha", "9876543213", "sneha@example.test", "1234"),
                    new User("U004", "Kavya", "9876543214", "kavya@example.test", "1234"),
                    new User("U005", "Meera", "9876543215", "meera@example.test", "1234")));
        }
        if (includeDemoData && files.isContactsFileEmpty()) {
            files.writeContacts("U001", List.of(
                    new TrustedContact("C001", "Ravi", "Brother", "9876543211"),
                    new TrustedContact("C002", "Priya", "Friend", "9876543212"),
                    new TrustedContact("C003", "Kumar", "Father", "9876543213"),
                    new TrustedContact("C004", "Sneha", "Sister", "9876543214"),
                    new TrustedContact("C005", "Arjun", "Friend", "9876543215")));
        }
        if (includeDemoData && files.readLocation("U001") == null) {
            files.writeLocation("U001", new Location("L001", 17.4483, 78.3915, "Madhapur, Hyderabad"));
        }
        if (includeDemoData && files.isAlertsFileEmpty()) {
            String[] samples = {
                    "A001|Ananya|Madhapur, Hyderabad|08-10-2026|09:10|SENT (SIMULATED)",
                    "A002|Ananya|Hitech City, Hyderabad|08-10-2026|10:20|SENT (SIMULATED)",
                    "A003|Priya|Gachibowli, Hyderabad|08-10-2026|11:15|SENT (SIMULATED)",
                    "A004|Sneha|Kukatpally, Hyderabad|08-10-2026|12:30|SENT (SIMULATED)",
                    "A005|Kavya|Jubilee Hills, Hyderabad|08-10-2026|13:10|SENT (SIMULATED)"
            };
            files.writeAlertSamples(samples);
        }
        if (includeDemoData && files.isSafePlacesFileEmpty()) {
            files.writeSafePlaceSamples(new String[]{
                    "SP001|Police|Madhapur Police Station|Madhapur|0.8|Local",
                    "SP002|Hospital|City Care Hospital|Madhapur|1.2|true",
                    "SP003|Shop|SafeMart|Hitech City|1.5|true",
                    "SP004|Police|Hitech Police Station|Hitech City|2.1|Local",
                    "SP005|Hospital|Apollo Hospital|Jubilee Hills|3.0|true",
                    "SP006|Shop|Corner Pharmacy|Gachibowli|2.4|false"
            });
        }
    }
}
