package womensafety.model;

public class SafePlace {
    protected String placeId;
    protected String name;
    protected String location;
    protected double distance;
    private final String type;

    public SafePlace(String placeId, String name, String location, double distance, String type) {
        this.placeId = placeId;
        this.name = name;
        this.location = location;
        this.distance = distance;
        this.type = type;
    }
    public String displayDetails() { return name + " (" + type + "), " + location + " - " + distance + " km"; }
    public String getPlaceId() { return placeId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getDistance() { return distance; }
    public String getType() { return type; }
}
