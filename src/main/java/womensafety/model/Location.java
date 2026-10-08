package womensafety.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Location {
    private String locationId;
    private double latitude;
    private double longitude;
    private String address;
    private String timestamp;

    public Location(String locationId, double latitude, double longitude, String address) {
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        updateLocation(latitude, longitude, address);
    }

    public void updateLocation(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String displayLocation() { return address + " (" + latitude + ", " + longitude + ")"; }
}
