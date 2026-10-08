package womensafety.util;

import womensafety.model.Location;
import womensafety.model.SOSAlert;
import womensafety.model.TrustedContact;
import womensafety.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private final Path usersFile;
    private final Path contactsFile;
    private final Path locationsFile;
    private final Path alertsFile;
    private final Path safePlacesFile;
    private final Path emergencyNumbersFile;

    public FileManager() {
        this(Path.of("data"));
    }

    public FileManager(Path data) {
        this.usersFile = data.resolve("users.txt");
        this.contactsFile = data.resolve("contacts.txt");
        this.locationsFile = data.resolve("locations.txt");
        this.alertsFile = data.resolve("alert_history.txt");
        this.safePlacesFile = data.resolve("safe_places.txt");
        this.emergencyNumbersFile = data.resolve("emergency_numbers.txt");
        try {
            Files.createDirectories(data);
            for (Path file : List.of(usersFile, contactsFile, locationsFile, alertsFile, safePlacesFile, emergencyNumbersFile)) {
                if (Files.notExists(file)) Files.createFile(file);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize local data files.", e);
        }
    }

    public List<String[]> readRows(Path file, int columns) {
        List<String[]> rows = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] fields = line.split("\\|", -1);
                if (fields.length == columns) rows.add(fields);
            }
            return rows;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + file + ".", e);
        }
    }

    public List<User> readUsers() {
        List<User> result = new ArrayList<>();
        for (String[] r : readRows(usersFile, 5)) result.add(new User(r[0], r[1], r[2], r[3], r[4]));
        return result;
    }

    public void writeUsers(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) lines.add(join(u.getUserId(), u.getName(), u.getPhone(), u.getEmail(), u.getPassword()));
        write(usersFile, lines);
    }

    public List<TrustedContact> readContacts(String userId) {
        List<TrustedContact> result = new ArrayList<>();
        for (String[] r : readRows(contactsFile, 5)) {
            if (r[0].equals(userId)) result.add(new TrustedContact(r[1], r[2], r[3], r[4]));
        }
        return result;
    }

    public void writeContacts(String userId, List<TrustedContact> contacts) {
        List<String> lines = new ArrayList<>();
        for (String[] r : readRows(contactsFile, 5)) if (!r[0].equals(userId)) {
            lines.add(join(r[0], r[1], r[2], r[3], r[4]));
        }
        for (TrustedContact c : contacts) lines.add(join(userId, c.getContactId(), c.getName(), c.getRelation(), c.getPhone()));
        write(contactsFile, lines);
    }

    public Location readLocation(String userId) {
        for (String[] r : readRows(locationsFile, 6)) {
            if (r[0].equals(userId)) {
                Location location = new Location(r[1], Double.parseDouble(r[2]), Double.parseDouble(r[3]), r[4]);
                location.setTimestamp(r[5]);
                return location;
            }
        }
        return null;
    }

    public void writeLocation(String userId, Location location) {
        List<String> lines = new ArrayList<>();
        for (String[] r : readRows(locationsFile, 6)) if (!r[0].equals(userId)) {
            lines.add(join(r[0], r[1], r[2], r[3], r[4], r[5]));
        }
        lines.add(join(userId, location.getLocationId(), location.getLatitude(), location.getLongitude(),
                location.getAddress(), location.getTimestamp()));
        write(locationsFile, lines);
    }

    public List<String[]> readAlerts() { return readRows(alertsFile, 6); }
    public List<String[]> readSafePlaces() { return readRows(safePlacesFile, 6); }

    public void saveAlert(SOSAlert alert) {
        String line = join(alert.getAlertId(), alert.getUser().getName(), alert.getLocation().getAddress(),
                alert.getDate(), alert.getTime(), alert.getStatus());
        try {
            Files.writeString(alertsFile, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save alert history.", e);
        }
    }

    public boolean isUsersFileEmpty() { return readRows(usersFile, 5).isEmpty(); }
    public boolean isContactsFileEmpty() { return readRows(contactsFile, 5).isEmpty(); }
    public boolean isAlertsFileEmpty() { return readRows(alertsFile, 6).isEmpty(); }
    public void writeAlertSamples(String[] samples) { write(alertsFile, List.of(samples)); }
    public void writeSafePlaceSamples(String[] samples) { write(safePlacesFile, List.of(samples)); }
    public boolean isSafePlacesFileEmpty() { return readSafePlaces().isEmpty(); }

    public String[] readEmergencyNumbers(String userId) {
        for (String[] row : readRows(emergencyNumbersFile, 3)) {
            if (row[0].equals(userId)) return row;
        }
        return new String[]{userId, "", ""};
    }

    public void writeEmergencyNumbers(String userId, String police, String hospital) {
        List<String> rows = new ArrayList<>();
        for (String[] row : readRows(emergencyNumbersFile, 3)) {
            if (!row[0].equals(userId)) rows.add(join(row[0], row[1], row[2]));
        }
        rows.add(join(userId, police, hospital));
        write(emergencyNumbersFile, rows);
    }

    private void write(Path file, List<String> lines) {
        try { Files.write(file, lines, StandardCharsets.UTF_8); }
        catch (IOException e) { throw new IllegalStateException("Could not save " + file + ".", e); }
    }

    private String join(Object... values) {
        List<String> safe = new ArrayList<>();
        for (Object value : values) safe.add(String.valueOf(value).replace("|", " ").replace("\n", " "));
        return String.join("|", safe);
    }
}
