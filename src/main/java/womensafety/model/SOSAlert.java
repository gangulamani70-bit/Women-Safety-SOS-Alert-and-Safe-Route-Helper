package womensafety.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class SOSAlert {
    private final String alertId;
    private final User user;
    private final Location location;
    private final String date;
    private final String time;
    private String status;

    public SOSAlert(User user, Location location) {
        this.alertId = "A-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.user = user;
        this.location = location;
        LocalDateTime now = LocalDateTime.now();
        this.date = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        this.time = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        this.status = "SENT (SIMULATED)";
    }
    public String getAlertId() { return alertId; }
    public User getUser() { return user; }
    public Location getLocation() { return location; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public void updateStatus(String status) { this.status = status; }
    public String getAlertDetails() { return alertId + " | " + user.getName() + " | " + location.getAddress() + " | " + date + " " + time + " | " + status; }
    @Override public String toString() { return getAlertDetails(); }
}
