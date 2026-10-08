package womensafety.model;

public class Police extends SafePlace {
    private final String stationType;
    public Police(String id, String name, String location, double distance, String stationType) {
        super(id, name, location, distance, "Police");
        this.stationType = stationType;
    }
    @Override public String displayDetails() { return super.displayDetails() + " | " + stationType + " police station"; }
}
