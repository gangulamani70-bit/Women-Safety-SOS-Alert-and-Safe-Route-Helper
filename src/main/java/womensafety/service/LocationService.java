package womensafety.service;

import womensafety.model.Location;
import womensafety.model.User;
import womensafety.util.FileManager;

import java.util.List;

public class LocationService {
    private final FileManager files;
    private final List<Location> available = List.of(
            new Location("L001", 17.4483, 78.3915, "Madhapur, Hyderabad"),
            new Location("L002", 17.4435, 78.3772, "Hitech City, Hyderabad"),
            new Location("L003", 17.4401, 78.3489, "Gachibowli, Hyderabad"),
            new Location("L004", 17.4933, 78.3997, "Kukatpally, Hyderabad"),
            new Location("L005", 17.4316, 78.4071, "Jubilee Hills, Hyderabad"));

    public LocationService(FileManager files) { this.files = files; }
    public List<Location> getAvailableLocations() { return available; }
    public Location getCurrentLocation(User user) {
        Location location = files.readLocation(user.getUserId());
        if (location != null) user.updateLocation(location);
        return location;
    }
    public void updateLocation(User user, Location selected) {
        Location updated = new Location(selected.getLocationId(), selected.getLatitude(), selected.getLongitude(), selected.getAddress());
        files.writeLocation(user.getUserId(), updated);
        user.updateLocation(updated);
    }
}
