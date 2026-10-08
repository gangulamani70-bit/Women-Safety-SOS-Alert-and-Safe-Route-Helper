package womensafety.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import womensafety.exception.NoContactException;
import womensafety.model.Location;
import womensafety.model.SOSAlert;
import womensafety.model.TrustedContact;
import womensafety.model.User;
import womensafety.util.FileManager;
import womensafety.util.SampleData;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SafetyServicesTest {
    @TempDir Path tempDir;

    @Test
    void sosRequiresTrustedContact() {
        FileManager files = new FileManager(tempDir);
        User user = new User("U100", "Demo", "9876543210", "demo@example.test", "pw");
        user.updateLocation(new Location("L1", 17.4, 78.3, "Madhapur, Hyderabad"));

        assertThrows(NoContactException.class, () -> new AlertService(files).sendSOS(user));
    }

    @Test
    void sosSavesAlertAndCanBeReadBack() throws Exception {
        FileManager files = new FileManager(tempDir);
        User user = new User("U100", "Demo", "9876543210", "demo@example.test", "pw");
        user.updateLocation(new Location("L1", 17.4, 78.3, "Madhapur, Hyderabad"));
        user.setTrustedContacts(List.of(new TrustedContact("C1", "Alex", "Friend", "9876543211")));

        SOSAlert alert = new AlertService(files).sendSOS(user);

        assertEquals(1, files.readAlerts().size());
        assertEquals(alert.getAlertId(), files.readAlerts().get(0)[0]);
        assertEquals("SENT (SIMULATED)", files.readAlerts().get(0)[5]);
    }

    @Test
    void registrationValidatesAndPersistsUser() throws Exception {
        FileManager files = new FileManager(tempDir);
        UserService service = new UserService(files);

        assertThrows(womensafety.exception.ValidationException.class,
                () -> service.registerUser("Demo", "bad-phone", "demo@example.test", "pw"));
        var user = service.registerUser("Demo", "9876543210", "demo@example.test", "pw");

        assertNotEquals("pw", files.readUsers().get(0).getPassword());
        assertTrue(files.readUsers().get(0).getPassword().startsWith("pbkdf2-sha256$"));
        assertNotNull(service.login(user.getUserId(), "pw"));
        assertNotNull(service.login("9876543210", "pw"));
        assertNull(service.login(user.getUserId(), "wrong"));
    }

    @Test
    void sampleDataIsInitializedOnlyOnce() {
        FileManager files = new FileManager(tempDir);

        SampleData.initialize(files);
        SampleData.initialize(files);

        assertEquals(5, files.readUsers().size());
        assertEquals(5, files.readContacts("U001").size());
        assertEquals(5, files.readAlerts().size());
        assertEquals(6, files.readSafePlaces().size());
        assertEquals(6, new SafePlaceService(files).getAllSafePlaces().size());
        assertNotNull(files.readLocation("U001"));
    }

    @Test
    void emergencyNumbersAreSavedPerUserAndReloaded() {
        FileManager files = new FileManager(tempDir);

        files.writeEmergencyNumbers("U100", "+1 800-555-0100", "020 555 0123");

        assertArrayEquals(new String[]{"U100", "+1 800-555-0100", "020 555 0123"},
                files.readEmergencyNumbers("U100"));
        assertArrayEquals(new String[]{"U200", "", ""}, files.readEmergencyNumbers("U200"));
    }

    @Test
    void productionInitializationDoesNotCreateDemoAccountsOrPlaces() {
        FileManager files = new FileManager(tempDir);

        SampleData.initialize(files, false);

        assertTrue(files.readUsers().isEmpty());
        assertTrue(files.readAlerts().isEmpty());
        assertTrue(files.readSafePlaces().isEmpty());
        assertTrue(new SafePlaceService(files).findNearestPlace().isEmpty());
    }

    @Test
    void legacyDemoPasswordIsHashedAfterSuccessfulLogin() {
        FileManager files = new FileManager(tempDir);
        SampleData.initialize(files);
        UserService service = new UserService(files);

        assertNotNull(service.login("U001", "1234"));
        assertTrue(files.readUsers().get(0).getPassword().startsWith("pbkdf2-sha256$"));
        assertNotNull(service.login("U001", "1234"));
    }
}
