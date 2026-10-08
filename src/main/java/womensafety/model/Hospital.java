package womensafety.model;

public class Hospital extends SafePlace {
    private final boolean emergencyAvailable;
    public Hospital(String id, String name, String location, double distance, boolean emergencyAvailable) {
        super(id, name, location, distance, "Hospital");
        this.emergencyAvailable = emergencyAvailable;
    }
    @Override public String displayDetails() {
        return super.displayDetails() + (emergencyAvailable ? " | Emergency care available" : "");
    }
}
